package com.achyutha.bankingapp.domain.converter;

import com.achyutha.bankingapp.domain.model.enums.AccountType;

import javax.persistence.AttributeConverter;

public class AccountTypeToStringConverter implements AttributeConverter<AccountType, String> {

    @Override
    public String convertToDatabaseColumn(AccountType accountType) {
        return (accountType != null) ? accountType.toString().toLowerCase() : null;
    }

    @Override
    public AccountType convertToEntityAttribute(String accountTypeStr) {
        try {
            return AccountType.valueOf(accountTypeStr.toLowerCase());
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
