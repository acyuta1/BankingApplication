package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.domain.model.AccountModels.SavingsAccount;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SavingsAccountRepository
        extends AccountRepository<SavingsAccount> {
}
