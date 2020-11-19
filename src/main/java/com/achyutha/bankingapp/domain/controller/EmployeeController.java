package com.achyutha.bankingapp.domain.controller;

import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.jwt.UserDetailsServiceImpl;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.AccountRequest;
import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.model.UserStatus;
import com.achyutha.bankingapp.domain.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Employee Controller.
 */
@RestController
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Compares whether the current user is equal to the logged in user.
     *
     * @param user The user.
     */
    private void compareUserName(String user) {
        if (!user.equals(userDetailsService.getCurrentLoggedInUser())) {
            log.error("Logged in user does not match with requesting user.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "logged in user trying to alter other user.");
        }
    }

    /**
     * To check whether an employee is active.
     *
     * @param user The user/employee.
     */
    private void isActive(User user) {
        log.debug("Comparing username against logged in username");
        compareUserName(user.getUsername());
        // Checking if the user is active.
        if (!user.getUserStatus().equals(UserStatus.active)) {
            log.error("Employee is either inactive or does not exist.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "logged in user is not active.");
        }
    }

    /**
     * To get information of an employee.
     *
     * @param user The user matching id.
     * @return The User object.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public User getEmployee(@PathVariable("id") User user) {
        isActive(user);
        log.debug("Fetched user: {}", user.getUsername());
        return user;
    }

    /**
     * Update default password and dob, to make the employee status active.
     *
     * @param user                The user matching id.
     * @param updateAfterCreation dto with password and dob.
     * @return Updated user.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public User mandatoryUpdate(@PathVariable("id") User user,
                                @RequestBody UpdateAfterCreation updateAfterCreation) {
        compareUserName(user.getUsername());
        log.debug("Updating employee: {}", user.getUsername());
        return employeeService.updateEmployee(user, updateAfterCreation);
    }

    /**
     * To fetch all customers.
     *
     * @param user The user.
     * @return List of customers.
     */
    @GetMapping("/{id}/customers")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<User> getAllCustomers(@PathVariable("id") User user) {
        isActive(user);
        log.debug("Fetching all Customers.");
        return employeeService.fetchAllCustomers();
    }

    /**
     * To add a new customer.
     *
     * @param signUpRequest The employee signupRequest object.
     * @return The response, with newly created employee username.
     */
    @PostMapping("/{id}/customers/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> addCustomer(@PathVariable("id") User user,
                                         @RequestBody SignUpRequest signUpRequest) {
        isActive(user);
        log.debug("Adding new customer with username {}", user.getUsername());
        return employeeService.addCustomer(signUpRequest);
    }

    /**
     * To Look at pending kyc approval.
     *
     * @return Response entity.
     */
    @GetMapping("/{id}/customers/kyc")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<Kyc> getAllPendingKyc(@PathVariable("id") User user) {
        isActive(user);
        log.debug("Fetching all pending kyc approvals.");
        return employeeService.fetchAllPendingKyc();
    }


    /**
     * To approve or reject a Kyc.
     *
     * @param kyc The Kyc information in question.
     * @return Response entity.
     */
    @PutMapping("/{id}/customers/kyc/{kycId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> processKyc(@PathVariable("id") User user,
                                        @PathVariable("kycId") Kyc kyc,
                                        @RequestParam("approve") Boolean approve) {
        isActive(user);
        log.debug("Processing a kyc request.");
        return employeeService.processKycRequest(kyc, approve);
    }

    @PutMapping("/{id}/customers/account/requests/{accountRequestId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> processAccountRequest(@PathVariable("id") User user,
                                                   @PathVariable("accountRequestId") AccountRequest accountRequest,
                                                   @RequestParam("approve") Boolean approve) {
        isActive(user);
        log.debug("Processing an account request.");
        return employeeService.processAccRequest(accountRequest, approve);
    }

    /**
     * To Look at pending account requests.
     *
     * @return Response entity.
     */
    @GetMapping("/{id}/customers/account/requests")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<AccountRequest> getAllPendingAccountRequests(@PathVariable("id") User user) {
        isActive(user);
        log.debug("Fetching all pending account requests approvals.");
        return employeeService.fetchAllPendingAccRequests();
    }

    /**
     * To delete a customer.
     * @param user The employee.
     * @param customer The customer.
     * @return The response.
     */
    @DeleteMapping("/{id}/customers/{userId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> deleteCustomer(@PathVariable("id") User user,
                                           @PathVariable("userId") User customer) {
        isActive(user);
        log.debug("Fetching all pending account requests approvals.");
        return employeeService.deleteCustomer(user, customer);
    }

}
