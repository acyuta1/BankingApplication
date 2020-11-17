package com.achyutha.bankingapp.domain.converter;

import com.achyutha.bankingapp.domain.model.TransactionType;

import javax.persistence.AttributeConverter;

public class TransactionTypeToStringConverter implements AttributeConverter<TransactionType, String> {

    @Override
    public String convertToDatabaseColumn(TransactionType transactionType) {
        return (transactionType != null) ? transactionType.toString().toLowerCase() : null;
    }

    @Override
    public TransactionType convertToEntityAttribute(String transactionType) {
        try {
            return TransactionType.valueOf(transactionType.toLowerCase());
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }
}
