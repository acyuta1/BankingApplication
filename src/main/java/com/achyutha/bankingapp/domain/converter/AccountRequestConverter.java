package com.achyutha.bankingapp.domain.converter;

import com.achyutha.bankingapp.domain.model.AccountRequest;
import com.achyutha.bankingapp.domain.service.AccountRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static com.achyutha.bankingapp.common.Constants.ACCOUNT_REQUEST_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountRequestConverter implements Converter<String, AccountRequest> {

    private final AccountRequestRepository accountRequestRepository;

    /**
     * Fetches a user when the id is provided.
     *
     * @param id String
     * @return user.
     */
    @Override
    public AccountRequest convert(String id) {
        return accountRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ACCOUNT_REQUEST_NOT_FOUND));
    }
}

