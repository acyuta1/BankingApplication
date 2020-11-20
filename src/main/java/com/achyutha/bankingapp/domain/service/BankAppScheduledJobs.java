package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.common.BankApplicationProperties;
import com.achyutha.bankingapp.common.Utils;
import com.achyutha.bankingapp.domain.model.AccountModels.SavingsAccount;
import com.achyutha.bankingapp.domain.model.enums.AccountStatus;
import com.achyutha.bankingapp.domain.service.account.LoanAccountRepository;
import com.achyutha.bankingapp.domain.service.account.SavingsAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.common.AccountUtils.constructTransaction;
import static com.achyutha.bankingapp.common.AccountUtils.setTransactionValues;
import static com.achyutha.bankingapp.common.Constants.BANK_APP_EMAIL_LOAN_SUBJECT;
import static com.achyutha.bankingapp.common.Constants.BANK_APP_EMAIL_LOAN_TEXT;

/**
 * BankApp Scheduled jobs.
 */
@Slf4j
@Component
@Service
@RequiredArgsConstructor
public class BankAppScheduledJobs {

    private final SavingsAccountRepository savingsAccountRepository;

    private final LoanAccountRepository loanAccountRepository;

    private final BankApplicationProperties bankApplicationProperties;

    private final Utils utils;

    /**
     * Every day at 6pm, interest of savings account will be calculated.
     */
    @Scheduled(cron = "0 0 18 * * *")
    public void updateInterest() {
        log.info("Executing Schedule job - Calculating Interest.");

        // Fetching all savings accounts whose status is active.
        var savingsAccounts = savingsAccountRepository.findAllByAccountStatus(AccountStatus.active);
        savingsAccounts = savingsAccounts.stream()
                .map(account -> {
                    // Interest will be calculated every day and stored in InterestAccruedLastMonth field.
                    if (account.getBalance() < 1)
                        return account;
                    else {
                        return account.setInterestAccruedLastMonth(account.getInterestAccruedLastMonth() +
                                (account.getBalance() * ((bankApplicationProperties.getSavingsAccountInterestRate() / 100) / 365)));
                    }
                }).collect(Collectors.toList());
        final Calendar c = Calendar.getInstance();
        // On the last day of every month, we will add the interest from InterestAccruedLastMonth to the actual balance.
        if (c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {
            savingsAccounts = savingsAccounts.stream().map(savingsAccount -> {
                        var transaction = constructTransaction(savingsAccount);
                        savingsAccount.setTransactionsRemaining(bankApplicationProperties.getTransactionLimitSavings())
                                .setBalance(savingsAccount.getBalance() + savingsAccount.getInterestAccruedLastMonth());
                        return (SavingsAccount) savingsAccount.setInterestAccruedLastMonth(0.0)
                                .setTransactions(setTransactionValues(transaction, savingsAccount, String.format("Interest Accrued for month %s", c.get(Calendar.MONTH))));
                    }
            ).collect(Collectors.toList());

            // Sending reminder  mails to users having active loan accounts.
            loanAccountRepository.findAllByAccountStatus(AccountStatus.active)
                    .forEach(loanAccount -> {
                        var user = loanAccount.getUser();
                        utils.sendEmail(user.getEmail(), BANK_APP_EMAIL_LOAN_SUBJECT, String.format(BANK_APP_EMAIL_LOAN_TEXT,
                                String.format("%s %s", user.getFirstName(), user.getLastName())));
                    });
        }

        savingsAccountRepository.saveAll(savingsAccounts);
        log.info("Executed Schedule job.");
    }
}
