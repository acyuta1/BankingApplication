package com.achyutha.bankingapp.common;

import com.achyutha.bankingapp.domain.model.AccountModels.Account;
import com.achyutha.bankingapp.domain.model.AccountModels.Transaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class AccountUtils {

    /**
     * Returns a new transaction.
     *
     * @param account The account.
     * @return The transaction.
     */
    public static Transaction constructTransaction(Account account) {
        log.trace("Constructing transaction.");
        return new Transaction()
                .setAccount(account)
                .setTransactionDate(LocalDateTime.now())
                .setId(UUID.randomUUID().toString())
                .setBalancePriorTransaction(account.getBalance());
    }

    /**
     * Sets the transaction details post transaction confirmation.
     *
     * @param transaction The transaction object.
     * @param account     The account.
     * @param message     The message.
     * @return Set of transactions including the new one.
     */
    public static Set<Transaction> setTransactionValues(Transaction transaction, Account account, String message) {
        log.trace("Adding new transaction to existing ones.");

        // Add message only if it was not set previously.
        if (transaction.getMessage()==null || message != null)
            transaction.setMessage(message);
        transaction.setBalanceAfterTransaction(account.getBalance());
        var existingTransactions = account.getTransactions();
        existingTransactions.add(transaction);
        return existingTransactions;
    }
}
