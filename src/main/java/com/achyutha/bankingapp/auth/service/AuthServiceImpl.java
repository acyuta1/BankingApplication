package com.achyutha.bankingapp.auth.service;

import com.achyutha.bankingapp.auth.dto.JwtResponse;
import com.achyutha.bankingapp.auth.dto.LoginRequest;
import com.achyutha.bankingapp.auth.dto.SignupRequest;
import com.achyutha.bankingapp.auth.jwt.JwtUtils;
import com.achyutha.bankingapp.auth.jwt.UserDetailsImpl;
import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.common.Utils;
import com.achyutha.bankingapp.domain.converter.RoleConverter;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.model.UserStatus;
import com.achyutha.bankingapp.domain.service.RoleRepository;
import com.achyutha.bankingapp.domain.service.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.auth.model.RoleType.*;
import static com.achyutha.bankingapp.common.Constants.EMPLOYEE_ID_PREFIX;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final PasswordEncoder encoder;

    private final JwtUtils jwtUtils;

    private final AuthenticationManager authenticationManager;

    private final RoleConverter roleConverter;

    @Override
    public ResponseEntity<?> signUp(SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: User already present");
        }

        // Create new user's account
        var user = new User()
                .setFirstName(signUpRequest.getFirstName())
                .setLastName(signUpRequest.getLastName())
                .setDob(signUpRequest.getDob())
                .setEmail(signUpRequest.getEmail())
                .setPassword(encoder.encode(signUpRequest.getPassword()));

        var latestId = userRepository.findFirstByOrderByIdDesc();

        if (latestId.isEmpty())
            user.setEmployeeId(String.format("%s%s", EMPLOYEE_ID_PREFIX, 1));
        else
            user.setEmployeeId(String.format("%s%s", EMPLOYEE_ID_PREFIX, latestId.get().getId() + 1));

        var strRoles = signUpRequest.getRole();
        var roles = new HashSet<Role>();

        if (strRoles == null)
            roles.add(roleConverter.convert(ROLE_CUSTOMER));
        else
            strRoles.forEach(role -> roles.add(roleConverter.convert(role)));

        if(signUpRequest.getUserStatus()==null)
            user.setUserStatus(UserStatus.active);

        userRepository.save(
                user
                        .setUsername(Utils.generateEmailFromName(user.getFirstName(), user.getEmployeeId()))
                        .setRoles(roles)
        );

        return ResponseEntity.ok(String.format("User registered successfully! Your company username/email address: %s", user.getUsername()));
    }

    @Override
    public ResponseEntity<?> signIn(LoginRequest loginRequest) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        var jwt = jwtUtils.generateJwtToken(authentication);

        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse().setRoles(roles).setToken(jwt).setId(userDetails.getId()).setName(userDetails.getName()).setEmployeeId(userDetails.getEmployeeId()));
    }
}
