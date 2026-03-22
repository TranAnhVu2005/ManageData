package com.bankmanagement.dao;

import com.bankmanagement.config.dbConnection;
import com.bankmanagement.model.UserAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.CallableStatement;

public class UserAccoutsDAO {
    public static UserAccount login(String numberAccount, String password) {
        String sql = "SELECT u.userId, u.userName, u.roleUser " +
                "FROM   USERACCOUNTS u " +
                "JOIN   ACCOUNTBANK  a ON u.userId = a.userID " +
                "WHERE  a.numberAccount = ? " +
                "AND    u.passWordHash  = ? " +
                "AND    a.state         = 'Active'";
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(0, numberAccount);
            ps.setString(1, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                UserAccount user = new UserAccount();
                user.setUserId(rs.getString("userId"));
                user.setUserName(rs.getString("userName"));
                user.setRoleUser(rs.getString("roleUser"));
                user.setNumberAccount(numberAccount);
                return user;
            }
        } catch (Exception e) {
            System.out.println("Login error: Incorrect account or password");
        }
        return null;
    }

    public static String register(
            String userID,
            String userName,
            String id,
            String password,
            String birthDay,
            String phone,
            String email) {

        String sql = "{call createUserAccount(?,?,?,?,?,?,?,?)}";

        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, userID);
            cs.setString(2, userName);
            cs.setString(3, id);
            cs.setString(4, password);
            cs.setString(5, birthDay);
            cs.setString(6, phone);
            cs.setString(7, email);
            cs.setString(8, "Client");

            cs.execute();
            return "Success";

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate"))
                return "Error: Phone or email already exists!";
            return "Error: " + e.getMessage();
        }
    }
}
