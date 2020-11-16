package com.achyutha.bankingapp.auth.service;

import com.achyutha.bankingapp.auth.dto.LoginRequest;
import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<?> signUp(SignUpRequest signUpRequest);

    ResponseEntity<?> signIn(LoginRequest loginRequest);
}
