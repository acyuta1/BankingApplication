package com.achyutha.bankingapp.domain.converter;

import com.achyutha.bankingapp.domain.model.AccountRequestStatus;

import javax.persistence.AttributeConverter;

public class AccountRequestStatusToStringConverter implements AttributeConverter<AccountRequestStatus, String> {

    @Override
    public String convertToDatabaseColumn(AccountRequestStatus accountType) {
        return (accountType != null) ? accountType.toString().toLowerCase() : null;
    }

    @Override
    public AccountRequestStatus convertToEntityAttribute(String accountTypeStr) {
        try {
            return AccountRequestStatus.valueOf(accountTypeStr.toLowerCase());
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
