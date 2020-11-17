package com.achyutha.bankingapp.domain.model;

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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Entity()
@Table(name = "kyc")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Kyc {

    @Id
    private String id;

    private String userName;

    private LocalDate dob;

    @NotBlank(message = "aadhar.number.required")
    @Length(min = 12, max = 12)
    private String aadharNumber;

    @NotBlank(message = "pan.card.id.required")
    @Length(min = 10, max = 10)
    private String panCard;

    @JsonIgnore
    private String newPassword;

    @NotNull
    private KycVerificationStatus kycVerificationStatus = KycVerificationStatus.pending;
}
