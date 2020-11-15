package com.achyutha.bankingapp.auth.dto;

import com.achyutha.bankingapp.auth.model.RoleType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * SignupRequest DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
public class SignupRequest {

    String username;
    String fullName;
    LocalDate dob;
    String password;
    String email;
    Set<RoleType> role = new HashSet<>();
}
