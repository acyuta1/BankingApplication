package com.achyutha.bankingapp.domain.model.AccountModels;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Entity(name = "savings_account")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SavingsAccount extends Account {

    @NotNull(message = "interest.accrued.cannot.be.null")
    private Double interestAccruedLastMonth = 0.0;

    @NotNull(message = "transaction.limit.null")
    private Integer transactionsRemaining;

}
