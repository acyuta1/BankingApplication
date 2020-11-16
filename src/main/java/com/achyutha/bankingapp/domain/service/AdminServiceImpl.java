package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.auth.service.AuthService;
import com.achyutha.bankingapp.domain.converter.RoleConverter;
import com.achyutha.bankingapp.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.auth.model.RoleType.*;
import static com.achyutha.bankingapp.common.Utils.defaultInit;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AuthService authService;

    private final UserRepository userRepository;

    private final RoleConverter roleConverter;

    @Override
    public ResponseEntity<?> addEmployee(SignUpRequest signupRequest) {
        return ResponseEntity.ok(String.format("%s and password - %s, please update asap to activate account.",
                authService
                        .signUp(defaultInit(signupRequest, ROLE_EMPLOYEE)).getBody(), signupRequest.getPassword()));
    }

    @Override
    public ResponseEntity<?> deleteEmployee(User user) {
        if (user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()).contains(ROLE_ADMIN))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("Cannot delete user - %s, since the user is an admin.", user.getUsername()));
        userRepository.delete(user);
        return ResponseEntity.ok(String.format("User %s deleted successfully.", user.getFirstName()));
    }

    @Override
    public List<User> getAllEmployees() {
        return userRepository.findByRoles_(Objects.requireNonNull(roleConverter.convert(ROLE_EMPLOYEE)));
    }
}
