package com.achyutha.bankingapp.domain.model;

import com.achyutha.bankingapp.common.validation.group.AdminLevelValidation;
import com.achyutha.bankingapp.common.validation.group.CustomerLevelValidation;
import com.achyutha.bankingapp.domain.model.enums.KycVerificationStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * Kyc Table.
 */
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

    @NotBlank(message = "kyc.username.empty", groups = {CustomerLevelValidation.class})
    private String userName;

    @NotNull(message = "kyc.dob.empty", groups = {CustomerLevelValidation.class})
    private LocalDate dob;

    @NotBlank(message = "kyc.aadhar.number.required", groups = {AdminLevelValidation.class, CustomerLevelValidation.class})
    @Size(min = 12, max = 12, groups = {AdminLevelValidation.class, CustomerLevelValidation.class})
    private String aadharNumber;

    @NotBlank(message = "kyc.pan.card.id.required", groups = {AdminLevelValidation.class, CustomerLevelValidation.class})
    @Size(min = 10, max = 10, groups = {AdminLevelValidation.class, CustomerLevelValidation.class})
    private String panCard;

    @JsonIgnore
    private String newPassword;

    private KycVerificationStatus kycVerificationStatus = KycVerificationStatus.pending;
}
