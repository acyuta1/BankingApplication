package com.achyutha.bankingapp.auth.service;

import com.achyutha.bankingapp.auth.dto.JwtResponse;
import com.achyutha.bankingapp.auth.dto.LoginRequest;
import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.dto.SignUpResponse;
import com.achyutha.bankingapp.auth.jwt.JwtUtils;
import com.achyutha.bankingapp.auth.jwt.UserDetailsImpl;
import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.common.Utils;
import com.achyutha.bankingapp.common.validation.group.AdminLevelValidation;
import com.achyutha.bankingapp.common.validation.group.CustomerLevelValidation;
import com.achyutha.bankingapp.common.validation.group.EmployeeLevelValidation;
import com.achyutha.bankingapp.domain.converter.RoleConverter;
import com.achyutha.bankingapp.domain.model.AccountModels.CurrentAccount;
import com.achyutha.bankingapp.domain.model.AccountStatus;
import com.achyutha.bankingapp.domain.model.AccountType;
import com.achyutha.bankingapp.domain.model.KycVerificationStatus;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.service.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Validator;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.auth.model.RoleType.*;
import static com.achyutha.bankingapp.common.Constants.EMPLOYEE_ID_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    private final JwtUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

    private final RoleConverter roleConverter;

    private final Validator validator;

    private void checkForErrors(Object signUpRequest, Class<?>... classes) {
        var errors = validator.validate(signUpRequest, classes);
        if (errors.size() > 0) {
            log.trace("Found error while validating account request dto");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "errors: " + errors.toString());
        }
    }

    private void validateRoleSignUpFields(SignUpRequest signUpRequest) {
        if (signUpRequest.getRole().contains(ROLE_ADMIN)) {
            checkForErrors(signUpRequest, AdminLevelValidation.class);
        }
        if (signUpRequest.getRole().contains(ROLE_EMPLOYEE)) {
            checkForErrors(signUpRequest, EmployeeLevelValidation.class);
        }
        if (signUpRequest.getRole().contains(ROLE_CUSTOMER)) {
            checkForErrors(signUpRequest, CustomerLevelValidation.class);
        }

    }

    @Override
    public ResponseEntity<SignUpResponse> signUp(SignUpRequest signUpRequest) {
        // Checking whether a customer with the same email exists already.
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            log.error("User exists already.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user already present.");
        }

        validateRoleSignUpFields(signUpRequest);

        // Create new user entry.
        var user = new User()
                .setFirstName(signUpRequest.getFirstName())
                .setLastName(signUpRequest.getLastName())
                .setDob(signUpRequest.getDob())
                .setEmail(signUpRequest.getEmail())
                .setUserStatus(signUpRequest.getUserStatus())
                .setPassword(encoder.encode(signUpRequest.getPassword()));

        /*
         Check if the role is admin or employee, if yes, Employee ID and username has to be set.
         If the user is a customer, email itself will be the username.
         */
        if (!signUpRequest.getRole().contains(ROLE_CUSTOMER)) {
            log.debug("Non customer role: {}", signUpRequest.getRole());

            var kyc = signUpRequest.getKyc()
                    .setId(UUID.randomUUID().toString())
                    .setKycVerificationStatus(KycVerificationStatus.verified)
                    .setDob(signUpRequest.getDob());
            if (signUpRequest.getRole().contains(ROLE_ADMIN))
                checkForErrors(kyc, AdminLevelValidation.class);
            if (signUpRequest.getRole().contains(ROLE_EMPLOYEE))
                checkForErrors(kyc, EmployeeLevelValidation.class);
            user.setKyc(kyc);

            var latestId = userRepository.findFirstByOrderByIdDesc();
            if (latestId.isEmpty())
                user.setEmployeeId(String.format("%s%s", EMPLOYEE_ID_PREFIX, 1));
            else
                user.setEmployeeId(String.format("%s%s", EMPLOYEE_ID_PREFIX, latestId.get().getId() + 1));

            var currentAccount = ((CurrentAccount) new CurrentAccount()
                    .setEmployer("Bank App")
                    .setId(UUID.randomUUID().toString())
                    .setAccountStatus(AccountStatus.active)
                    .setAccountType(AccountType.current)
                    .setUser(user));

            user.setUsername(Utils.generateEmailFromName(user.getFirstName(), user.getEmployeeId())).setAccounts(Set.of(currentAccount));
            kyc.setUserName(user.getUsername());
        } else
            user.setUsername(user.getEmail());

        var strRoles = signUpRequest.getRole();
        var roles = new HashSet<Role>();

        // Every user, irrespective of whether an admin, employee will have a current account opened, hence is a customer too.
        roles.add(roleConverter.convert(ROLE_CUSTOMER));
        strRoles.forEach(role -> {
            if (!role.equals(ROLE_CUSTOMER))
                roles.add(roleConverter.convert(role));
        });
        userRepository.save(user.setRoles(roles));
        return ResponseEntity.ok(new SignUpResponse().setId(user.getId()).setUserName(user.getUsername()));
    }

    @Override
    public ResponseEntity<?> signIn(LoginRequest loginRequest) {
        // Username and password are authenticated.
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // A token is constructed based on the authentication principal.
        var jwt = jwtUtils.generateJwtToken(authentication);

        var userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Roles of the user.
        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse().setRoles(roles).setToken(jwt).setId(userDetails.getId()).setName(userDetails.getName()).setEmployeeId(userDetails.getEmployeeId()));
    }
}
