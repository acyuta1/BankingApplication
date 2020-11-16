package com.achyutha.bankingapp.auth.service;

import com.achyutha.bankingapp.auth.dto.JwtResponse;
import com.achyutha.bankingapp.auth.dto.LoginRequest;
import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.jwt.JwtUtils;
import com.achyutha.bankingapp.auth.jwt.UserDetailsImpl;
import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.common.Utils;
import com.achyutha.bankingapp.domain.converter.RoleConverter;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.service.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.auth.model.RoleType.ROLE_CUSTOMER;
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
    public ResponseEntity<?> signUp(SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "user already present.");

        // Create new user's account
        var user = new User()
                .setFirstName(signUpRequest.getFirstName())
                .setLastName(signUpRequest.getLastName())
                .setDob(signUpRequest.getDob())
                .setEmail(signUpRequest.getEmail())
                .setUserStatus(signUpRequest.getUserStatus())
                .setPassword(encoder.encode(signUpRequest.getPassword()));

        if (!signUpRequest.getRole().contains(ROLE_CUSTOMER)) {
            var latestId = userRepository.findFirstByOrderByIdDesc();

            if (latestId.isEmpty())
                user.setEmployeeId(String.format("%s%s", EMPLOYEE_ID_PREFIX, 1));
            else
                user.setEmployeeId(String.format("%s%s", EMPLOYEE_ID_PREFIX, latestId.get().getId() + 1));
            user.setUsername(Utils.generateEmailFromName(user.getFirstName(), user.getEmployeeId()));
        } else
            user.setUsername(user.getEmail());

        var strRoles = signUpRequest.getRole();
        var roles = new HashSet<Role>();

        if (strRoles == null)
            roles.add(roleConverter.convert(ROLE_CUSTOMER));
        else
            strRoles.forEach(role -> roles.add(roleConverter.convert(role)));

        userRepository.save(user.setRoles(roles));

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
