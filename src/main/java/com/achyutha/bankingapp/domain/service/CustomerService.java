package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.domain.dto.AccountRequestDto;
import com.achyutha.bankingapp.domain.dto.AmountTransaction;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.AccountModels.Account;
import com.achyutha.bankingapp.domain.model.AccountRequest;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CustomerService {

    /**
     * To update kyc of a user.
     * @param user The user who is submitting kyc.
     * @param updateAfterCreation The payload which contains necessary fields.
     * @return The response entity.
     */
    ResponseEntity<?> updateKyc(User user, UpdateAfterCreation updateAfterCreation);

    /**
     * To request for an account.
     * @param user The user.
     * @param accountRequestDto The account request details.
     * @return The Account Request.
     */
    AccountRequest requestForAccount(User user, AccountRequestDto accountRequestDto);

    /**
     * To deposit or withdraw from an account.
     * @param user The user.
     * @param account The account.
     * @param amountTransaction The transaction information.
     * @return The account.
     */
    Account depositOrWithdrawFromAccount(User user, Account account, AmountTransaction amountTransaction);

    /**
     * Fetches all accounts of the logged in customer.
     * @param user The user.
     * @return The List of accounts.
     */
    List<? extends Account> fetchAllAccountsOfUsers(User user);
}
