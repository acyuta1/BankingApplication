package com.achyutha.bankingapp.domain.service.account;

import com.achyutha.bankingapp.domain.model.AccountModels.Account;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Account Repository.
 * @param <T> Can be either Savings, Current or Loan Account.
 */
public interface AccountRepository<T extends Account>
        extends
        JpaRepository<T , String>
{
    /**
     * Fetches all accounts of a user.
     * @param user The user.
     * @return List of accounts.
     */
    List<? extends Account> findAllByUser(User user);
}