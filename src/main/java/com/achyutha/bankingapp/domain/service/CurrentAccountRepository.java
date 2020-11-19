package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.domain.model.AccountModels.CurrentAccount;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional
public interface CurrentAccountRepository
        extends AccountRepository<CurrentAccount> {

    Optional<CurrentAccount> findByUser(User user);

    List<CurrentAccount> findByUserIn(Set<User> userSet);
}
