package com.achyutha.bankingapp.domain.model;

public enum  RepaymentTenure {
    month3(3.50), month6(4.00), year1(5.5), year2(6.00);


    public Double getInterestRate() {
        return interestRate;
    }

    Double interestRate;
    RepaymentTenure(Double interestRate){
        this.interestRate = interestRate;
    }
}
