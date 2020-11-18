package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.common.BankApplicationProperties;
import com.achyutha.bankingapp.common.validation.group.CurrentAccountValidation;
import com.achyutha.bankingapp.common.validation.group.KycGroup;
import com.achyutha.bankingapp.common.validation.group.LoanAccountValidation;
import com.achyutha.bankingapp.common.validation.group.SavingsAccountValidation;
import com.achyutha.bankingapp.domain.dto.AccountRequestDto;
import com.achyutha.bankingapp.domain.dto.AmountTransaction;
import com.achyutha.bankingapp.domain.dto.TransferAmountDto;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.AccountModels.*;
import com.achyutha.bankingapp.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Validator;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.achyutha.bankingapp.common.Constants.*;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {


    private final KycRepository kycRepository;

    private final Validator validator;

    private final UserRepository userRepository;

    private final BankApplicationProperties properties;

    private final SavingsAccountRepository savingsAccountRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final LoanAccountRepository loanAccountRepository;

    private final AccountRepository<Account> accountRepository;

    @Override
    public ResponseEntity<?> updateKyc(User user, UpdateAfterCreation updateAfterCreation) {
        var errors = validator.validate(updateAfterCreation, KycGroup.class);
        if (errors.isEmpty()) {
            var kycExisting = kycRepository.findByUserName(user.getUsername());
            if (kycExisting.isPresent()) {
                switch (kycExisting.get().getKycVerificationStatus()) {
                    case pending:
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have a pending kyc request present already.");
                    case rejected: {
                        kycRepository.save(new Kyc()
                                .setId(UUID.randomUUID().toString())
                                .setUserName(user.getUsername())
                                .setNewPassword(updateAfterCreation.getPassword())
                                .setPanCard(updateAfterCreation.getPanCard())
                                .setAadharNumber(updateAfterCreation.getAadharNumber())
                                .setDob(updateAfterCreation.getDob()));
                        return ResponseEntity.ok("Submitted request.");
                    }
                    case verified:
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your kyc is already verified.");
                }
            }
            kycRepository.save(new Kyc().setId(UUID.randomUUID().toString()).setUserName(user.getUsername()).setNewPassword(updateAfterCreation.getPassword()).setPanCard(updateAfterCreation.getPanCard()).setAadharNumber(updateAfterCreation.getAadharNumber()).setDob(updateAfterCreation.getDob()));
            return ResponseEntity.ok("Kyc submitted.");
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.toString());
    }

    private void getErrors(AccountRequestDto accountRequestDto, Class<?>... classes) {
        var errors = validator.validate(accountRequestDto, classes);
        if (errors.size() > 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "errors: " + errors.toString());
    }

    @Override
    public AccountRequest requestForAccount(User user, AccountRequestDto accountRequestDto) {
        if (accountRequestDto.getAccountType().equals(AccountType.savings))
            getErrors(accountRequestDto, SavingsAccountValidation.class);
        else if (accountRequestDto.getAccountType().equals(AccountType.current))
            getErrors(accountRequestDto, SavingsAccountValidation.class, CurrentAccountValidation.class);
        else if (accountRequestDto.getAccountType().equals(AccountType.loan))
            getErrors(accountRequestDto, SavingsAccountValidation.class, LoanAccountValidation.class);

        var existingRequests = user.getAccountRequests();
        var accountRequest = new AccountRequest()
                .setId(UUID.randomUUID().toString())
                .setAccountType(accountRequestDto.getAccountType())
                .setUser(user)
                .setAccountRequestStatus(AccountRequestStatus.submitted);
        if (accountRequestDto.getAccountType().equals(AccountType.current))
            accountRequest.setEmployer(accountRequestDto.getEmployer());
        if (accountRequestDto.getAccountType().equals(AccountType.loan))
            accountRequest.setLoanAmount(accountRequestDto.getLoanAmount()).setRepaymentTenure(accountRequestDto.getRepaymentTenure());

        existingRequests.add(accountRequest);
        userRepository.save(user.setAccountRequests(existingRequests));
        return accountRequest;
    }

    /**
     * Calculate new balance - for savings and current.
     *
     * @param transactionType The transaction type.
     * @param amount          The amount.
     * @param existingBalance The existing balance.
     * @param accountType     The account type.
     * @return The New balance.
     */
    private Double calculate(TransactionType transactionType, Double amount, Double existingBalance, AccountType accountType, Transaction transaction) {
        if (transactionType.equals(TransactionType.deposit)) {
            transaction.setMessage(String.format(DEPOSIT_MESSAGE, amount));
            return existingBalance + amount;
        } else if (transactionType.equals(TransactionType.withdraw)) {
            if (accountType.equals(AccountType.savings)) {
                if (amount > properties.getMaxWithdrawLimit())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Withdraw request limit exceeded.");
                if (existingBalance - properties.getMinBalanceSavings() < amount)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Withdraw request amount greater than balance - %s.", existingBalance));
                transaction.setMessage(String.format(WITHDRAW_MESSAGE, amount));
                return existingBalance - amount;
            } else if (accountType.equals(AccountType.current)) {
                if (existingBalance - amount < 0)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Withdraw request amount greater than balance - %s.", existingBalance));
                transaction.setMessage(String.format(WITHDRAW_MESSAGE, amount));
                return existingBalance - amount;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transaction type unknown.");
    }

    /**
     * Returns a new transaction.
     *
     * @param account The account.
     * @return The transaction.
     */
    private Transaction constructTransaction(Account account) {
        return new Transaction()
                .setAccount(account)
                .setTransactionDate(LocalDate.now())
                .setId(UUID.randomUUID().toString())
                .setBalancePriorTransaction(account.getBalance());
    }

    /**
     * Transaction process of savings account.
     *
     * @param savingsAccount    The savings account.
     * @param amountTransaction The amountTransaction dto.
     * @param newTransaction    The new transaction entry.
     * @return The account after changes.
     */
    private Account savingsTransaction(SavingsAccount savingsAccount, AmountTransaction amountTransaction, Transaction newTransaction) {
        if (savingsAccount.getTransactionsRemaining() > 0) {
            if (savingsAccount.getTransactions().isEmpty()) {
                if (amountTransaction.getTransactionType().equals(TransactionType.withdraw) || amountTransaction.getAmount() < properties.getMinBalanceSavings())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format("First transaction, a deposit of %s is necessary.", properties.getMinBalanceSavings()));
                savingsAccount.setBalance(amountTransaction.getAmount());
            } else
                savingsAccount.setBalance(calculate(amountTransaction.getTransactionType(), amountTransaction.getAmount(), savingsAccount.getBalance(), AccountType.savings, newTransaction));
            newTransaction.setBalanceAfterTransaction(savingsAccount.getBalance());
            var existingTransactions = savingsAccount.getTransactions();
            existingTransactions.add(newTransaction);
            return savingsAccountRepository.save((SavingsAccount) savingsAccount
                    .setTransactionsRemaining(savingsAccount.getTransactionsRemaining() - 1)
                    .setTransactions(existingTransactions))
                    .setId(UUID.randomUUID().toString());
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ran out of transactions for the month.");
    }

    /**
     * Transaction process of current account.
     *
     * @param currentAccount    The current account.
     * @param amountTransaction The amountTransaction dto.
     * @param newTransaction    The new transaction entry.
     * @return The account after changes.
     */
    private Account currentAccountTransaction(CurrentAccount currentAccount, AmountTransaction amountTransaction, Transaction newTransaction) {
        currentAccount.setBalance(calculate(amountTransaction.getTransactionType(), amountTransaction.getAmount(), currentAccount.getBalance(), AccountType.current, newTransaction));
        newTransaction.setBalanceAfterTransaction(currentAccount.getBalance());
        var existingTransactions = currentAccount.getTransactions();
        existingTransactions.add(newTransaction);
        return currentAccountRepository.save((CurrentAccount) currentAccount
                .setTransactions(existingTransactions))
                .setId(UUID.randomUUID().toString());
    }

    /**
     * Transaction process of loan account.
     *
     * @param loanAccount       The loan account.
     * @param amountTransaction The amountTransaction dto.
     * @param newTransaction    The new transaction entry.
     * @return The account after changes.
     */
    private Account loanAccountTransaction(LoanAccount loanAccount, AmountTransaction amountTransaction, Transaction newTransaction) {
        if (amountTransaction.getTransactionType().equals(TransactionType.withdraw))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot withdraw, amount was already debited.");
        var newBalance = loanAccount.getBalance() - amountTransaction.getAmount();
        newTransaction.setBalanceAfterTransaction(newBalance).setMessage(String.format(LOAN_MESSAGE, amountTransaction.getAmount()));
        var existingTransactions = loanAccount.getTransactions();
        if (newBalance < 0) {
            var currentAccountEntry = currentAccountRepository.findByUser(loanAccount.getUser());
            if (currentAccountEntry.isEmpty())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current account must be present.");
            currentAccountRepository.save((CurrentAccount) currentAccountEntry.get().setBalance(currentAccountEntry.get().getBalance() + (amountTransaction.getAmount() - loanAccount.getBalance())));
            newTransaction.setBalanceAfterTransaction(0.0);
            existingTransactions.add(newTransaction);
            return loanAccountRepository.save((LoanAccount) loanAccount.setLastRepayment(amountTransaction.getAmount()).setAccountStatus(AccountStatus.closed).setTransactions(existingTransactions).setBalance(0.0));
        } else if (newBalance == 0) {
            newTransaction.setBalanceAfterTransaction(0.0);
            existingTransactions.add(newTransaction);
            return loanAccountRepository.save((LoanAccount) loanAccount.setLastRepayment(amountTransaction.getAmount()).setAccountStatus(AccountStatus.closed).setTransactions(existingTransactions).setBalance(0.0));
        }
        return loanAccountRepository.save((LoanAccount) loanAccount.setLastRepayment(amountTransaction.getAmount()).setBalance(newBalance).setTransactions(existingTransactions));
    }

    @Override
    public Account depositOrWithdrawFromAccount(User user, Account account, AmountTransaction amountTransaction) {
        if (!user.getUsername().equals(account.getUser().getUsername()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "account does not belong to user.");
        var transaction = constructTransaction(account);
        if (!account.getAccountStatus().equals(AccountStatus.active))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not active");
        if (amountTransaction.getAccountType().equals(AccountType.savings) && account.getAccountType().equals(AccountType.savings)) {
            return savingsTransaction((SavingsAccount) account, amountTransaction, transaction);
        } else if (amountTransaction.getAccountType().equals(AccountType.current) && account.getAccountType().equals(AccountType.current)) {
            return currentAccountTransaction((CurrentAccount) account, amountTransaction, transaction);
        } else if (amountTransaction.getAccountType().equals(AccountType.loan) && account.getAccountType().equals(AccountType.loan)) {
            return loanAccountTransaction((LoanAccount) account, amountTransaction, transaction);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Not a proper transaction request - %s with account - %s",
                amountTransaction.getAccountType(), account.getAccountType()));
    }

    @Override
    public List<? extends Account> fetchAllAccountsOfUsers(User user) {
        return accountRepository.findAllByUser(user);
    }

    @Override
    public Account getAccount(User user, Account account) {
        if (account.getUser().getUsername().equals(user.getUsername()))
            return account;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "account does not belong to user.");
    }

    @Override
    public Kyc getDetailsOfCustomer(User user, Kyc kyc) {
        if (kyc.getUserName().equals(user.getUsername()))
            return kyc;
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kyc does not belong to user.");
    }

    @Override
    public ResponseEntity<?> transferAmount(User user, Account account, TransferAmountDto transferAmountDto) {
        Optional<Account> targetAccount = accountRepository.findById(transferAmountDto.getAccountId());
        if (targetAccount.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target account invalid.");
        if (account.getBalance() - transferAmountDto.getAmount() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount transfer request exceeds current balance.");
        if (account.getAccountType().equals(AccountType.loan) || targetAccount.get().getAccountType().equals(AccountType.loan))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only send from/to a non loan account.");
        if (account.getId().equals(transferAmountDto.getAccountId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send to same account.");

        var receiver = targetAccount.get();
        var transactionSender = constructTransaction(account);
        var transactionReceiver = constructTransaction(receiver);

        transactionSender.setBalancePriorTransaction(account.getBalance()).setBalanceAfterTransaction(account.getBalance() - transferAmountDto.getAmount())
                .setMessage(String.format(TRANSFER_AMOUNT, transferAmountDto.getAmount(), account.getId(), receiver.getId(), receiver.getUser().getUsername()));

        transactionReceiver.setBalancePriorTransaction(receiver.getBalance()).setBalanceAfterTransaction(receiver.getBalance() + transferAmountDto.getAmount())
                .setMessage(String.format(RECEIVE_AMOUNT, transferAmountDto.getAmount(), account.getId(), account.getUser().getUsername()));
        var existingTransactionsSender = account.getTransactions();
        var existingTransactionsReceiver = receiver.getTransactions();

        account.setBalance(account.getBalance() - transferAmountDto.getAmount()).setTransactions(existingTransactionsSender);
        receiver.setBalance(receiver.getBalance() + transferAmountDto.getAmount()).setTransactions(existingTransactionsReceiver);
        accountRepository.saveAll(List.of(account, receiver));
        return ResponseEntity.ok("Transferred Successfully.");
    }
}
