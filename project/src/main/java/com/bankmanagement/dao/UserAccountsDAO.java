package com.bankmanagement.dao;

import com.bankmanagement.config.dbConnection;
import com.bankmanagement.model.BankAccount;
import com.bankmanagement.model.Cards;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO trung tâm — xử lý toàn bộ truy vấn liên quan đến tài khoản người dùng và
 * tài khoản ngân hàng. Mỗi nhóm hàm được phân chia rõ ràng theo nghiệp vụ.
 */
public class UserAccountsDAO {

    // =========================================================================
    // NHÓM 1: XÁC THỰC (Authentication Helpers)
    // =========================================================================

    public static boolean verifyUserPassword(String userID, String rawPassword) {
        String sql = "SELECT passWordHash FROM USERACCOUNTS WHERE userID = ?";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return org.mindrot.jbcrypt.BCrypt.checkpw(rawPassword, rs.getString("passWordHash"));
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error verifying password: " + e.getMessage());
        }
        return false;
    }

    public static boolean verifyAccountPin(String numberAccount, String rawPin) {
        String sql = "SELECT pinCodeHash FROM ACCOUNTBANK WHERE numberAccount = ?";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, numberAccount);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return org.mindrot.jbcrypt.BCrypt.checkpw(rawPin, rs.getString("pinCodeHash"));
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error verifying account PIN: " + e.getMessage());
        }
        return false;
    }

    public static boolean verifyCardPin(String cardNumber, String rawPin) {
        String sql = "SELECT cardPinCodeHash FROM CARDS WHERE cardNumber = ?";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cardNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return org.mindrot.jbcrypt.BCrypt.checkpw(rawPin, rs.getString("cardPinCodeHash"));
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error verifying card PIN: " + e.getMessage());
        }
        return false;
    }

    /** Kiểm tra xem một giá trị đã tồn tại trong cột cụ thể của bảng hay chưa (Dùng cho random IDs). */
    public static boolean existedString(String tableName, String columnName, String value) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE " + columnName + " = ?";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, value);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[DAO] Error checking existed string: " + e.getMessage());
            return true; // Giả định tồn tại để tránh trùng lặp nếu lỗi
        }
    }

    // =========================================================================
    // NHÓM 2: THÔNG TIN TÀI KHOẢN NGƯỜI DÙNG (User Account Info)
    // =========================================================================

    public static Map<String, String> getProfile(String userID) {
        String sql = "SELECT userID, userName, ID, birthDay, numberPhone, email, roleUser " +
                "FROM USERACCOUNTS WHERE userID = ?";
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, String> profile = new HashMap<>();
                profile.put("userID", rs.getString("userID"));
                profile.put("userName", rs.getString("userName"));
                profile.put("ID", rs.getString("ID"));
                profile.put("birthDay", rs.getString("birthDay"));
                profile.put("numberPhone", rs.getString("numberPhone"));
                profile.put("email", rs.getString("email"));
                profile.put("roleUser", rs.getString("roleUser"));
                return profile;
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error fetching profile: " + e.getMessage());
        }
        return null;
    }

    public static String updateInfo(String userID, String userName, String id,
            String birthDay, String numberPhone,
            String email, String newPasswordHash) {
        String sql = "{call updateInfo(?,?,?,?,?,?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, userID);
            cs.setString(2, nvl(userName));
            cs.setString(3, nvl(id));
            cs.setString(4, nvl(birthDay));
            cs.setString(5, nvl(numberPhone));
            cs.setString(6, nvl(email));
            cs.setString(7, newPasswordHash); 
            cs.registerOutParameter(8, Types.VARCHAR);
            cs.execute();
            return cs.getString(8);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // =========================================================================
    // NHÓM 3: TÀI KHOẢN NGÂN HÀNG (Bank Account Operations)
    // =========================================================================

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
            System.err.println("[DAO] Error creating bank account: " + e.getMessage());
            return -1;
        }
    }

    public static List<BankAccount> getActiveAccountByUserId(String userId) {
        String sql = "SELECT numberAccount, pinCodeHash, balance, state " +
                "FROM ACCOUNTBANK WHERE userID = ? AND state = 'Active'";
        List<BankAccount> accounts = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                BankAccount acc = new BankAccount();
                acc.setNumberAccount(rs.getString("numberAccount"));
                acc.setUserId(userId);
                acc.setPinCodeHash(rs.getString("pinCodeHash"));
                acc.setState(rs.getString("state"));
                acc.setBalance(rs.getDouble("balance"));
                accounts.add(acc);
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error fetching accounts: " + e.getMessage());
        }
        return accounts;
    }

    public static List<Cards> getCardsByUserId(String userId) {
        String sql = "SELECT c.cardNumber, c.cardPinCodeHash, c.secureCode, a.numberAccount " +
                "FROM CARDS c JOIN ACCOUNTBANK a ON c.numberAccount = a.numberAccount " +
                "WHERE a.userID = ? AND a.state = 'Active' AND c.expire_at > CURRENT_DATE";
        List<Cards> cards = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Cards card = new Cards();
                card.setCardNumber(rs.getString("cardNumber"));
                card.setCardPinCodeHash(rs.getString("cardPinCodeHash"));
                card.setSecureCode(rs.getString("secureCode"));
                card.setNumberAccount(rs.getString("numberAccount"));
                cards.add(card);
            }
        } catch (SQLException e) {
            System.err.println("[DAO] Error fetching cards: " + e.getMessage());
        }
        return cards;
    }

    public static Map<String, Object> getBalance(String accountNumber) {
        String sql = "{call checkBalance(?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, accountNumber);
            cs.registerOutParameter(2, Types.DOUBLE);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();

            Map<String, Object> result = new HashMap<>();
            result.put("balance", cs.getDouble(2));
            result.put("resultCode", cs.getInt(3));
            return result;
        } catch (SQLException e) {
            System.err.println("[DAO] Error fetching balance: " + e.getMessage());
            Map<String, Object> err = new HashMap<>();
            err.put("balance", -1.0);
            err.put("resultCode", 4);
            return err;
        }
    }

    public static String changeAccountPin(String numberAccount, String newPinHash) {
        String sql = "{call changeAccountPin(?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, numberAccount);
            cs.setString(2, newPinHash);
            cs.registerOutParameter(3, Types.VARCHAR);
            cs.execute();
            return cs.getString(3);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // =========================================================================
    // NHÓM 4: GIAO DỊCH & THẺ (Transaction & Card Operations)
    // =========================================================================

    public static int withdrawMoney(String cardNumber, double amount, String transactionId) {
        String sql = "{call withDrawMoney(?,?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, cardNumber);
            cs.setDouble(2, amount);
            cs.setString(3, transactionId);
            cs.registerOutParameter(4, Types.INTEGER);
            cs.execute();
            return cs.getInt(4);
        } catch (SQLException e) {
            System.err.println("[UserAccountsDAO] Error withdrawing: " + e.getMessage());
            return 4;
        }
    }

    public static String transferMoney(String numberAccount, String destAccount, double amount) {
        String sql = "{call transferMoney(?,?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, numberAccount);
            cs.setString(2, destAccount);
            cs.setDouble(3, amount);
            cs.registerOutParameter(4, Types.VARCHAR);
            cs.execute();
            return cs.getString(4);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    public static List<Map<String, Object>> checkTransaction(String numberAccount, String[] resultStatus) {
        String sql = "{call checkTransaction(?,?)}";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, numberAccount);
            cs.registerOutParameter(2, Types.VARCHAR);

            boolean hasResultSet = cs.execute();
            do {
                if (hasResultSet) {
                    try (ResultSet rs = cs.getResultSet()) {
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("transactionId", rs.getString("transactionId"));
                            row.put("created_at", rs.getString("created_at"));
                            row.put("amount", rs.getDouble("amount"));
                            row.put("state", rs.getString("stateOfTransaction"));
                            row.put("type", rs.getString("typeOfTransactionCode"));
                            row.put("numberAccount", rs.getString("numberAccount"));
                            row.put("destinationAccount", rs.getString("destinationAccount"));
                            rows.add(row);
                        }
                    }
                }
                hasResultSet = cs.getMoreResults();
            } while (hasResultSet || cs.getUpdateCount() != -1);

            resultStatus[0] = cs.getString(2);
        } catch (Exception e) {
            resultStatus[0] = "Error: " + e.getMessage();
        }
        return rows;
    }

    public static List<Map<String, Object>> searchTransactionByDate(
            String numberAccount, String fromDate, String toDate, String[] resultStatus) {
        String sql = "{call searchTransactionByDate(?,?,?,?)}";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, numberAccount);
            cs.setString(2, fromDate);
            cs.setString(3, toDate);
            cs.registerOutParameter(4, Types.VARCHAR);

            boolean hasResultSet = cs.execute();
            do {
                if (hasResultSet) {
                    try (ResultSet rs = cs.getResultSet()) {
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("transactionId", rs.getString("transactionId"));
                            row.put("created_at", rs.getString("created_at"));
                            row.put("amount", rs.getDouble("amount"));
                            row.put("state", rs.getString("stateOfTransaction"));
                            row.put("type", rs.getString("typeOfTransactionCode"));
                            row.put("numberAccount", rs.getString("numberAccount"));
                            row.put("destinationAccount", rs.getString("destinationAccount"));
                            rows.add(row);
                        }
                    }
                }
                hasResultSet = cs.getMoreResults();
            } while (hasResultSet || cs.getUpdateCount() != -1);

            resultStatus[0] = cs.getString(4);
        } catch (Exception e) {
            resultStatus[0] = "Error: " + e.getMessage();
        }
        return rows;
    }

    public static int createCard(String cardNumber, String numberAccount, String pin, String ccv) {
        String sql = "{call createCard(?,?,?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, cardNumber);
            cs.setString(2, numberAccount);
            String pinHash = org.mindrot.jbcrypt.BCrypt.hashpw(pin, org.mindrot.jbcrypt.BCrypt.gensalt());
            cs.setString(3, pinHash);
            cs.setString(4, ccv);
            cs.registerOutParameter(5, Types.INTEGER);
            cs.execute();
            return cs.getInt(5);
        } catch (Exception e) {
            System.err.println("[DAO] Error creating card: " + e.getMessage());
            return -1;
        }
    }
    // =========================================================================
    // PRIVATE HELPER
    // =========================================================================

    private static String nvl(String value) {
        return (value != null && !value.isEmpty()) ? value : null;
    }
}
