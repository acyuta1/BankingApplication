package com.achyutha.bankingapp.domain.converter;

import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.service.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static com.achyutha.bankingapp.common.Constants.USER_NOT_FOUND;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserConverter implements Converter<String, User> {

    private final UserRepository userRepository;

    /**
     * Fetches a user when the id is provided.
     *
     * @param id String
     * @return user.
     */
    @Override
    public User convert(String id) {
        System.out.println("here");
        System.out.println(id);
        return userRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND));
    }
}
