package com.achyutha.bankingapp.domain.model.AccountModels;

import com.achyutha.bankingapp.domain.model.AccountType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Entity(name = "savings_account")
@EntityListeners(AuditingEntityListener.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SavingsAccount extends Account{

        private Long balance = 0L;

        private Double interestAccruedLastYear = 0.0;

        private AccountType accountType = AccountType.savings;

}
