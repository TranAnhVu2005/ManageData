package com.bankmanagement.model;

public class Cards {
    private String cardNumber;
    private String cardPinCodeHash;
    private Date createdAt;
    private Date expireDate;
    private String secureCode;
    private String numberAccount;

    // Constructors
    public Cards() {
        cardNumber = "";
        cardPinCodeHash = "";
        createdAt = new Date();
        expireDate = new Date();
        secureCode = "";
        numberAccount = "";
    }

    public Cards(String cardNumber, String cardPinCodeHash, Date createdAt, Date expireDate, String secureCode, String numberAccount) {
        this.cardNumber = cardNumber;
        this.cardPinCodeHash = cardPinCodeHash;
        this.createdAt = createdAt;
        this.expireDate = expireDate;
        this.secureCode = secureCode;
        this.numberAccount = numberAccount;
    }

    // Getters and Setters
    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
    }
}