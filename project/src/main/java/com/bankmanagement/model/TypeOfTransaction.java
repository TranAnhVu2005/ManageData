package com.bankmanagement.model;

class TypeOfTransaction {
    private String typeOfTransactionCode;
    private String nameTypeOfTransaction;
    private String description;

    public TypeOfTransaction() {
        typeOfTransactionCode = "";
        nameTypeOfTransaction = "";
        description = "";
    }

    public TypeOfTransaction(String typeOfTransactionCode, String nameTypeOfTransaction, String description) {
        this.typeOfTransactionCode = typeOfTransactionCode;
        this.nameTypeOfTransaction = nameTypeOfTransaction;
        this.description = description;
    }

    public String getTypeOfTransactionCode() {
        return typeOfTransactionCode;
    }

    public void setTypeOfTransactionCode(String typeOfTransactionCode) {
        this.typeOfTransactionCode = typeOfTransactionCode;
    }

    public String getNameTypeOfTransaction() {
        return nameTypeOfTransaction;
    }

    public void setNameTypeOfTransaction(String nameTypeOfTransaction) {
        this.nameTypeOfTransaction = nameTypeOfTransaction;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}