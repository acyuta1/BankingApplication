package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.domain.model.AccountModels.Account;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository<T extends Account>
        extends
        JpaRepository<T , String>
{
    List<? extends Account> findAllByUser(User user);
}