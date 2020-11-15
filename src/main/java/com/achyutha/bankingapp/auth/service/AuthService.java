package com.achyutha.bankingapp.auth.service;

import com.achyutha.bankingapp.auth.dto.LoginRequest;
import com.achyutha.bankingapp.auth.dto.SignupRequest;
import org.springframework.http.ResponseEntity;

public interface AuthService {

    ResponseEntity<?> signUp(SignupRequest signUpRequest);

    ResponseEntity<?> signIn(LoginRequest loginRequest);
}
