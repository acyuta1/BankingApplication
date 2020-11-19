package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.common.BankApplicationProperties;
import com.achyutha.bankingapp.domain.model.AccountModels.SavingsAccount;
import com.achyutha.bankingapp.domain.model.AccountStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.common.AccountUtils.constructTransaction;
import static com.achyutha.bankingapp.common.AccountUtils.setTransactionValues;

@Slf4j
@Component
@Service
@RequiredArgsConstructor
public class BankAppScheduledJobs {

    private final SavingsAccountRepository savingsAccountRepository;

    private final BankApplicationProperties bankApplicationProperties;

    /**
     * Every day at 6pm, interest of savings account will be calculated.
     */
    @Scheduled(cron = "0 0 18 * * *")
    public void updateInterest() {
        log.info("Executing Schedule job - Calculating Interest.");
        var savingsAccounts = savingsAccountRepository.findAllByAccountStatus(AccountStatus.active);
        savingsAccounts = savingsAccounts.stream()
                .map(account -> {
                    if (account.getBalance() < 1)
                        return account;
                    else {
                        return account.setInterestAccruedLastMonth(account.getBalance() * ((bankApplicationProperties.getSavingsAccountInterestRate() / 100) / 365));
                    }
                }).collect(Collectors.toList());
        final Calendar c = Calendar.getInstance();
        if (c.get(Calendar.DATE) == c.getActualMaximum(Calendar.DATE)) {
            savingsAccounts = savingsAccounts.stream().map(savingsAccount -> {
                        var transaction = constructTransaction(savingsAccount);
                        savingsAccount.setBalance(savingsAccount.getBalance() + savingsAccount.getInterestAccruedLastMonth());
                        return (SavingsAccount) savingsAccount.setInterestAccruedLastMonth(0.0)
                                .setTransactions(setTransactionValues(transaction, savingsAccount, String.format("Interest Accrued for month %s", c.get(Calendar.MONTH))));
                    }
            ).collect(Collectors.toList());
        }

        savingsAccountRepository.saveAll(savingsAccounts);
        log.info("Executed Schedule job.");
    }
}
