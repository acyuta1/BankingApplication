package com.achyutha.bankingapp.common;

import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.jwt.UserDetailsServiceImpl;
import com.achyutha.bankingapp.auth.model.RoleType;
import com.achyutha.bankingapp.domain.model.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.common.Constants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class Utils {

    private final UserDetailsServiceImpl userDetailsService;

    private final Validator validator;

    /**
     * To generate an email for the newly registered employee, with first-name and employeeId (unique).
     *
     * @param firstName  The first name.
     * @param employeeId The employee id.
     * @return The email id.
     */
    public static String generateEmailFromName(String firstName, String employeeId) {
        if (firstName.split(BLANK_SPACE).length > 1)
            firstName = firstName.split(BLANK_SPACE)[0];
        return String.format(EMPLOYEE_EMAIL_PATTERN, firstName.toLowerCase(), employeeId.toLowerCase(), EMPLOYEE_EMAIL_SUFFIX);
    }

    /**
     * A temporary password, generated from the UUID's first 12 alphabetic + numeric chars.
     *
     * @return The temporary password.
     */
    public static String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(12);
    }

    /**
     * Initializes default values -- to password, dob.
     *
     * @return The updated sign up request object.
     */
    public static SignUpRequest defaultInit(SignUpRequest signupRequest, RoleType roleType) {
        return signupRequest
                .setPassword(generateTemporaryPassword())
                .setUserStatus(UserStatus.initial)
                .setDob(DEFAULT_DATE)
                .setRole(Set.of(roleType));
    }

    /**
     * To validate an object provided with validation group.
     * @param object The object being validated.
     * @param classes The classes.
     */
    public void checkForErrors(Object object, Class<?>... classes) {
        var errors = validator.validate(object, classes);
        if (errors.size() > 0) {
            log.trace("Found error while validating account request dto");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, String.format("errors: %s",
                    errors.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(" "))));
        }
    }


}
