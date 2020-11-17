package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.domain.model.AccountModels.LoanAccount;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface LoanAccountRepository
        extends AccountRepository<LoanAccount> {
}