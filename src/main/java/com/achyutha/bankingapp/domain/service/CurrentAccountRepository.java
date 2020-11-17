package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.domain.model.AccountModels.CurrentAccount;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface CurrentAccountRepository
        extends AccountRepository<CurrentAccount> {

    Optional<CurrentAccount> findByUser(User user);
}
