package com.achyutha.bankingapp.common;

import com.achyutha.bankingapp.domain.model.AccountType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    public static final List<String> EXPORT_USER_INFO = List.of("User ID","Full Name","Total Accounts","User Status","Statement Period");
    public static final List<String> EXPORT_TRANSACTION_INFO = List.of("Transaction Date","Balance before transaction","Balance after transaction","Message");

    public static final List<String> EXPORT_BASIC_ACCOUNT_INFO = List.of("Account ID","Account Type","Account Balance");
    public static final Map<AccountType, List<String>> EXPORT_ACCOUNT_TYPE_INFO = Map.of(
            AccountType.savings, Stream.concat(EXPORT_BASIC_ACCOUNT_INFO.stream(),List.of("Interest Accrued Last Month","Transactions remaining").stream()).collect(Collectors.toList()),
            AccountType.current, Stream.concat(EXPORT_BASIC_ACCOUNT_INFO.stream(),List.of("Employer").stream()).collect(Collectors.toList()),
            AccountType.loan, Stream.concat(EXPORT_BASIC_ACCOUNT_INFO.stream(),List.of("Loan Amount","Tenure chosen","Last Repayment").stream()).collect(Collectors.toList())
    );





}
