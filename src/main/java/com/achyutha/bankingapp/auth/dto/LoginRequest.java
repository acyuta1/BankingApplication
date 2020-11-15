package com.achyutha.bankingapp.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Login Request DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
public class LoginRequest {

    String username;
    String password;
}
