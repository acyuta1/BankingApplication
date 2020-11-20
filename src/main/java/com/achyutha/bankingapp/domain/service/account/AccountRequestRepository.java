package com.achyutha.bankingapp.domain.service.account;

import com.achyutha.bankingapp.domain.model.AccountModels.AccountRequest;
import com.achyutha.bankingapp.domain.model.enums.AccountRequestStatus;
import com.achyutha.bankingapp.domain.model.enums.AccountType;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Account Request Repository.
 */
@Repository
public interface AccountRequestRepository extends JpaRepository<AccountRequest, String> {

    /**
     * To fetch a list of account requests with status provided, to be viewed by the employee.
     * @param accountRequestStatus The account request status.
     * @return The list of account requests.
     */
    List<AccountRequest> findAllByAccountRequestStatus(AccountRequestStatus accountRequestStatus);

    /**
     * To see if a user already has accounts present.
     * @param user The user.
     * @param accountType The account type.
     * @param accountRequestStatus The account request status.
     * @return The list of account requests.
     */
    List<AccountRequest> findAllByUserAndAccountTypeAndAccountRequestStatus(User user, AccountType accountType, AccountRequestStatus accountRequestStatus);
}