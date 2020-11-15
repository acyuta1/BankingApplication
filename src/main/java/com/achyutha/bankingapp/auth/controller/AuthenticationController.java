package com.achyutha.bankingapp.auth.controller;

import com.achyutha.bankingapp.auth.dto.JwtResponse;
import com.achyutha.bankingapp.auth.dto.LoginRequest;
import com.achyutha.bankingapp.auth.dto.SignupRequest;
import com.achyutha.bankingapp.auth.jwt.JwtUtils;
import com.achyutha.bankingapp.auth.jwt.UserDetailsImpl;
import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.service.RoleRepository;
import com.achyutha.bankingapp.domain.service.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.auth.model.RoleType.*;

/**
 * Authentication Controller.
 */
@RestController
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    /**
     * To login a registered user.
     * @param loginRequest The login request payload mapped to an appropriate DTO.
     * @return If authentication is successful, the JwtResponse is sent along with status ok.
     */
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var jwt = jwtUtils.generateJwtToken(authentication);

        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse().setRoles(roles).setToken(jwt).setId(userDetails.getId()).setName(userDetails.getName()));
    }

    /**
     * To singup, if not registered already.
     * @param signUpRequest The signUpRequest payload mapped to an appropriate DTO.
     * @return Appropriate response in String.
     */
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Username is already taken!");
        }

        // Create new user's account
        var user = new User()
                .setFullName(signUpRequest.getFullName())
                .setDob(signUpRequest.getDob())
                .setUsername(signUpRequest.getUsername())
                .setPassword(encoder.encode(signUpRequest.getPassword()));

        var strRoles = signUpRequest.getRole();
        var roles = new HashSet<Role>();

        if (strRoles == null) {
            var userRole = roleRepository.findByName(ROLE_CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case ROLE_ADMIN:
                        Role adminRole = roleRepository.findByName(ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case ROLE_EMPLOYEE:
                        Role modRole = roleRepository.findByName(ROLE_EMPLOYEE)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(modRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ROLE_CUSTOMER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully!");
    }
}
