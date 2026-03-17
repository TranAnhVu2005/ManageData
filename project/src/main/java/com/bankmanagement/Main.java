package com.bankmanagement;

import java.sql.Connection;

import com.bankmanagement.config.dbConnection;

public class Main {
    public static void main(String[] args) {
        dbConnection a = new dbConnection();
        Connection connect = a.getConnection();
    }
}
