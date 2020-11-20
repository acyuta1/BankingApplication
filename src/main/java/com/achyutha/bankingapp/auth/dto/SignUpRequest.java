package com.achyutha.bankingapp.auth.dto;

import com.achyutha.bankingapp.auth.model.RoleType;
import com.achyutha.bankingapp.common.validation.group.AdminLevelValidation;
import com.achyutha.bankingapp.common.validation.group.CustomerLevelValidation;
import com.achyutha.bankingapp.common.validation.group.EmployeeLevelValidation;
import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.model.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * SignUpRequest DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {

    @NotBlank(message = "first.name.empty", groups = {AdminLevelValidation.class, CustomerLevelValidation.class, EmployeeLevelValidation.class})
    private String firstName;

    @NotBlank(message = "last.name.empty", groups = {AdminLevelValidation.class, CustomerLevelValidation.class, EmployeeLevelValidation.class})
    private String lastName;

    @NotNull(message = "date.of.birth.is.empty", groups = {AdminLevelValidation.class})
    private LocalDate dob;

    @NotBlank(message = "password.is.empty", groups = {AdminLevelValidation.class})
    @Size(min = 7, max = 120, groups = {AdminLevelValidation.class})
    private String password;

    @NotEmpty(message = "email.empty", groups = {AdminLevelValidation.class, CustomerLevelValidation.class})
    @Email(message = "invalid.email")
    private String email;

    private UserStatus userStatus = UserStatus.active;

    @NotNull(message = "role.empty", groups = {AdminLevelValidation.class})
    private Set<RoleType> role = new HashSet<>();

    @NotNull(message = "kyc.empty", groups = {AdminLevelValidation.class, EmployeeLevelValidation.class})
    private Kyc kyc;

}
