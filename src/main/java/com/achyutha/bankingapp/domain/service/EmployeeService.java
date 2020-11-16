package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.model.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

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
    ResponseEntity<?> processKycRequest(Kyc kyc, Boolean approve);

    /**
     * Fetch all pending kyc.
     * @return List of pending kyc.
     */
    List<Kyc> fetchAllPendingKyc();
}
