package com.bankmanagement.dao;

import com.bankmanagement.config.dbConnection;
import com.bankmanagement.model.UserAccount;
import com.bankmanagement.model.BankAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.CallableStatement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

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

    //  Hàm lấy số dư tài khoản dựa trên số tài khoản, sử dụng thủ tục lưu trữ trong CSDL để đảm bảo lấy được số dư mới nhất sau mỗi giao dịch
    public static Map<String, Object> getBalance(String accountNumber, String pinCodeHash) {
        String sql = "{call checkBalance(?,?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, accountNumber);
            cs.setString(2, pinCodeHash);
            cs.registerOutParameter(3, Types.DOUBLE);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();

            double balance = cs.getDouble(3);
            int resultCode = cs.getInt(4);
            Map<String, Object> result = new HashMap<>();
            result.put("balance", balance);
            result.put("resultCode", resultCode);

            return result;
        } catch (SQLException e) {
            System.out.println("Error fetching balance: " + e.getMessage());
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("balance", -1);
            errorResult.put("resultCode", -1);
            return errorResult;
        }
    }

    // Tạo tài khoản ngân hàng mới cho người dùng, trả về mã lỗi cụ thể để hiển thị thông báo phù hợp
    public static int createBankAccount(String accountNumber, String pinCodeHash, String userId) {
        String sql = "{call createBankAccount(?,?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {

            cs.setString(1, accountNumber);
            cs.setString(2, pinCodeHash);
            cs.setString(3, userId);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();

            return cs.getInt(4);

        } catch (SQLException e) {
            System.out.println("Error creating bank account: " + e.getMessage());
            return -1;
        }
    }

    public static BankAccount[] getActiveAccountByUserId(String userId) {
        String sql = "SELECT numberAccount, pinCodeHash, balance,state " +
             "FROM ACCOUNTBANK WHERE userID = ? AND state = 'Active'";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            BankAccount[] accounts = new BankAccount[10]; // Giả sử mỗi người dùng có tối đa 10 tài khoản
            int index = 0;
            while (rs.next() && index < accounts.length) {
                BankAccount account = new BankAccount();
                account.setNumberAccount(rs.getString("numberAccount"));
                account.setUserId(userId);
                account.setPinCodeHash(rs.getString("pinCodeHash"));
                account.setState(rs.getString("state"));
                account.setBalance(rs.getDouble("balance"));
                accounts[index++] = account;
            }
            return accounts;

        } catch (SQLException e) {
            System.out.println("Error fetching active account: " + e.getMessage());
        }
        return null;
    }
}
