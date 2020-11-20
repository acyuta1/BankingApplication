package com.achyutha.bankingapp.domain.service.account;

import com.achyutha.bankingapp.domain.model.AccountModels.LoanAccount;
import com.achyutha.bankingapp.domain.model.enums.AccountStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface LoanAccountRepository
        extends AccountRepository<LoanAccount> {

    /**
     * Fetch all loan accounts whose account status is provided.
     * @return The list of savings accounts.
     */
    List<LoanAccount> findAllByAccountStatus(AccountStatus accountStatus);
}