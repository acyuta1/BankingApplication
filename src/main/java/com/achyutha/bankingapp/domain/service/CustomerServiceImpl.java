package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.common.BankApplicationProperties;
import com.achyutha.bankingapp.common.validation.group.CurrentAccountValidation;
import com.achyutha.bankingapp.common.validation.group.CustomerLevelValidation;
import com.achyutha.bankingapp.common.validation.group.LoanAccountValidation;
import com.achyutha.bankingapp.common.validation.group.SavingsAccountValidation;
import com.achyutha.bankingapp.domain.dto.AccountRequestDto;
import com.achyutha.bankingapp.domain.dto.AmountTransaction;
import com.achyutha.bankingapp.domain.dto.TransferAmountDto;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.AccountModels.*;
import com.achyutha.bankingapp.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Validator;
import java.util.List;
import java.util.UUID;

import static com.achyutha.bankingapp.common.AccountUtils.constructTransaction;
import static com.achyutha.bankingapp.common.AccountUtils.setTransactionValues;
import static com.achyutha.bankingapp.common.Constants.*;

@Slf4j
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

    /**
     * Constructs a kyc object from the dto object.
     *
     * @param updateAfterCreation The kyc dto.
     * @param user                The user object.
     * @return The kyc object.
     */
    private Kyc constructKyc(UpdateAfterCreation updateAfterCreation, User user) {
        return new Kyc()
                .setId(UUID.randomUUID().toString())
                .setUserName(user.getUsername())
                .setNewPassword(updateAfterCreation.getPassword())
                .setPanCard(updateAfterCreation.getPanCard())
                .setAadharNumber(updateAfterCreation.getAadharNumber())
                .setDob(updateAfterCreation.getDob());
    }

    /**
     * To validateAccountRequest dto object.
     *
     * @param accountRequestDto The account request object.
     * @param classes           The classes - groups.
     */
    private void getErrors(AccountRequestDto accountRequestDto, Class<?>... classes) {
        var errors = validator.validate(accountRequestDto, classes);
        if (errors.size() > 0) {
            log.trace("Found error while validating account request dto");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "errors: " + errors.toString());
        }
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
        log.trace("In calculate method.");

        // If type is deposit, add with existing balance irrespective of current or savings account.
        if (transactionType.equals(TransactionType.deposit)) {
            log.trace("Depositing amount {}", amount);
            transaction.setMessage(String.format(DEPOSIT_MESSAGE, amount));
            return existingBalance + amount;
        }
        /*
        If the type is withdraw:
            i. For a savings account:
                a. The amount must not exceed withdraw limit.
                b. the withdraw amount must not result in withdrawal of min balance also.
            ii. For a current account:
                a. the withdraw amount must not exceed available balance.
         */
        else if (transactionType.equals(TransactionType.withdraw)) {
            if (accountType.equals(AccountType.savings)) {
                log.trace("Savings account, and withdrawing amount request.");
                if (amount > properties.getMaxWithdrawLimit())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Withdraw request limit exceeded.");
                if (existingBalance - properties.getMinBalanceSavings() < amount)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Withdraw request amount greater than balance - %s.", existingBalance));
                transaction.setMessage(String.format(WITHDRAW_MESSAGE, amount));
                return existingBalance - amount;
            } else if (accountType.equals(AccountType.current)) {
                log.trace("Current account, and withdrawing amount request.");
                if (existingBalance - amount < 0)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Withdraw request amount greater than balance - %s.", existingBalance));
                transaction.setMessage(String.format(WITHDRAW_MESSAGE, amount));
                return existingBalance - amount;
            }
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transaction type unknown.");
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
        log.trace("In savingsTransaction method.");

        // Checking if the savings account has not crossed transaction limit.
        if (savingsAccount.getTransactionsRemaining() > 0) {

            // Checking if this is the first transaction.
            if (savingsAccount.getTransactions().isEmpty()) {

                // If this is the first transaction, a min balance has to be deposited. And cannot be a withdraw request.
                if (amountTransaction.getTransactionType().equals(TransactionType.withdraw) || amountTransaction.getAmount() < properties.getMinBalanceSavings()) {
                    log.error("Cannot be a withdraw request and deposit amount has to be greater than min balance.");
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format("First transaction, a deposit of %s is necessary.", properties.getMinBalanceSavings()));
                }
                newTransaction.setMessage(String.format(DEPOSIT_MESSAGE, amountTransaction.getAmount()));
                savingsAccount.setBalance(amountTransaction.getAmount());
            } else {
                // if not the first transaction, proceed normally.
                log.trace("Calculating new balance.");
                savingsAccount.setBalance(calculate(amountTransaction.getTransactionType(), amountTransaction.getAmount(), savingsAccount.getBalance(), AccountType.savings, newTransaction));
            }
            return savingsAccountRepository.save((SavingsAccount) savingsAccount
                    .setTransactionsRemaining(savingsAccount.getTransactionsRemaining() - 1)
                    .setTransactions(setTransactionValues(newTransaction, savingsAccount, null)));
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
        return currentAccountRepository.save((CurrentAccount) currentAccount
                .setTransactions(setTransactionValues(newTransaction, currentAccount, null)));
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
        if (newBalance <= 0) {
            if (newBalance < 0) {
                var currentAccountEntry = currentAccountRepository.findByUser(loanAccount.getUser());
                if (currentAccountEntry.isEmpty())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current account must be present.");
                var currentAccount = currentAccountEntry.get();
                var currentTransaction = constructTransaction(currentAccount);
                currentAccount.setBalance(currentAccountEntry.get().getBalance() + (amountTransaction.getAmount() - loanAccount.getBalance()));
                currentAccountRepository.save((CurrentAccount) currentAccount
                        .setTransactions(setTransactionValues(currentTransaction, currentAccount, "Extra loan amount paid transferred/refunded.")));
            }
            newTransaction.setBalanceAfterTransaction(0.0);
            existingTransactions.add(newTransaction);
            return loanAccountRepository.save((LoanAccount) loanAccount.setLastRepayment(amountTransaction.getAmount()).setAccountStatus(AccountStatus.closed).setTransactions(existingTransactions).setBalance(0.0));
        }
        return loanAccountRepository.save((LoanAccount) loanAccount.setLastRepayment(amountTransaction.getAmount()).setBalance(newBalance).setTransactions(existingTransactions));
    }

    @Override
    public ResponseEntity<?> updateKyc(User user, UpdateAfterCreation updateAfterCreation) {

        // Validating updateAfterCreationDTO.
        var errors = validator.validate(updateAfterCreation, CustomerLevelValidation.class);
        if (errors.isEmpty()) {
            log.debug("No errors found during validation.");

            // Checking if an already existing kyc exists.
            var kycExisting = kycRepository.findByUserName(user.getUsername());

            if (kycExisting.isPresent()) {
                switch (kycExisting.get().getKycVerificationStatus()) {
                    // If exists and is pending, we throw an exception.
                    case pending:
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You have a pending kyc request present already.");
                        // If exists and is rejected, we create a new request.
                    case rejected: {
                        kycRepository.save(constructKyc(updateAfterCreation, user));
                        return ResponseEntity.ok("Submitted request.");
                    }
                    // If already verified, we throw an error.
                    case verified:
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your kyc is already verified.");
                }
            }
            log.debug("Saving a new request for user {}", user.getUsername());
            // Saving a new request.
            kycRepository.save(constructKyc(updateAfterCreation, user));
            return ResponseEntity.ok("Kyc submitted.");
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.toString());
    }

    @Override
    public AccountRequest requestForAccount(User user, AccountRequestDto accountRequestDto) {
        /*
        We first validate the dto, using appropriate groups.
        Only after validation, we proceed.
         */
        if (accountRequestDto.getAccountType().equals(AccountType.savings))
            getErrors(accountRequestDto, SavingsAccountValidation.class);
        else if (accountRequestDto.getAccountType().equals(AccountType.current))
            getErrors(accountRequestDto, SavingsAccountValidation.class, CurrentAccountValidation.class);
        else if (accountRequestDto.getAccountType().equals(AccountType.loan))
            getErrors(accountRequestDto, SavingsAccountValidation.class, LoanAccountValidation.class);

        // We add this new request to the existing request SET.
        var existingRequests = user.getAccountRequests();
        var accountRequest = new AccountRequest()
                .setId(UUID.randomUUID().toString())
                .setAccountType(accountRequestDto.getAccountType())
                .setUser(user)
                .setAccountRequestStatus(AccountRequestStatus.submitted);

        // Setting unique fields  of Current and Loan account types.
        if (accountRequestDto.getAccountType().equals(AccountType.current))
            accountRequest.setEmployer(accountRequestDto.getEmployer());
        if (accountRequestDto.getAccountType().equals(AccountType.loan))
            accountRequest.setLoanAmount(accountRequestDto.getLoanAmount()).setRepaymentTenure(accountRequestDto.getRepaymentTenure());

        existingRequests.add(accountRequest);
        userRepository.save(user.setAccountRequests(existingRequests));
        return accountRequest;
    }

    @Override
    public Account depositOrWithdrawFromAccount(User user, Account account, AmountTransaction amountTransaction) {

        // Checking if the account actually belongs to user.
        if (!user.getUsername().equals(account.getUser().getUsername())) {
            log.error("Account {} does not belong to {}", account.getId(), user.getUsername());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "account does not belong to user.");
        }

        // Checking if the account is actually operational.
        if (!account.getAccountStatus().equals(AccountStatus.active)) {
            log.error("The account {} is not active", account.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not active");
        }

        // A new transaction object.
        var transaction = constructTransaction(account);

        // Checking if the account type and the request dto account type actually match.
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
        // if the account actually belongs to the user.
        if (account.getUser().getUsername().equals(user.getUsername()))
            return account;
        log.error("Account {} does not belong to user {}", account.getId(), user.getUsername());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "account does not belong to user.");
    }

    @Override
    public Kyc getDetailsOfCustomer(User user, Kyc kyc) {
        // if the kyc actually belongs to the user.
        if (kyc.getUserName().equals(user.getUsername()))
            return kyc;
        log.error("Kyc {} does not belong to user {}", kyc.getId(), user.getUsername());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kyc does not belong to user.");
    }

    @Override
    public ResponseEntity<?> transferAmount(User user, Account account, TransferAmountDto transferAmountDto) {
        // Checking if the target account id is valid.
        var targetAccount = accountRepository.findById(transferAmountDto.getAccountId());
        if (targetAccount.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target account invalid.");
        if (account.getBalance() - transferAmountDto.getAmount() < 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount transfer request exceeds current balance.");
        if (account.getAccountType().equals(AccountType.loan) || targetAccount.get().getAccountType().equals(AccountType.loan))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can only send from/to a non loan account.");
        if (account.getId().equals(transferAmountDto.getAccountId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send to same account.");

        log.trace("Initiated transfer of funds.");
        var receiver = targetAccount.get();
        var transactionSender = constructTransaction(account).setBalancePriorTransaction(account.getBalance());
        var transactionReceiver = constructTransaction(receiver).setBalancePriorTransaction(receiver.getBalance());

        account.setBalance(account.getBalance() - transferAmountDto.getAmount());
        receiver.setBalance(receiver.getBalance() + transferAmountDto.getAmount());

        account.setTransactions(setTransactionValues(
                transactionSender,
                account,
                String.format(TRANSFER_AMOUNT, transferAmountDto.getAmount(), account.getId(), receiver.getId(), receiver.getUser().getUsername())
        ));

        receiver.setTransactions(setTransactionValues(
                transactionReceiver,
                receiver,
                String.format(RECEIVE_AMOUNT, transferAmountDto.getAmount(), account.getId(), account.getUser().getUsername())
        ));

        accountRepository.saveAll(List.of(account, receiver));
        return ResponseEntity.ok("Transferred Successfully.");
    }
}
