package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.jwt.UserDetailsServiceImpl;
import com.achyutha.bankingapp.auth.service.AuthService;
import com.achyutha.bankingapp.common.validation.group.EmployeeLevelValidation;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.model.KycVerificationStatus;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Validator;
import java.util.List;

import static com.achyutha.bankingapp.auth.model.RoleType.ROLE_CUSTOMER;
import static com.achyutha.bankingapp.common.Constants.USER_NOT_FOUND;
import static com.achyutha.bankingapp.common.Utils.defaultInit;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final PasswordEncoder encoder;

    private final UserRepository userRepository;

    private final AuthService authService;

    private final UserDetailsServiceImpl userDetailsServiceImpl;

    private final KycRepository kycRepository;

    private final Validator validator;

    /**
     * To check whether an employee is active.
     * @param username The user name of employee.
     */
    private void isActive(String username){
        var user = userRepository.findByUsername(username);
        if(user.isEmpty() || !user.get().getUserStatus().equals(UserStatus.active))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "logged in user is not active.");
    }

    @Override
    public User updateEmployee(User user, UpdateAfterCreation updateAfterCreation) {
        var errors = validator.validate(updateAfterCreation, EmployeeLevelValidation.class);
        if(errors.isEmpty())
            return userRepository.save(user.setDob(updateAfterCreation.getDob()).setPassword(encoder.encode(updateAfterCreation.getPassword())).setUserStatus(UserStatus.active));
        else
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errors.toString());
    }

    @Override
    public ResponseEntity<?> addCustomer(SignUpRequest signUpRequest) {
        isActive(userDetailsServiceImpl.getCurrentLoggedInUser());
        return ResponseEntity.ok(String.format("%s and password - %s, please update asap to activate account.",
                authService
                .signUp(defaultInit(signUpRequest, ROLE_CUSTOMER)).getBody(), signUpRequest.getPassword()));
    }

    @Override
    public ResponseEntity<?> processKycRequest(Kyc kyc, Boolean approve) {
        if(approve){
            if(kyc.getNewPassword()==null || kyc.getNewPassword().isBlank())
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "new.password.empty");
            var user = userRepository.findByUsername(kyc.getUserName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
            userRepository.save(user.setPassword(encoder.encode(kyc.getNewPassword())).setDob(kyc.getDob()).setKyc(kyc));
            kycRepository.save(kyc.setKycVerificationStatus(KycVerificationStatus.verified).setNewPassword(null));
            return ResponseEntity.ok("Changed status to verified.");
        }
        kyc.setKycVerificationStatus(KycVerificationStatus.rejected);
        return ResponseEntity.ok("Rejected the kyc verification request..");
    }

    @Override
    public List<Kyc> fetchAllPendingKyc() {
        return kycRepository.findAllByKycVerificationStatus(KycVerificationStatus.pending);
    }
}
