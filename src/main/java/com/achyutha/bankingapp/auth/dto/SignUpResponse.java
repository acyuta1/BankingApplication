package com.achyutha.bankingapp.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import static com.achyutha.bankingapp.common.Constants.SIGN_IN_RESPONSE;

/**
 * JwtResponse DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SignUpResponse {

    private Long id;

    private String name;

    private String userName;

    private String tempPassword;

    @Override
    public String toString() {
        return (String.format(SIGN_IN_RESPONSE, name, id, tempPassword));
    }
}
