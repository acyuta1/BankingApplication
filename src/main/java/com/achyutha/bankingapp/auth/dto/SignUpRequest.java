package com.achyutha.bankingapp.auth.dto;

import com.achyutha.bankingapp.auth.model.RoleType;
import com.achyutha.bankingapp.domain.model.UserStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * SignupRequest DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
public class SignUpRequest {

    @NotBlank(message = "first.name.empty")
    String firstName;

    @NotBlank(message = "last.name.empty")
    String lastName;

    LocalDate dob;

    String password;

    @NotEmpty(message = "email.empty")
    @Email
    String email;

    UserStatus userStatus = UserStatus.active;

    Set<RoleType> role = new HashSet<>();
}
