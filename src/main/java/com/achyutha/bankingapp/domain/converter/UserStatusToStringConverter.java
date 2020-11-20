package com.achyutha.bankingapp.domain.converter;

import com.achyutha.bankingapp.domain.model.enums.UserStatus;

import javax.persistence.AttributeConverter;

public class UserStatusToStringConverter implements AttributeConverter<UserStatus, String> {

    @Override
    public String convertToDatabaseColumn(UserStatus attribute) {
        return (attribute != null) ? attribute.toString().toLowerCase() : null;
    }

    @Override
    public UserStatus convertToEntityAttribute(String dbData) {
        try {
            return UserStatus.valueOf(dbData.toLowerCase());
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
