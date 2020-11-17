package com.achyutha.bankingapp.domain.controller;

import com.achyutha.bankingapp.auth.dto.SignUpRequest;
import com.achyutha.bankingapp.auth.jwt.UserDetailsServiceImpl;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.AccountRequest;
import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.model.UserStatus;
import com.achyutha.bankingapp.domain.service.EmployeeService;
import com.achyutha.bankingapp.domain.service.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
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

    private final UserRepository userRepository;

    /**
     * Compares whether the current user is equal to the logged in user.
     * @param user The user.
     */
    private void compareUserName(String user) {
        if (!user.equals(userDetailsService.getCurrentLoggedInUser()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "logged in user trying to alter other user.");
    }

    /**
     * To check whether an employee is active.
     *
     * @param username The user name of employee.
     */
    private void isActive(String username) {
        compareUserName(username);
        var user = userRepository.findByUsername(username);
        if (user.isEmpty() || !user.get().getUserStatus().equals(UserStatus.active))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "logged in user is not active.");
    }

    /**
     * //todo: get employee must only get employees and customers
     * To get information of an admin.
     *
     * @param user The user matching id.
     * @return The User object.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public User getEmployee(@PathVariable("id") User user) {
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
    public User updateEmployee(@PathVariable("id") User user,
                               @RequestBody UpdateAfterCreation updateAfterCreation) {
        compareUserName(user.getUsername());
        return employeeService.updateEmployee(user, updateAfterCreation);
    }

    /**
     * To add a new customer.
     *
     * @param signUpRequest The employee signupRequest object.
     * @return The response, with newly created employee username.
     */
    @PostMapping("/{id}/add")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> addCustomer(@PathVariable("id") User user,
                                         @RequestBody @Valid SignUpRequest signUpRequest) {
        isActive(user.getUsername());
        return employeeService.addCustomer(signUpRequest);
    }

    /**
     * To Look at pending kyc approval.
     *
     * @return Response entity.
     */
    @GetMapping("/{id}/kyc")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<Kyc> getAllPendingKyc(@PathVariable("id") User user) {
        isActive(user.getUsername());
        return employeeService.fetchAllPendingKyc();
    }


    /**
     * To approve or reject a Kyc.
     *
     * @param kyc The Kyc information in question.
     * @return Response entity.
     */
    @PutMapping("/{id}/kyc/{kycId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> processKyc(@PathVariable("id") User user,
                                        @PathVariable("kycId") Kyc kyc,
                                        @RequestParam("approve") Boolean approve) {
        isActive(user.getUsername());
        return employeeService.processKycRequest(kyc, approve);
    }

    @PutMapping("/{id}/account/requests/{accountRequestId}")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<?> processAccountRequest(@PathVariable("id") User user,
                                                   @PathVariable("accountRequestId") AccountRequest accountRequest,
                                                   @RequestParam("approve") Boolean approve) {
        isActive(user.getUsername());
        return employeeService.processAccRequest(accountRequest, approve);
    }

    /**
     * To Look at pending account requests.
     *
     * @return Response entity.
     */
    @GetMapping("/{id}/account/requests")
    @PreAuthorize("hasRole('EMPLOYEE')")
    public List<AccountRequest> getAllPendingAccountRequests(@PathVariable("id") User user) {
        isActive(user.getUsername());
        return employeeService.fetchAllPendingAccRequests();
    }


}
