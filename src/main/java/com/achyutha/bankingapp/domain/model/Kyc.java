package com.achyutha.bankingapp.domain.model;

import com.achyutha.bankingapp.common.validation.group.AdminLevelValidation;
import com.achyutha.bankingapp.common.validation.group.CustomerLevelValidation;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity
@Table(name = "kyc", uniqueConstraints = {@UniqueConstraint(columnNames = {"aadharNumber", "panCard"})})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Kyc {

    @Id
    private String id;

    @NotBlank(message = "username.empty", groups = {CustomerLevelValidation.class})
    private String userName;

    @NotNull(message = "dob.empty", groups = {CustomerLevelValidation.class})
    private LocalDate dob;

    @NotBlank(message = "aadhar.number.required", groups = {AdminLevelValidation.class, CustomerLevelValidation.class})
    @Length(min = 12, max = 12)
    private String aadharNumber;

    @NotBlank(message = "pan.card.id.required", groups = {AdminLevelValidation.class, CustomerLevelValidation.class})
    @Length(min = 10, max = 10)
    private String panCard;

    @JsonIgnore
    private String newPassword;

    private KycVerificationStatus kycVerificationStatus = KycVerificationStatus.pending;
}
