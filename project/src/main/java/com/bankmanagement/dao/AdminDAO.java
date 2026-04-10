package com.bankmanagement.dao;

import com.bankmanagement.config.dbConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object chuyên biệt cho các nghiệp vụ Quản trị/Nhân viên
 * (Staff/Admin).
 * Giúp tách biệt logic quản lý hệ thống khỏi các nghiệp vụ ngân hàng của khách
 * hàng.
 */
public class AdminDAO {

    /** Đăng tiền mặt vào tài khoản khách hàng (Nghiệp vụ do nhân viên thực hiện). */
    public static int depositMoney(String staffId, String userAccount, String staffAccount, String transactionId, double amount) {
        String sql = "{call depositMoney(?,?,?,?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, staffId);
            cs.setString(2, userAccount);
            cs.setString(3, staffAccount);
            cs.setString(4, transactionId);
            cs.setDouble(5, amount);
            cs.registerOutParameter(6, Types.INTEGER);
            cs.execute();
            return cs.getInt(6);
        } catch (SQLException e) {
            System.err.println("[AdminDAO] Error depositing money: " + e.getMessage());
            return 2;
        }
    }

    /** Khóa tài khoản ngân hàng của khách hàng. */
    public static String blockAccount(String numberAccount) {
        String sql = "{call blockAccount(?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, numberAccount);
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            return cs.getString(2);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /** Mở khóa tài khoản ngân hàng của khách hàng. */
    public static String unblockAccount(String numberAccount) {
        String sql = "{call unblockAccount(?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, numberAccount);
            cs.registerOutParameter(2, Types.VARCHAR);
            cs.execute();
            return cs.getString(2);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /** Lấy danh sách toàn bộ người dùng trong hệ thống. */
    public static List<Map<String, String>> getAllUsers() {
        String sql = "SELECT userID, userName, ID, birthDay, numberPhone, email, roleUser " +
                "FROM USERACCOUNTS ORDER BY roleUser, userName";
        List<Map<String, String>> users = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> u = new HashMap<>();
                u.put("userID", rs.getString("userID"));
                u.put("userName", rs.getString("userName"));
                u.put("ID", rs.getString("ID"));
                u.put("birthDay", rs.getString("birthDay"));
                u.put("numberPhone", rs.getString("numberPhone"));
                u.put("email", rs.getString("email"));
                u.put("roleUser", rs.getString("roleUser"));
                users.add(u);
            }
        } catch (SQLException e) {
            System.err.println("[AdminDAO] Error fetching all users: " + e.getMessage());
        }
        return users;
    }

    /** Tìm kiếm người dùng theo số điện thoại hoặc ID (Staff search). */
    public static List<Map<String, String>> searchUser(String keyword, String[] resultStatus) {
        String sql = "{call searchUser(?,?)}";
        List<Map<String, String>> users = new ArrayList<>();
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, keyword);
            cs.registerOutParameter(2, Types.VARCHAR);

            boolean hasResultSet = cs.execute();
            do {
                if (hasResultSet) {
                    try (ResultSet rs = cs.getResultSet()) {
                        while (rs.next()) {
                            Map<String, String> u = new HashMap<>();
                            u.put("userID", rs.getString("userID"));
                            u.put("userName", rs.getString("userName"));
                            u.put("ID", rs.getString("ID"));
                            u.put("birthDay", rs.getString("birthDay"));
                            u.put("numberPhone", rs.getString("numberPhone"));
                            u.put("email", rs.getString("email"));
                            u.put("roleUser", rs.getString("roleUser"));
                            u.put("totalAccounts", rs.getString("totalAccounts"));
                            u.put("accountList", rs.getString("accountList"));
                            users.add(u);
                        }
                    }
                }
                hasResultSet = cs.getMoreResults();
            } while (hasResultSet || cs.getUpdateCount() != -1);

            resultStatus[0] = cs.getString(2);
        } catch (Exception e) {
            resultStatus[0] = "Error: " + e.getMessage();
        }
        return users;
    }

    /** Xem lịch sử thay đổi thông tin của một người dùng bất kỳ. */
    /** Xem lịch sử thay đổi thông tin bằng SĐT hoặc CCCD. */
    public static List<Map<String, Object>> viewAuditLogs(String keyword, String[] status) {
        List<Map<String, Object>> logs = new ArrayList<>();
        String sql = "{call viewAuditLogs(?, ?)}";

        try (Connection conn = dbConnection.getConnection();
                CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, keyword); // Truyền SĐT hoặc CCCD
            stmt.registerOutParameter(2, Types.VARCHAR);

            boolean hasResultSet = stmt.execute();

            // XẢ TOÀN BỘ RESULT SET TRƯỚC (Giống hàm search và checkTransaction)
            do {
                if (hasResultSet) {
                    try (ResultSet rs = stmt.getResultSet()) {
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("logId", rs.getInt("logId"));
                            row.put("actionType", rs.getString("actionType"));
                            row.put("oldValue", rs.getString("oldValue"));
                            row.put("newValue", rs.getString("newValue"));
                            row.put("changedAt", rs.getString("changedAt"));
                            logs.add(row);
                        }
                    }
                }
                hasResultSet = stmt.getMoreResults();
            } while (hasResultSet || stmt.getUpdateCount() != -1);

            // ĐỌC BIẾN OUT SAU KHI ĐÃ XỬ LÝ XONG RESULT SET
            status[0] = stmt.getString(2);

        } catch (SQLException e) {
            status[0] = "Error: " + e.getMessage();
        }
        return logs;
    }

    /** Lấy dữ liệu thống kê tổng quan của toàn hệ thống. */
    public static Map<String, String> getSystemStatistics() {
        String sql = "{call getSystemStatistics(?,?,?)}";
        Map<String, String> stats = new HashMap<>();
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.registerOutParameter(2, Types.INTEGER);
            cs.registerOutParameter(3, Types.DOUBLE);
            cs.execute();
            stats.put("totalUsers", String.valueOf(cs.getInt(1)));
            stats.put("totalAccounts", String.valueOf(cs.getInt(2)));
            stats.put("totalBalance", String.format("%,.2f", cs.getDouble(3)));
        } catch (SQLException e) {
            System.err.println("[AdminDAO] Error fetching system statistics: " + e.getMessage());
        }
        return stats;
    }

    /** Ghi log hành động thực hiện bởi Staff. */
    public static void logStaffAction(String staffID, String actionType, String targetInfo) {
        String sql = "{call logStaffAction(?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
                CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, staffID);
            cs.setString(2, actionType);
            cs.setString(3, (targetInfo != null && !targetInfo.isEmpty()) ? targetInfo : null);
            cs.execute();
        } catch (SQLException e) {
            System.err.println("[AdminDAO] Error logging staff action: " + e.getMessage());
        }
    }
}
