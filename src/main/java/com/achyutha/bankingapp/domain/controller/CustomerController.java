package com.achyutha.bankingapp.domain.controller;

import com.achyutha.bankingapp.auth.jwt.UserDetailsServiceImpl;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;

/**
 * Customer Controller.
 */
@RestController
@SpringBootApplication
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;

    private final UserDetailsServiceImpl userDetailsService;

    private void compareUserName(String user){
        if(!user.equals(userDetailsService.getCurrentLoggedInUser()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "logged in user trying to alter other user.");
    }

    /**
     * To get information of the logged in customer.
     * @param user The user matching id.
     * @return The User object.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public User getCustomer(@PathVariable("id") User user)
    {
        compareUserName(user.getUsername());
        return user;
    }

    /**
     * To update the users kyc information.
     * @param updateAfterCreation The kyc payload.
     * @return Response with appropriate String.
     */
    @PutMapping("/{id}/kyc")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateKyc(@PathVariable("id") User user,
                                       @RequestBody UpdateAfterCreation updateAfterCreation) {
        compareUserName(user.getUsername());
        return customerService.updateKyc(user, updateAfterCreation);
    }

//    public ResponseEntity<?> requestAccount(){
//
//    }
}
