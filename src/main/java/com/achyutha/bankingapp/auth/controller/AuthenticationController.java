package com.achyutha.bankingapp.auth.controller;

import com.achyutha.bankingapp.auth.dto.LoginRequest;
import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.model.RoleType;
import com.achyutha.bankingapp.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

/**
 * Authentication Controller.
 */
@RestController
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthService authService;

    /**
     * Login page, for a registered customer.
     * @param loginRequest The login request payload mapped to an appropriate DTO.
     * @return If authentication is successful, the JwtResponse is sent along with status ok.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        return authService.signIn(loginRequest);
    }

    /**
     * To singup, if not registered already.
     * @param signUpRequest The signUpRequest payload mapped to an appropriate DTO.
     * @return Appropriate response in String.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if(signUpRequest.getRole().contains(RoleType.ROLE_ADMIN)) {
            return authService.signUp(signUpRequest);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot Add a non-admin user directly.");
    }

}
