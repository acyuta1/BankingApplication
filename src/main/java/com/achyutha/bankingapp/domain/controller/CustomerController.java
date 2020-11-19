package com.achyutha.bankingapp.domain.controller;

import com.achyutha.bankingapp.auth.jwt.UserDetailsServiceImpl;
import com.achyutha.bankingapp.domain.dto.AccountRequestDto;
import com.achyutha.bankingapp.domain.dto.AmountTransaction;
import com.achyutha.bankingapp.domain.dto.TransferAmountDto;
import com.achyutha.bankingapp.domain.dto.UpdateAfterCreation;
import com.achyutha.bankingapp.domain.model.AccountModels.Account;
import com.achyutha.bankingapp.domain.model.AccountRequest;
import com.achyutha.bankingapp.domain.model.Kyc;
import com.achyutha.bankingapp.domain.model.KycVerificationStatus;
import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.service.CustomerService;
import com.achyutha.bankingapp.domain.service.ExportToPdf;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.achyutha.bankingapp.common.Constants.KYC_NOT_UPDATED;

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

    /**
     * Compares whether the current user is equal to the logged in user.
     *
     * @param user The user.
     */
    private void compareUserName(String user) {
        if (!user.equals(userDetailsService.getCurrentLoggedInUser()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "logged in user trying to alter other user.");
    }

    /**
     * To check whether a user's kyc is confirmed or not.
     *
     * @param user The user.
     */
    private void checkForKycVerification(User user) {
        compareUserName(user.getUsername());
        if (user.getKyc() == null || !user.getKyc().getKycVerificationStatus().equals(KycVerificationStatus.verified))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, KYC_NOT_UPDATED);
    }

    /**
     * To get information of the logged in customer.
     *
     * @param user The user matching id.
     * @return The User object.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public User getCustomer(@PathVariable("id") User user) {
        log.debug("Fetching user {}", user.getUsername());
        compareUserName(user.getUsername());
        return user;
    }

    /**
     * To update the users kyc information.
     *
     * @param user                user
     * @param updateAfterCreation The kyc payload.
     * @return Response with appropriate String.
     */
    @PutMapping("/{id}/kyc")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> updateKyc(@PathVariable("id") User user,
                                       @RequestBody UpdateAfterCreation updateAfterCreation) {
        compareUserName(user.getUsername());
        log.debug("Updating kyc with information provided by user.");
        return customerService.updateKyc(user, updateAfterCreation);
    }


    @PostMapping("/{id}/account/request")
    @PreAuthorize("hasRole('CUSTOMER')")
    public AccountRequest requestAccount(@PathVariable("id") User user,
                                         @RequestBody AccountRequestDto accountRequestDto) {
        checkForKycVerification(user);
        log.debug("User {} requesting for account of type {}", user.getUsername(), accountRequestDto.getAccountType());
        return customerService.requestForAccount(user, accountRequestDto);
    }

    @GetMapping("/{id}/account")
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<? extends Account> getAllAccountsOfUser(@PathVariable("id") User user) {
        checkForKycVerification(user);
        log.trace("Fetching all accounts of user: {}", user.getUsername());
        return customerService.fetchAllAccountsOfUsers(user);
    }

    @PutMapping("/{id}/account/{accountId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Account depositOrWithdraw(
            @PathVariable("id") User user,
            @PathVariable("accountId") Account account,
            @RequestBody @Valid AmountTransaction amountTransaction) {
        checkForKycVerification(user);
        log.trace("A transaction request {} on account {}", amountTransaction.getTransactionType(),
                amountTransaction.getAccountType());
        return customerService.depositOrWithdrawFromAccount(user, account, amountTransaction);
    }

    //
    @GetMapping("/{id}/account/{accountId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Account getAccount(@PathVariable("id") User user,
                              @PathVariable("accountId") Account account) {
        checkForKycVerification(user);
        log.trace("Fetching account with id {} of user {}", account.getId(), user.getUsername());
        return customerService.getAccount(user, account);
    }

    @GetMapping("/{id}/kyc/{kycId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Kyc getKyc(@PathVariable("id") User user,
                      @PathVariable("kycId") Kyc kyc) {
        checkForKycVerification(user);
        log.trace("Getting KYC of a customer with username {}.", user.getUsername());
        return customerService.getDetailsOfCustomer(user, kyc);
    }

    @PostMapping("/{id}/account/{accountId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> transferAmount(@PathVariable("id") User user,
                                            @PathVariable("accountId") Account account,
                                            @RequestBody @Valid TransferAmountDto transferAmountDto) {
        checkForKycVerification(user);
        log.trace("Transfer request from account {} to account {} by user {}",
                account.getId(), transferAmountDto.getAccountId(), user.getUsername());
        return customerService.transferAmount(user, account, transferAmountDto);
    }

    @PutMapping("/{id}/account/{accountId}/close")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> closeAccount(@PathVariable("id") User user,
                                          @PathVariable("accountId") Account account) {
        checkForKycVerification(user);
        log.trace("Attempting to close account {} by user {}", account.getId(), user.getUsername());
        return customerService.closeAccount(user, account);
    }

    @GetMapping("/{id}/account/export")
    @PreAuthorize("hasRole('CUSTOMER')")
    public void exportToPdf(@PathVariable("id") User user,
                            HttpServletResponse response) throws IOException {
        checkForKycVerification(user);
        response.setContentType("application/pdf");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=users_" + currentDateTime + ".pdf";
        response.setHeader(headerKey, headerValue);


        ExportToPdf exporter = new ExportToPdf(user);
        exporter.export(response);
    }


}
