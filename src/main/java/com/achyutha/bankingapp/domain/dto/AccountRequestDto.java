package com.achyutha.bankingapp.domain.dto;

import com.achyutha.bankingapp.common.validation.group.CurrentAccountValidation;
import com.achyutha.bankingapp.common.validation.group.LoanAccountValidation;
import com.achyutha.bankingapp.common.validation.group.SavingsAccountValidation;
import com.achyutha.bankingapp.domain.converter.AccountTypeToStringConverter;
import com.achyutha.bankingapp.domain.model.enums.AccountType;
import com.achyutha.bankingapp.domain.model.enums.RepaymentTenure;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;

import javax.persistence.Convert;
import javax.validation.constraints.NotNull;

import static com.achyutha.bankingapp.domain.model.enums.RepaymentTenure.year1;

/**
 * A transaction DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
public class AccountRequestDto {

    @NotNull(groups = {CurrentAccountValidation.class, SavingsAccountValidation.class, LoanAccountValidation.class})
    @Convert(converter = AccountTypeToStringConverter.class)
    private AccountType accountType;

    @NotNull(groups = CurrentAccountValidation.class)
    private String employer;

    @Range(min = 100000)
    @NotNull(message = "account.request.loan.is.null", groups = {LoanAccountValidation.class})
    private Long loanAmount;

    @NotNull(message = "account.request.repayment.tenure.is.empty", groups = {LoanAccountValidation.class})
    private RepaymentTenure repaymentTenure = year1;

}
