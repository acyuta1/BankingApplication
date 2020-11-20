package com.achyutha.bankingapp.domain.model.enums;

/**
 * Repayment Tenure.
 */
public enum RepaymentTenure {

    month3(3.50), month6(4.00), year1(5.5), year2(6.00);

    Double interestRate;

    RepaymentTenure(Double interestRate) {
        this.interestRate = interestRate;
    }

    public Double getInterestRate() {
        return interestRate;
    }
}
