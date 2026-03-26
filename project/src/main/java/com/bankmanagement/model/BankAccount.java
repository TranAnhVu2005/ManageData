package com.bankmanagement.model;
import java.util.Date;

class BankAccount {
    private String numberAccount;
    private String userId;
    private String pinCodeHash;
    private double balance;
    private String state;
    private Date createdAt;
    
    public BankAccount() {
        numberAccount = "";
        userId = "";
        pinCodeHash = "";
        balance = 0.0;
        state = "active";
        createdAt = new Date();
    }

    public BankAccount(String numberAccount, String userId, String pinCodeHash, double balance, String state, Date createdAt) {
        this.numberAccount = numberAccount;
        this.userId = userId;
        this.pinCodeHash = pinCodeHash;
        this.balance = balance;
        this.state = state;
        this.createdAt = createdAt;
    }

    public String getNumberAccount() {
        return numberAccount;
    }

    public void setNumberAccount(String numberAccount) {
        this.numberAccount = numberAccount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPinCodeHash() {
        return pinCodeHash;
    }

    public void setPinCodeHash(String pinCodeHash) {
        this.pinCodeHash = pinCodeHash;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}