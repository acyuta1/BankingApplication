package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.common.BankApplicationProperties;
import com.achyutha.bankingapp.common.validation.group.CurrentAccountValidation;
import com.achyutha.bankingapp.common.validation.group.KycGroup;
import com.achyutha.bankingapp.common.validation.group.LoanAccountValidation;
import com.achyutha.bankingapp.common.validation.group.SavingsAccountValidation;
import com.achyutha.bankingapp.domain.dto.AccountRequestDto;
import com.achyutha.bankingapp.domain.dto.AmountTransaction;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {


    private final KycRepository kycRepository;

    private final Validator validator;

    private final UserRepository userRepository;

    private final BankApplicationProperties properties;

    private final SavingsAccountRepository savingsAccountRepository;
    private final CurrentAccountRepository currentAccountRepository;

    private final AccountRepository accountRepository;

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
    private Double calculate(TransactionType transactionType, Double amount, Double existingBalance, AccountType accountType) {
        if (transactionType.equals(TransactionType.deposit))
            return existingBalance + amount;
        else if (transactionType.equals(TransactionType.withdraw)) {
            if (accountType.equals(AccountType.savings)) {
                if (amount > properties.getMaxWithdrawLimit())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Withdraw request limit exceeded.");
                if (existingBalance - properties.getMinBalanceSavings() < amount)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Withdraw request amount greater than balance - %s.", existingBalance));
            } else if (accountType.equals(AccountType.current)) {
                if (existingBalance - amount < 0)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Withdraw request amount greater than balance - %s.", existingBalance));
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
                .setBalancePriorTransaction(account.getBalance());
//        var accountCasted = isSavings ? (SavingsAccount) account : (CurrentAccount) account;
//        transaction.setBalanceAfterTransaction(accountCasted.getBalance());
//        var existingTransactions = accountCasted.getTransactions();
//        existingTransactions.add(transaction.setId(UUID.randomUUID().toString()));
//        return savingsAccountRepository.save(accountCasted.
//                .setTransactionsRemaining(accountCasted.getTransactionsRemaining() - 1)
//                .setTransactions(existingTransactions))
//                .setId(UUID.randomUUID().toString());
    }

    @Override
    public Account depositOrWithdrawFromAccount(User user, Account account, AmountTransaction amountTransaction) {
        var transaction = constructTransaction(account);
        if(!account.getAccountStatus().equals(AccountStatus.active))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "not active");
        if (amountTransaction.getAccountType().equals(AccountType.savings) && account.getAccountType().equals(AccountType.savings)) {
            var savingsAccount = (SavingsAccount) account;
            if (savingsAccount.getTransactionsRemaining() > 0) {
                if (savingsAccount.getTransactions().isEmpty()) {
                    if (amountTransaction.getTransactionType().equals(TransactionType.withdraw) || amountTransaction.getAmount() < properties.getMinBalanceSavings())
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                String.format("First transaction, a deposit of %s is necessary.", properties.getMinBalanceSavings()));
                    savingsAccount.setBalance(amountTransaction.getAmount());
                } else
                    savingsAccount.setBalance(calculate(amountTransaction.getTransactionType(), amountTransaction.getAmount(), savingsAccount.getBalance(), AccountType.savings));
                transaction.setBalanceAfterTransaction(savingsAccount.getBalance());
                var existingTransactions = savingsAccount.getTransactions();
                existingTransactions.add(transaction.setId(UUID.randomUUID().toString()));
                return savingsAccountRepository.save((SavingsAccount) savingsAccount
                        .setTransactionsRemaining(savingsAccount.getTransactionsRemaining() - 1)
                        .setTransactions(existingTransactions))
                        .setId(UUID.randomUUID().toString());
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ran out of transactions for the month.");
        } else if (amountTransaction.getAccountType().equals(AccountType.current) && account.getAccountType().equals(AccountType.current)) {
            var currentAccount = (CurrentAccount) account;
            currentAccount.setBalance(calculate(amountTransaction.getTransactionType(), amountTransaction.getAmount(), currentAccount.getBalance(), AccountType.current));
            transaction.setBalanceAfterTransaction(currentAccount.getBalance());
            var existingTransactions = currentAccount.getTransactions();
            existingTransactions.add(transaction.setId(UUID.randomUUID().toString()));
            return currentAccountRepository.save((CurrentAccount) currentAccount
                    .setTransactions(existingTransactions))
                    .setId(UUID.randomUUID().toString());
        } else if(amountTransaction.getAccountType().equals(AccountType.loan) && account.getAccountType().equals(AccountType.loan)){
            if(amountTransaction.getTransactionType().equals(TransactionType.withdraw))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot withdraw, amount was already debited.");
            var loanAccount = (LoanAccount) account;
            var amountPaid = calculate(amountTransaction.getTransactionType(), amountTransaction.getAmount(), loanAccount.getBalance(), AccountType.loan);
            if(loanAccount.getBalance() - amountPaid < 0) {
                var currentAccountEntry = currentAccountRepository.findByUser(loanAccount.getUser());
                if(currentAccountEntry.isEmpty())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current account must be present.");
                currentAccountRepository.save((CurrentAccount) currentAccountEntry.get().setBalance(currentAccountEntry.get().getBalance() + (loanAccount.getBalance() - amountPaid)));
                loanAccount.setAccountStatus(AccountStatus.closed);
            } if(loanAccount.getBalance() - amountPaid == 0)
                loanAccount.setAccountStatus(AccountStatus.closed);

            loanAccount.setLastRepayment(amountPaid).setBalance(loanAccount.getBalance() - amountPaid);
        }
        return null;
    }

    @Override
    public List<? extends Account> fetchAllAccountsOfUsers(User user) {
        return accountRepository.findAllByUser(user);
    }
}
