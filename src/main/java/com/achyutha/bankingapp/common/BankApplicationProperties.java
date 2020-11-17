package com.achyutha.bankingapp.common;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Properties defined in application.yaml
 */
@Value
@ConstructorBinding
@ConfigurationProperties(prefix = "bank-application-properties")
public class BankApplicationProperties {
    String jwtSecret;
    Long jwtExpirationMs;
    Integer minBalanceSavings;
    Integer transactionLimitSavings;
    Integer maxWithdrawLimit;
}
