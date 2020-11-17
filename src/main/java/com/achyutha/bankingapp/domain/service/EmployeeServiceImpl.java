package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.service.AuthService;
import com.achyutha.bankingapp.common.BankApplicationProperties;
import com.achyutha.bankingapp.common.validation.group.CurrentAccountValidation;
import com.achyutha.bankingapp.common.validation.group.EmployeeLevelValidation;
import com.achyutha.bankingapp.common.validation.group.LoanAccountValidation;
import com.achyutha.bankingapp.common.validation.group.SavingsAccountValidation;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.AccountModels.*;
import com.achyutha.bankingapp.domain.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Validator;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.achyutha.bankingapp.auth.model.RoleType.ROLE_CUSTOMER;
import static com.achyutha.bankingapp.common.Constants.USER_NOT_FOUND;
import static com.achyutha.bankingapp.common.Utils.defaultInit;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final AuthService authService;

    private final BankApplicationProperties properties;

    private final KycRepository kycRepository;

    private final Validator validator;

    private final SavingsAccountRepository savingsAccountRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final LoanAccountRepository loanAccountRepository;


    private Map<AccountType, AccountRepository<? extends Account>> typeToRepositoryMap() {
        return Map.of(AccountType.savings, savingsAccountRepository);
    }

    private final AccountRequestRepository accountRequestRepository;

    @Override
    public User updateEmployee(User user, UpdateAfterCreation updateAfterCreation) {
        var errors = validator.validate(updateAfterCreation, EmployeeLevelValidation.class);
        if (errors.isEmpty())
            return userRepository.save(user.setDob(updateAfterCreation.getDob()).setPassword(encoder.encode(updateAfterCreation.getPassword())).setUserStatus(UserStatus.active));
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.toString());
    }

    @Override
    public ResponseEntity<?> addCustomer(SignUpRequest signUpRequest) {
        return ResponseEntity.ok(String.format("%s and password - %s, please update asap to activate account.",
                authService
                        .signUp(defaultInit(signUpRequest, ROLE_CUSTOMER)).getBody(), signUpRequest.getPassword()));
    }

    @Override
    public ResponseEntity<?> processKycRequest(Kyc kyc, Boolean approve) {
        if (kyc.getKycVerificationStatus().equals(KycVerificationStatus.verified) ||
                kyc.getKycVerificationStatus().equals(KycVerificationStatus.rejected))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already verified / rejected.");
        if (approve) {
            if (kyc.getNewPassword() == null || kyc.getNewPassword().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "new.password.empty");
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

    private void getErrors(AccountRequest accountRequest, Class<?>... classes) {
        var errors = validator.validate(accountRequest, classes);
        if (errors.size() > 0) {
            accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.rejected));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "errors: " + errors.toString());
        }
    }

    /**
     * To calculate the repayment total amount.
     * @return Total repayment amount.
     */
    private Double repaymentAmountCalc(Long loanAmount, RepaymentTenure repaymentTenure){
        return loanAmount * (repaymentTenure.getInterestRate()/100);
    }

    @Override
    public ResponseEntity<?> processAccRequest(AccountRequest accountRequest, Boolean approve) {
        if (accountRequest.getAccountRequestStatus().equals(AccountRequestStatus.processed) ||
                accountRequest.getAccountRequestStatus().equals(AccountRequestStatus.rejected))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already processed or rejected.");
        if (approve) {
            var typeRequested = accountRequest.getAccountType();
            if (typeRequested.equals(AccountType.savings)) {
                getErrors(accountRequest, SavingsAccountValidation.class);
                savingsAccountRepository.save((SavingsAccount) new SavingsAccount()
                        .setTransactionsRemaining(properties.getTransactionLimitSavings())
                        .setAccountType(AccountType.savings)
                        .setAccountStatus(AccountStatus.active)
                        .setUser(accountRequest.getUser())
                        .setId(UUID.randomUUID().toString()));

                accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.processed));
                return ResponseEntity.ok("Account created.");
            } else if (typeRequested.equals(AccountType.current)) {
                getErrors(accountRequest, SavingsAccountValidation.class, CurrentAccountValidation.class);
                var currentAccounts = accountRequestRepository
                        .findAllByUserAndAccountTypeAndAccountRequestStatus(accountRequest.getUser(), AccountType.current, AccountRequestStatus.processed);
                if (!currentAccounts.isEmpty()) {
                    accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.rejected));
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A user can only have one current account");
                }
                currentAccountRepository.save((CurrentAccount) new CurrentAccount()
                        .setEmployer(accountRequest.getEmployer())
                        .setId(UUID.randomUUID().toString())
                        .setAccountStatus(AccountStatus.active)
                        .setAccountType(AccountType.current)
                        .setUser(accountRequest.getUser()));
                accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.processed));
                return ResponseEntity.ok("Account created.");
            } else if (typeRequested.equals(AccountType.loan)) {
                getErrors(accountRequest, SavingsAccountValidation.class, LoanAccountValidation.class);
                var currentAccountEntry = currentAccountRepository.findByUser(accountRequest.getUser());
                if(currentAccountEntry.isEmpty())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current account must be present.");
                var currentAccount = currentAccountEntry.get();
                var transactions = currentAccount.getTransactions();
                transactions.add(new Transaction().setId(UUID.randomUUID().toString()).setTransactionDate(LocalDate.now()).setBalancePriorTransaction(currentAccount.getBalance()).setAccount(currentAccount));
                currentAccount.setBalance(currentAccount.getBalance() + accountRequest.getLoanAmount()).setTransactions(transactions);
                currentAccountRepository.save(currentAccount);
                loanAccountRepository.save((LoanAccount) new LoanAccount()
                        .setLoanAmount(accountRequest.getLoanAmount())
                        .setLastRepayment(0.0)
                        .setRepaymentTenure(accountRequest.getRepaymentTenure())
                        .setBalance(repaymentAmountCalc(accountRequest.getLoanAmount(),accountRequest.getRepaymentTenure()))
                        .setId(UUID.randomUUID().toString())
                        .setAccountStatus(AccountStatus.active)
                        .setAccountType(AccountType.loan)
                        .setUser(accountRequest.getUser()));
                accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.processed));
                return ResponseEntity.ok("Account created.");
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Incorrect request type.");
        }
        accountRequestRepository.save(accountRequest.setAccountRequestStatus(AccountRequestStatus.rejected));
        return ResponseEntity.ok("Rejected account request..");
    }

    @Override
    public List<AccountRequest> fetchAllPendingAccRequests() {
        return accountRequestRepository.findAllByAccountRequestStatus(AccountRequestStatus.submitted);
    }
}
