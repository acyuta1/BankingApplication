package com.achyutha.bankingapp.domain.converter;

import com.achyutha.bankingapp.domain.model.AccountModels.Account;
import com.achyutha.bankingapp.domain.service.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static com.achyutha.bankingapp.common.Constants.ACCOUNT_NOT_FOUND;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountConverter implements Converter<String, Account> {

    private final AccountRepository<Account> accountRepository;

    /**
     * Fetches a user when the id is provided.
     *
     * @param id String
     * @return user.
     */
    @SneakyThrows
    @Override
    public Account convert(String id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, ACCOUNT_NOT_FOUND));
    }
}