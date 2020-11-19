package com.achyutha.bankingapp.domain.model;

import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.common.validation.group.AdminLevelValidation;
import com.achyutha.bankingapp.domain.converter.UserStatusToStringConverter;
import com.achyutha.bankingapp.domain.model.AccountModels.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity()
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Email
    private String username;

    @NotBlank(groups = AdminLevelValidation.class)
    private String firstName;

    @NotBlank(groups = AdminLevelValidation.class)
    private String lastName;

    private LocalDate dob;

    private String employeeId;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(max = 120)
    @JsonIgnore
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @NotNull
    @Convert(converter = UserStatusToStringConverter.class)
    private UserStatus userStatus = UserStatus.active;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "kyc_id", referencedColumnName = "id")
    private Kyc kyc;

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Account> accounts = new HashSet<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade = CascadeType.ALL)
    private Set<AccountRequest> accountRequests = new HashSet<>();
}
