package com.achyutha.bankingapp.domain.converter;

import com.achyutha.bankingapp.auth.model.Role;
import com.achyutha.bankingapp.auth.model.RoleType;
import com.achyutha.bankingapp.domain.service.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static com.achyutha.bankingapp.common.Constants.USER_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class RoleConverter implements Converter<RoleType, Role> {

    private final RoleRepository roleRepository;

    /**
     * Fetches a user when the id is provided.
     *
     * @param roleType String
     * @return user.
     */
    @Override
    public Role convert(RoleType roleType) {
        System.out.println("here");
        System.out.println(roleType);
        return roleRepository.findByName(roleType)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }
}
