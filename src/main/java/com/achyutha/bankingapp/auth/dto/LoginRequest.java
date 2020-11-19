package com.achyutha.bankingapp.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;

/**
 * Login Request DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
public class LoginRequest {

    @NotBlank(message = "username.is.empty")
    String username;

    @NotBlank(message = "password.is.empty")
    String password;
}
