package com.achyutha.bankingapp.domain.model.AccountModels;

import com.achyutha.bankingapp.domain.model.enums.RepaymentTenure;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.validation.constraints.NotNull;

import static com.achyutha.bankingapp.domain.model.enums.RepaymentTenure.year1;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Entity(name = "loan_account")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LoanAccount extends Account {

    @Range(min = 100000)
    @NotNull(message = "loan.account.loan.is.null")
    private Long loanAmount;

    @NotNull(message = "loan.account.loan.repayment.is.empty")
    private Double lastRepayment = 0.0;

    @NotNull(message = "loan.account.repayment.tenure.is.empty")
    private RepaymentTenure repaymentTenure = year1;

}
