package com.achyutha.bankingapp.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

import static com.achyutha.bankingapp.common.Constants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class Utils {

    static Random random = new SecureRandom();

    /**
     * To generate an email for the newly registered employee, with first-name and employeeId (unique).
     * @param firstName The first name.
     * @param employeeId The employee id.
     * @return The email id.
     */
    public static String generateEmailFromName(String firstName, String employeeId) {
        if (firstName.split(BLANK_SPACE).length > 1)
            firstName = firstName.split(BLANK_SPACE)[0].toLowerCase();
        return String.format(EMPLOYEE_EMAIL_PATTERN, firstName, employeeId.toLowerCase(), EMPLOYEE_EMAIL_SUFFIX);
    }
}
