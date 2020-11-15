package com.achyutha.bankingapp.auth.jwt;

import com.achyutha.bankingapp.domain.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * User Details Impl.
 */
@Getter
@Setter
@Accessors(chain = true)
public class UserDetailsImpl implements UserDetails {

    private Long id;

    private String name;

    private String username;

    @JsonIgnore
    private String password;

    private String employeeId;

    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Returns the user details object, which can then be used as a authentication principle.
     * @param user The user object.
     * @return The userDetailsImpl object with fields filled from the user and also the authorities.
     */
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl()
                .setId(user.getId())
                .setName(String.format("%s %s",user.getFirstName(),user.getLastName()))
                .setUsername(user.getUsername())
                .setAuthorities(authorities)
                .setPassword(user.getPassword())
                .setEmployeeId(user.getEmployeeId());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}