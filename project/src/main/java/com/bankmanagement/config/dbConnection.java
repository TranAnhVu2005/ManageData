package com.bankmanagement.config;

import java.sql.Connection;
import java.sql.DriverManager;

public class dbConnection {
    public static Connection getConnection() {
        String databaseName = "MANAGEBANKACCOUNT";
        String userName = "root";
        String passWord = "";
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost/" + databaseName,
                    userName,
                    passWord);
            System.out.println("Noi ket thanh cong");

        } catch (Exception ex) {
            System.out.println("Noi ket khong thanh cong");
            ex.printStackTrace();
        }
        return conn;
    }

  
}