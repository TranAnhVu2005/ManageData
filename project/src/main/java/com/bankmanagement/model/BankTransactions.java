package com.bankmanagement.model;

class BankTransactions {
    private String transactionId;
    private Date createdAt;
    private double amount;
    private String stateOfTransaction;
    private String typeOfTransactionCode;
    private String numberAccount;
    private String destinationAccount;


    public BankTransactions() {
        transactionId = "";
        createdAt = new Date();
        amount = 0.0;
        stateOfTransaction = "";
        typeOfTransactionCode = "";
        numberAccount = "";
        destinationAccount = "";
    }

    public BankTransactions(String transactionId, Date createdAt, double amount, String stateOfTransaction, String typeOfTransactionCode, String numberAccount, String destinationAccount) {
        this.transactionId = transactionId;
        this.createdAt = createdAt;
        this.amount = amount;
        this.stateOfTransaction = stateOfTransaction;
        this.typeOfTransactionCode = typeOfTransactionCode;
        this.numberAccount = numberAccount;
        this.destinationAccount = destinationAccount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getStateOfTransaction() {
        return stateOfTransaction;
    }

    public void setStateOfTransaction(String stateOfTransaction) {
        this.stateOfTransaction = stateOfTransaction;
    }

    public String getNumberAccount() {
        return numberAccount;
    }

    public void setNumberAccount(String numberAccount) {
        this.numberAccount = numberAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }


    public String getTypeOfTransactionCode() {
        return typeOfTransactionCode;
    }

    public void setTypeOfTransactionCode(String typeOfTransactionCode) {
        this.typeOfTransactionCode = typeOfTransactionCode;
    }
}