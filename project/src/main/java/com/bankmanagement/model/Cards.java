package com.bankmanagement.model;
import java.util.Date;

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

    public String getCardPinCodeHash() {
        return cardPinCodeHash;
    }

    public void setCardPinCodeHash(String cardPinCodeHash) {
        this.cardPinCodeHash = cardPinCodeHash;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public String getNumberAccount() {
        return numberAccount;
    }

    public void setNumberAccount(String numberAccount) {
        this.numberAccount = numberAccount;
    }
}