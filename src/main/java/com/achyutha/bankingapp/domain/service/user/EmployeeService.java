package com.achyutha.bankingapp.domain.service.user;

import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.AccountModels.AccountRequest;
import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface EmployeeService {

    /**
     * Update user and set new password. And then set user status to active.
     * @param user The existing user.
     * @param updateAfterCreation Password and dob updation.
     * @return The updated user.
     */
    User updateEmployee(User user, UpdateAfterCreation updateAfterCreation);

    /**
     * To add a customer with basic details.
     * @param signUpRequest The basic detail payload.
     * @return The response.
     */
    ResponseEntity<?> addCustomer(SignUpRequest signUpRequest);

    /**
     * To approve or reject a request.
     * @param kyc The kyc.
     * @param approve true - approve, false - reject.
     * @return The response.
     */
    ResponseEntity<?> processKycRequest(Kyc kyc, Map<String, String> approve);

    /**
     * Fetch all pending kyc.
     * @return List of pending kyc.
     */
    List<Kyc> fetchAllPendingKyc();

    /**
     * To approve or reject a request.
     * @param accountRequest The account request entry.
     * @param approve true - approve, false - reject.
     * @return The response.
     */
    ResponseEntity<?> processAccRequest(AccountRequest accountRequest, Map<String, String> approve);

    /**
     * To fetch all pending account requests.
     * @return List of Account request.
     */
    List<AccountRequest> fetchAllPendingAccRequests();

    /**
     * Fetch all users with role 'CUSTOMER'.
     * @return The list of customers.
     */
    List<User> fetchAllCustomers();

    /**
     * To delete a customer.
     * @param user The employee.
     * @param customer The customer.
     * @return Response.
     */
    ResponseEntity<?> deleteCustomer(User user, User customer);
}
