package com.achyutha.bankingapp.common;

import java.time.LocalDate;

public class Constants {

    public static final String BLANK_SPACE = " ";
    public static final String USER_NOT_FOUND = "user.not.found";
    public static final String KYC_NOT_FOUND = "kyc.not.found";

    public static final String EMPLOYEE_EMAIL_SUFFIX = "@bankapp.com";
    public static final String EMPLOYEE_EMAIL_PATTERN = "%s.%s%s";

    public static final String EMPLOYEE_ID_PREFIX = "BA";

    public static final LocalDate DEFAULT_DATE = LocalDate.of(1996,4,28);

    public static final String KYC_NOT_UPDATED = "kyc.not.updated.or.verified";
    public static final String ACCOUNT_REQUEST_NOT_FOUND = "account.request.not.found";
    public static final String ACCOUNT_NOT_FOUND = "account.not.found";

    public static final String DEPOSIT_MESSAGE = "Deposited/repaid %s money";
    public static final String WITHDRAW_MESSAGE = "Withdrew %s money";
    public static final String LOAN_MESSAGE = "Repayment of %s successful.";
    public static final String TRANSFER_AMOUNT = "Transferred amount - %s from %s to %s (%s)";
    public static final String RECEIVE_AMOUNT = "Received amount - %s from %s (%s)";
    public static final String LOAN_AMOUNT_CREDITED = "Loan approved, credited amount - %s successfully.";



}
