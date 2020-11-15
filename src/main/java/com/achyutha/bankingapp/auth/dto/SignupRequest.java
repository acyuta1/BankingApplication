package com.achyutha.bankingapp.auth.dto;

import com.achyutha.bankingapp.auth.model.RoleType;
import com.achyutha.bankingapp.domain.model.UserStatus;
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

    String firstName;
    String lastName;
    LocalDate dob;
    String password;
    String email;
    UserStatus userStatus;
    Set<RoleType> role = new HashSet<>();
}
