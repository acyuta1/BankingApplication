package com.achyutha.bankingapp.domain.dto;

import com.achyutha.bankingapp.common.validation.group.CustomerLevelValidation;
import com.achyutha.bankingapp.common.validation.group.EmployeeLevelValidation;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Login Request DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
public class UpdateAfterCreation {

    @NotBlank(message = "password.empty", groups = {CustomerLevelValidation.class, EmployeeLevelValidation.class})
    String password;

    @NotNull(message = "dob.is.null", groups = {CustomerLevelValidation.class, EmployeeLevelValidation.class})
    LocalDate dob;

    @NotBlank(message = "aadhar.number.required", groups = CustomerLevelValidation.class)
    @Length(min = 12, max = 12)
    private String aadharNumber;

    @NotBlank(message = "pan.card.id.required", groups = CustomerLevelValidation.class)
    @Length(min = 10, max = 10)
    private String panCard;
}
