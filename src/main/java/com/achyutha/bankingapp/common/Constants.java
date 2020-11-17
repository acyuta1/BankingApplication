package com.achyutha.bankingapp.common;

import com.achyutha.bankingapp.domain.model.AccountModels.Account;
import com.achyutha.bankingapp.domain.model.AccountModels.SavingsAccount;
import com.achyutha.bankingapp.domain.model.AccountType;

import java.time.LocalDate;
import java.util.Map;

public class Constants {

    public static String BLANK_SPACE = " ";
    public static String USER_NOT_FOUND = "user.not.found";
    public static String KYC_NOT_FOUND = "kyc.not.found";

    public static String EMPLOYEE_EMAIL_SUFFIX = "@bankapp.com";
    public static String EMPLOYEE_EMAIL_PATTERN = "%s.%s%s";

    public static String EMPLOYEE_ID_PREFIX = "BA";

    public static LocalDate DEFAULT_DATE = LocalDate.of(1996,4,28);

    public static String KYC_NOT_UPDATED = "kyc.not.updated.or.verified";
    public static String ACCOUNT_REQUEST_NOT_FOUND = "account.request.not.found";
    public static String ACCOUNT_NOT_FOUND = "account.not.found";

    public static Map<AccountType, Class<? extends Account>> TYPE_MAP =
            Map.of(
                    AccountType.savings, SavingsAccount.class);
}
