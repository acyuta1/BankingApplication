package com.achyutha.bankingapp.domain.model.AccountModels;

import com.achyutha.bankingapp.common.validation.group.CurrentAccountValidation;
import com.achyutha.bankingapp.common.validation.group.LoanAccountValidation;
import com.achyutha.bankingapp.common.validation.group.SavingsAccountValidation;
import com.achyutha.bankingapp.domain.converter.AccountRequestStatusToStringConverter;
import com.achyutha.bankingapp.domain.converter.AccountTypeToStringConverter;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.model.enums.AccountRequestStatus;
import com.achyutha.bankingapp.domain.model.enums.AccountType;
import com.achyutha.bankingapp.domain.model.enums.RepaymentTenure;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import static com.achyutha.bankingapp.domain.model.enums.RepaymentTenure.year1;

/**
 * Account Request Table.
 */
@Entity()
@Table(name = "account_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccountRequest {

    @Id
    private String id;

    @NotNull(groups = {CurrentAccountValidation.class, SavingsAccountValidation.class, LoanAccountValidation.class})
    @Convert(converter = AccountTypeToStringConverter.class)
    private AccountType accountType;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id")
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @JsonBackReference
    private User user;

    @NotNull(groups = {CurrentAccountValidation.class, SavingsAccountValidation.class})
    @Convert(converter = AccountRequestStatusToStringConverter.class)
    private AccountRequestStatus accountRequestStatus = AccountRequestStatus.submitted;

    @NotNull(groups = CurrentAccountValidation.class)
    private String employer;

    @NotNull(message = "loan.is.null", groups = {LoanAccountValidation.class})
    private Long loanAmount;

    @NotNull(message = "repayment.tenure.is.empty", groups = {LoanAccountValidation.class})
    private RepaymentTenure repaymentTenure = year1;

}
