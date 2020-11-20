package com.achyutha.bankingapp.domain.dto;

import com.achyutha.bankingapp.domain.converter.AccountTypeToStringConverter;
import com.achyutha.bankingapp.domain.model.enums.AccountType;
import com.achyutha.bankingapp.domain.model.enums.TransactionType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Convert;
import javax.validation.constraints.NotNull;

/**
 * A transaction DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
public class AmountTransaction {

    @NotNull(message = "account.type.null")
    @Convert(converter = AccountTypeToStringConverter.class)
    private AccountType accountType;

    @NotNull(message = "transaction.type.null")
    private TransactionType transactionType = TransactionType.deposit;

    private Double amount;
}