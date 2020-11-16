package com.achyutha.bankingapp.auth.controller;

import com.achyutha.bankingapp.auth.dto.LoginRequest;
import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

//    private final AuthenticationManager authenticationManager;

//    private final UserRepository userRepository;
//
//    private final RoleRepository roleRepository;
//
//    private final PasswordEncoder encoder;

//    private final JwtUtils jwtUtils;
//
    private final AuthService authService;

    /**
     * To login a registered user.
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

        return authService.signUp(signUpRequest);
    }
}
