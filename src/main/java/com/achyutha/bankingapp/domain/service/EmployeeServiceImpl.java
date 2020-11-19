package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.dto.SignUpResponse;
import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.auth.service.AuthService;
import com.achyutha.bankingapp.common.BankApplicationProperties;
import com.achyutha.bankingapp.common.Utils;
import com.achyutha.bankingapp.common.validation.group.CurrentAccountValidation;
import com.achyutha.bankingapp.common.validation.group.EmployeeLevelValidation;
import com.achyutha.bankingapp.common.validation.group.LoanAccountValidation;
import com.achyutha.bankingapp.common.validation.group.SavingsAccountValidation;
import com.achyutha.bankingapp.domain.converter.RoleConverter;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.AccountModels.Account;
import com.achyutha.bankingapp.domain.model.AccountModels.CurrentAccount;
import com.achyutha.bankingapp.domain.model.AccountModels.LoanAccount;
import com.achyutha.bankingapp.domain.model.AccountModels.SavingsAccount;
import com.achyutha.bankingapp.domain.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.auth.model.RoleType.ROLE_CUSTOMER;
import static com.achyutha.bankingapp.common.AccountUtils.constructTransaction;
import static com.achyutha.bankingapp.common.AccountUtils.setTransactionValues;
import static com.achyutha.bankingapp.common.Constants.LOAN_AMOUNT_CREDITED;
import static com.achyutha.bankingapp.common.Constants.USER_NOT_FOUND;
import static com.achyutha.bankingapp.common.Utils.defaultInit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final AuthService authService;

    private final BankApplicationProperties properties;

    private final Utils utils;

    private final KycRepository kycRepository;

    private final SavingsAccountRepository savingsAccountRepository;

    private final CurrentAccountRepository currentAccountRepository;

    private final LoanAccountRepository loanAccountRepository;

    private final RoleConverter roleConverter;

    private final AccountRequestRepository accountRequestRepository;

    /**
     * To calculate the repayment total amount.
     *
     * @return Total repayment amount.
     */
    private Double repaymentAmountCalc(Long loanAmount, RepaymentTenure repaymentTenure) {
        return loanAmount * (repaymentTenure.getInterestRate() / 100);
    }

    /**
     * If approved by an employee, the savings account will be created.
     *
     * @param accountRequest The Account request.
     */
    private void createSavingsAccount(AccountRequest accountRequest) {
        //  Validating errors.
        utils.checkForErrors(accountRequest, SavingsAccountValidation.class);

        // Creating Savings account, now that validation was successful.
        savingsAccountRepository.save((SavingsAccount) new SavingsAccount()
                .setTransactionsRemaining(properties.getTransactionLimitSavings())
                .setAccountType(AccountType.savings)
                .setAccountStatus(AccountStatus.active)
                .setUser(accountRequest.getUser())
                .setId(UUID.randomUUID().toString()));

        // Setting account request as processed.
        accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.processed));
    }

    /**
     * If approved by an employee, the current account will be created.
     *
     * @param accountRequest The Account request.
     */
    private void createCurrentAccount(AccountRequest accountRequest) {
        //  Validating errors.
        utils.checkForErrors(accountRequest, SavingsAccountValidation.class, CurrentAccountValidation.class);

        // Checking if a current account already exists. Only one current account per user is allowed.
        var currentAccounts = accountRequestRepository
                .findAllByUserAndAccountTypeAndAccountRequestStatus(accountRequest.getUser(), AccountType.current, AccountRequestStatus.processed);
        if (!currentAccounts.isEmpty()) {
            log.error("Existing current account found.");
            accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.rejected));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A user can only have one current account");
        }

        // Creating current account, now that validation was successful.
        currentAccountRepository.save((CurrentAccount) new CurrentAccount()
                .setEmployer(accountRequest.getEmployer())
                .setId(UUID.randomUUID().toString())
                .setAccountStatus(AccountStatus.active)
                .setAccountType(AccountType.current)
                .setUser(accountRequest.getUser()));

        // Setting account request as processed.
        accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.processed));
    }


    /**
     * If approved by an employee, the loan account will be created.
     *
     * @param accountRequest The Account request.
     */
    private void createLoanAccount(AccountRequest accountRequest) {
        //  Validating errors.
        utils.checkForErrors(accountRequest, SavingsAccountValidation.class, LoanAccountValidation.class);

        /*
        Checking if a user has a current account.
        Only a customer with current account present can possess a loan account.
         */
        var currentAccountEntry = currentAccountRepository.findByUser(accountRequest.getUser());
        if (currentAccountEntry.isEmpty()) {
            log.error("Current account not present.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current account must be present.");
        }

        // Creating loan account, now that validation was successful.
        var loanAccount = (LoanAccount) new LoanAccount()
                .setLoanAmount(accountRequest.getLoanAmount())
                .setLastRepayment(0.0)
                .setRepaymentTenure(accountRequest.getRepaymentTenure())
                .setBalance(accountRequest.getLoanAmount() + repaymentAmountCalc(accountRequest.getLoanAmount(), accountRequest.getRepaymentTenure()))
                .setId(UUID.randomUUID().toString())
                .setAccountStatus(AccountStatus.active)
                .setAccountType(AccountType.loan)
                .setUser(accountRequest.getUser());


        // Setting new transaction since loan approval and crediting itself is a transaction.
        loanAccount.setTransactions(setTransactionValues(constructTransaction(loanAccount).setBalancePriorTransaction(0.0),
                loanAccount, String.format(LOAN_AMOUNT_CREDITED, accountRequest.getLoanAmount())));
        loanAccountRepository.save(loanAccount);

        // The approved amount will be credited to current account.
        var currentAccount = currentAccountEntry.get();
        var transaction = constructTransaction(currentAccount);
        // Balance is updated.
        currentAccount.setBalance(currentAccount.getBalance() + accountRequest.getLoanAmount());
        currentAccount.setTransactions(setTransactionValues(transaction, currentAccount, String.format(LOAN_AMOUNT_CREDITED, accountRequest.getLoanAmount())));
        currentAccountRepository.save(currentAccount);

        accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.processed));
    }

    @Override
    public User updateEmployee(User user, UpdateAfterCreation updateAfterCreation) {
        // Validating employee object.
        utils.checkForErrors(updateAfterCreation, EmployeeLevelValidation.class);
        log.debug("User validation successful. Employee is now active.");
        return userRepository.save(user.setDob(updateAfterCreation.getDob()).setPassword(encoder.encode(updateAfterCreation.getPassword())).setUserStatus(UserStatus.active));
    }

    @Override
    public ResponseEntity<?> addCustomer(SignUpRequest signUpRequest) {
        return ResponseEntity.ok(((SignUpResponse) Objects.requireNonNull(authService
                .signUp(defaultInit(signUpRequest, ROLE_CUSTOMER)).getBody())).setTempPassword(signUpRequest.getPassword()));
    }

    @Override
    public ResponseEntity<?> processKycRequest(Kyc kyc, Boolean approve) {
        //  Checking if the kyc is verified already.
        if (kyc.getKycVerificationStatus().equals(KycVerificationStatus.verified) ||
                kyc.getKycVerificationStatus().equals(KycVerificationStatus.rejected)) {
            log.error("Attempting to process an already rejected/approved request.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already verified / rejected.");
        }
        if (approve) {
            // Checking if the password is null or blank.
            if (kyc.getNewPassword() == null || kyc.getNewPassword().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "new.password.empty");

            // Setting new password and linking kyc.
            var user = userRepository.findByUsername(kyc.getUserName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
            userRepository.save(user.setPassword(encoder.encode(kyc.getNewPassword())).setDob(kyc.getDob()).setKyc(kyc));
            kycRepository.save(kyc.setKycVerificationStatus(KycVerificationStatus.verified).setNewPassword(null));
            return ResponseEntity.ok("Changed status to verified.");
        }
        kycRepository.save(kyc.setKycVerificationStatus(KycVerificationStatus.rejected));
        return ResponseEntity.ok("Rejected the kyc verification request..");
    }

    @Override
    public List<Kyc> fetchAllPendingKyc() {
        return kycRepository.findAllByKycVerificationStatus(KycVerificationStatus.pending);
    }

    @Override
    public ResponseEntity<?> processAccRequest(AccountRequest accountRequest, Boolean approve) {

        //  Checking if the kyc is verified already.
        if (accountRequest.getAccountRequestStatus().equals(AccountRequestStatus.processed) ||
                accountRequest.getAccountRequestStatus().equals(AccountRequestStatus.rejected)) {
            log.error("Attempting to process an already rejected/approved request.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already processed or rejected.");
        }
        if (approve) {
            var typeRequested = accountRequest.getAccountType();
            switch (typeRequested) {
                case savings: {
                    log.debug("Creating savings account.");
                    createSavingsAccount(accountRequest);
                    return ResponseEntity.ok("Savings account created.");
                }
                case current: {
                    log.debug("Creating current account.");
                    createCurrentAccount(accountRequest);
                    return ResponseEntity.ok("Credit account created.");
                }
                case loan: {
                    log.debug("Creating loan account.");
                    createLoanAccount(accountRequest);
                    return ResponseEntity.ok("Loan account created.");
                }
            }
            log.error("Account type is incorrect.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect Account type.");
        }
        // Rejecting account request.
        log.debug("Account request {} is rejected.", accountRequest.getId());
        accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.rejected));
        return ResponseEntity.ok("Rejected account request..");
    }

    @Override
    public List<AccountRequest> fetchAllPendingAccRequests() {
        return accountRequestRepository.findAllByAccountRequestStatus(AccountRequestStatus.submitted);
    }

    @Override
    public List<User> fetchAllCustomers() {
        return userRepository.findByRoles_(Objects.requireNonNull(roleConverter.convert(ROLE_CUSTOMER)));
    }

    @Override
    public ResponseEntity<?> deleteCustomer(User user, User customer) {
        var roles = customer.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        // Only customers can be deleted. (Since an employee or an admin can be a user too, we must distinguish first.
        System.out.println(roles);
        if (roles.size() == 1 && roles.get(0).equals(ROLE_CUSTOMER)) {
            var accounts = customer.getAccounts();
            for (Account account : accounts) {
                if (!account.getAccountStatus().equals(AccountStatus.closed)) {
                    log.error("Active accounts found.");
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot be deleted as the customer " +
                            "has active accounts.");
                }
            }
            // Soft deletion, since records are crucial.
            log.error("Soft deletion of user {}.", customer.getUsername());
            userRepository.save(customer.setUserStatus(UserStatus.inactive));
            return ResponseEntity.ok("Deleted successfully.");
        }
        log.error("Customer {} is not eligible for deletion", customer.getUsername());
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer is not eligible for deletion.");
    }
}
