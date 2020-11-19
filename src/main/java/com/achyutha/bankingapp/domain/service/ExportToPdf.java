package com.achyutha.bankingapp.domain.service;

import com.achyutha.bankingapp.domain.model.AccountModels.*;
import com.achyutha.bankingapp.domain.model.AccountType;
import com.achyutha.bankingapp.domain.model.User;
import com.lowagie.text.Font;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.achyutha.bankingapp.common.Constants.*;


@Slf4j
@RequiredArgsConstructor
@AllArgsConstructor
public class ExportToPdf {

    private User user;

    /**
     * Constructs/Adds headers from the list values.
     * @param table Target table.
     * @param headers The list consisting of header names.
     */
    private void constructHeaderFromList(PdfPTable table, List<String> headers) {

        log.trace("Adding headers");
        // Adding new cell.
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(Color.BLUE);
        cell.setPadding(5);

        // Font configuration.
        Font font = FontFactory.getFont(FontFactory.HELVETICA);
        font.setColor(Color.WHITE);

        // Iterating through the list and adding headers.
        for(String header: headers) {
            cell.setPhrase(new Phrase(header, font));
            table.addCell(cell);
        }
    }

    /**
     * Account table - Savings, Current or Loan.
     * @param table The target table.
     * @param account The account.
     */
    private void writeAccountTableData(PdfPTable table, Account account) {
        // Setting common to all 3 kinds (fields) first.
        table.addCell(String.valueOf(account.getId()));
        table.addCell(account.getAccountType().toString());
        table.addCell(String.valueOf(account.getBalance()));

        log.trace("Account type found is {}",account.getAccountType());
        if(account.getAccountType().equals(AccountType.savings)){
            table.addCell(String.valueOf(((SavingsAccount)account).getInterestAccruedLastMonth()));
            table.addCell(String.valueOf(((SavingsAccount)account).getTransactionsRemaining()));
        }
        else if(account.getAccountType().equals(AccountType.current)){
            table.addCell(String.valueOf(((CurrentAccount)account).getEmployer()));
        }
        else if(account.getAccountType().equals(AccountType.loan)){
            table.addCell(String.valueOf(((LoanAccount)account).getLoanAmount()));
            table.addCell(String.valueOf(((LoanAccount)account).getRepaymentTenure()));
            table.addCell(String.valueOf(((LoanAccount)account).getLastRepayment()));
        }
    }

    private void writeTransactionTableData(PdfPTable table, List<Transaction> transactionList) {
        // Iterating through the transactions and setting fields.
        log.trace("Adding transaction table items");
        for (Transaction transaction : transactionList) {
            table.addCell(String.valueOf(transaction.getTransactionDate()));
            table.addCell(transaction.getBalancePriorTransaction().toString());
            table.addCell(transaction.getBalanceAfterTransaction().toString());
            table.addCell(transaction.getMessage());
        }
    }

    public void export(HttpServletResponse response) throws DocumentException, IOException {
        // New document.
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Font for the main title.
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(18);
        font.setColor(Color.BLUE);

        // Main title.
        Paragraph p = new Paragraph("Banking Application", font);
        p.setAlignment(Paragraph.ALIGN_CENTER);
        document.add(p);

        // Adding user information, towards the right of the page.
        font.setColor(Color.BLACK);
        font.setSize(10);
        p = new Paragraph(String.format(
                "User ID - %s\nFull Name - %s\nTotal Accounts - %s\nUser Status - %s\nStatement Period - %s",
                user.getUsername(), String.format("%s %s",user.getFirstName(),user.getLastName()), user.getAccounts().size(), user.getUserStatus(), LocalDate.now()), font);
        p.setAlignment(Paragraph.ALIGN_RIGHT);
        document.add(p);

        PdfPTable table = new PdfPTable(5);
        var accounts = user.getAccounts();

        if(accounts.size()==0){
            log.trace("No accounts found.");
            p = new Paragraph("No Accounts Found", font);
            p.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(p);
        }
        else {
            log.trace("Adding account table and corresponding transactions.");
            for(Account account: accounts) {

                font.setSize(10);
                font.setColor(Color.blue);
                p = new Paragraph(String.format("%s Account", account.getAccountType()), font);
                p.setAlignment(Paragraph.ALIGN_LEFT);
                document.add(p);

                var headers = EXPORT_ACCOUNT_TYPE_INFO.get(account.getAccountType());
                table = new PdfPTable(headers.size());
                table.setWidthPercentage(100f);
                table.setSpacingBefore(10);

                constructHeaderFromList(table, headers);
                writeAccountTableData(table,account);
                document.add(table);

                var transactions = account.getTransactions();

                p = new Paragraph(String.format("Total Transactions - %s", transactions.size()), font);
                p.setAlignment(Paragraph.ALIGN_LEFT);
                document.add(p);
                font.setSize(8);
                headers = EXPORT_TRANSACTION_INFO;
                table = new PdfPTable(headers.size());
                table.setWidthPercentage(100f);
                table.setSpacingBefore(10);
                constructHeaderFromList(table, headers);
                writeTransactionTableData(table,transactions.stream().sorted(Comparator.comparing(Transaction::getTransactionDate)).collect(Collectors.toList()));
                document.add(table);
            }
        }
        document.close();

    }

}
