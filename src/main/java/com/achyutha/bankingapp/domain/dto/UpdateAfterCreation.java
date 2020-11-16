package com.achyutha.bankingapp.domain.dto;

import com.achyutha.bankingapp.common.validation.group.EmployeeLevelValidation;
import com.achyutha.bankingapp.common.validation.group.KycGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Login Request DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
public class UpdateAfterCreation {

    @NotBlank(message = "password.empty", groups = {KycGroup.class, EmployeeLevelValidation.class})
    String password;

    LocalDate dob;

    @NotBlank(message = "aadhar.number.required", groups = KycGroup.class)
    @Length(min = 12, max = 12)
    private String aadharNumber;

    @NotBlank(message = "pan.card.id.required", groups = KycGroup.class)
    @Length(min = 10, max = 10)
    private String panCard;

}
