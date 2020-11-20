package com.achyutha.bankingapp.domain.service.account;

import com.achyutha.bankingapp.domain.model.AccountModels.CurrentAccount;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface CurrentAccountRepository
        extends AccountRepository<CurrentAccount> {

    /**
     * Returns a current account.
     * @param user The user.
     * @return A current account object, if present.
     */
    Optional<CurrentAccount> findByUser(User user);
}
