package com.achyutha.bankingapp.domain.dto;

import com.achyutha.bankingapp.common.validation.group.CustomerLevelValidation;
import com.achyutha.bankingapp.common.validation.group.EmployeeLevelValidation;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Login Request DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
public class UpdateAfterCreation {

    @NotBlank(message = "update.password.empty", groups = {CustomerLevelValidation.class, EmployeeLevelValidation.class})
    @Size(min = 7, max = 120, groups = {CustomerLevelValidation.class, EmployeeLevelValidation.class})
    String password;

    @NotNull(message = "update.dob.is.null", groups = {CustomerLevelValidation.class, EmployeeLevelValidation.class})
    LocalDate dob;

    @NotBlank(message = "update.aadhar.number.required", groups = CustomerLevelValidation.class)
    @Size(min = 12, max = 12)
    private String aadharNumber;

    @NotBlank(message = "update.pan.card.id.required", groups = CustomerLevelValidation.class)
    @Size(min = 10, max = 10)
    private String panCard;
}
