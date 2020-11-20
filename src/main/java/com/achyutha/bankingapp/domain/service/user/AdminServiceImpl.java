package com.achyutha.bankingapp.domain.service.user;

import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.dto.SignUpResponse;
import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.auth.service.AuthService;
import com.achyutha.bankingapp.common.Utils;
import com.achyutha.bankingapp.domain.converter.RoleConverter;
import com.achyutha.bankingapp.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.auth.model.RoleType.ROLE_ADMIN;
import static com.achyutha.bankingapp.auth.model.RoleType.ROLE_EMPLOYEE;
import static com.achyutha.bankingapp.common.Constants.*;
import static com.achyutha.bankingapp.common.Utils.defaultInit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AuthService authService;

    private final UserRepository userRepository;

    private final RoleConverter roleConverter;

    private final Utils utils;



    @Override
    public ResponseEntity<String> addEmployee(SignUpRequest signupRequest) {
        var success = ((SignUpResponse) Objects.requireNonNull(authService
                .signUp(defaultInit(signupRequest, ROLE_EMPLOYEE)).getBody())).setTempPassword(signupRequest.getPassword());
        utils.sendEmail(signupRequest.getEmail(), BANK_APP_EMAIL_SUBJECT, success.toString());
        return ResponseEntity.ok("Employee has been mailed with details.");
    }

    @Override
    public List<User> getAllEmployees() {
        // Fetching all users with role EMPLOYEE.
        return userRepository.findByRoles_(Objects.requireNonNull(roleConverter.convert(ROLE_EMPLOYEE)));
    }

    @Override
    public ResponseEntity<?> deleteEmployee(User user) {
        // Can only delete non admin users.
        if (user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()).contains(ROLE_ADMIN)) {
            log.error("Attempting to delete admin user.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format(CANNOT_DELETE_ADMIN, user.getUsername()));
        }
        userRepository.delete(user);
        return ResponseEntity.ok(String.format(USER_DELETED_SUCCESS, user.getFirstName()));
    }
}
