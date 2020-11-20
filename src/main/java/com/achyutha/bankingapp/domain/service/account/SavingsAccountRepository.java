package com.achyutha.bankingapp.domain.service.account;

import com.achyutha.bankingapp.domain.model.AccountModels.SavingsAccount;
import com.achyutha.bankingapp.domain.model.enums.AccountStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface SavingsAccountRepository
        extends AccountRepository<SavingsAccount> {

    /**
     * Fetch all savings accounts whose account status is provided.
     * @return The list of savings accounts.
     */
    List<SavingsAccount> findAllByAccountStatus(AccountStatus accountStatus);
}
