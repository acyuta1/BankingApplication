package com.achyutha.bankingapp.auth.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * JwtResponse DTO.
 */
@Getter
@Setter
@Accessors(chain = true)
public class JwtResponse {
    String token;
    Long id;
    String employeeId;
    String name;
    List<String> roles;
}
