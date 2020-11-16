package com.achyutha.bankingapp.domain.model;

import com.achyutha.bankingapp.domain.converter.AccountTypeToStringConverter;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity()
@Table(name = "account_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class AccountRequest {

    @Id
    private String id;

    @NotEmpty
    @Convert(converter = AccountTypeToStringConverter.class)
    private AccountType accountType;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id")
    @JsonProperty(access = JsonProperty.Access.READ_WRITE)
    @JsonBackReference
    private User user;

}
