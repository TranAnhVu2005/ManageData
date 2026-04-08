package com.bankmanagement.dao;

import com.bankmanagement.config.dbConnection;
import com.bankmanagement.model.BankAccount;
import com.bankmanagement.model.UserAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO trung tâm — xử lý toàn bộ truy vấn liên quan đến tài khoản người dùng và
 * tài khoản ngân hàng. Mỗi nhóm hàm được phân chia rõ ràng theo nghiệp vụ.
 *
 * Quy tắc bảo mật: Lớp này KHÔNG chứa logic kiểm tra mật khẩu/PIN bằng SQL.
 * Mọi xác thực đều đi qua BCrypt tại Java trước khi gọi xuống Stored Procedure.
 */
public class UserAccoutsDAO {

    // =========================================================================
    // NHÓM 1: XÁC THỰC (Authentication Helpers)
    // =========================================================================

    /**
     * Xác thực mật khẩu đăng nhập hệ thống của người dùng bằng BCrypt.
     * Lấy hash từ DB rồi so sánh tại Java — tránh truyền raw password xuống SQL.
     */
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
            System.out.println("[DAO] Error verifying password: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xác thực mã PIN của tài khoản ngân hàng.
     * Dùng trước khi thực hiện Chuyển tiền, Xem số dư, Đổi PIN.
     */
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
            System.out.println("[DAO] Error verifying account PIN: " + e.getMessage());
        }
        return false;
    }

    /**
     * Xác thực mã PIN của thẻ ngân hàng (bảng CARDS).
     * Dùng trước khi thực hiện Rút tiền qua thẻ.
     */
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
            System.out.println("[DAO] Error verifying card PIN: " + e.getMessage());
        }
        return false;
    }

    // =========================================================================
    // NHÓM 2: THÔNG TIN TÀI KHOẢN NGƯỜI DÙNG (User Account Info)
    // =========================================================================

    /**
     * [Task 11] Lấy toàn bộ thông tin hồ sơ của người dùng theo userID.
     * Trả về Map để View dễ dàng render mà không phụ thuộc vào Model.
     */
    public static Map<String, String> getProfile(String userID) {
        String sql = "SELECT userID, userName, ID, birthDay, numberPhone, email, roleUser " +
                     "FROM USERACCOUNTS WHERE userID = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Map<String, String> profile = new HashMap<>();
                profile.put("userID",      rs.getString("userID"));
                profile.put("userName",    rs.getString("userName"));
                profile.put("ID",          rs.getString("ID"));
                profile.put("birthDay",    rs.getString("birthDay"));
                profile.put("numberPhone", rs.getString("numberPhone"));
                profile.put("email",       rs.getString("email"));
                profile.put("roleUser",    rs.getString("roleUser"));
                return profile;
            }
        } catch (SQLException e) {
            System.out.println("[DAO] Error fetching profile: " + e.getMessage());
        }
        return null;
    }

    /**
     * [Task 2b] Cập nhật thông tin hồ sơ người dùng.
     * Truyền null cho trường nào muốn giữ nguyên — DB dùng COALESCE để bỏ qua.
     * newPasswordHash = null nghĩa là không đổi mật khẩu.
     */
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
            cs.setString(7, newPasswordHash); // null = giữ hash cũ (COALESCE trong Proc)
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

    /**
     * [Task 9] Tạo tài khoản ngân hàng mới cho người dùng.
     * Trả về mã kết quả: 0=thành công, 1=không tìm thấy user, 2=lỗi server, 3=trùng số TK.
     */
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
            System.out.println("[DAO] Error creating bank account: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Lấy danh sách tất cả tài khoản ngân hàng đang Active của một userID.
     * Kết quả được lưu trong mảng tối đa 10 phần tử (giới hạn nghiệp vụ).
     */
    public static BankAccount[] getActiveAccountByUserId(String userId) {
        String sql = "SELECT numberAccount, pinCodeHash, balance, state " +
                     "FROM ACCOUNTBANK WHERE userID = ? AND state = 'Active'";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            BankAccount[] accounts = new BankAccount[10];
            int index = 0;
            while (rs.next() && index < accounts.length) {
                BankAccount acc = new BankAccount();
                acc.setNumberAccount(rs.getString("numberAccount"));
                acc.setUserId(userId);
                acc.setPinCodeHash(rs.getString("pinCodeHash"));
                acc.setState(rs.getString("state"));
                acc.setBalance(rs.getDouble("balance"));
                accounts[index++] = acc;
            }
            return accounts;
        } catch (SQLException e) {
            System.out.println("[DAO] Error fetching accounts: " + e.getMessage());
        }
        return null;
    }

    /**
     * [Task 5] Lấy số dư tài khoản ngân hàng qua Stored Procedure.
     * PIN đã được xác thực tầng Java trước khi gọi hàm này.
     * Trả về Map gồm "balance" và "resultCode" (0=OK, 1=ko tìm thấy, 3=blocked, 4=lỗi).
     */
    public static Map<String, Object> getBalance(String accountNumber) {
        String sql = "{call checkBalance(?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, accountNumber);
            cs.registerOutParameter(2, Types.DOUBLE);
            cs.registerOutParameter(3, Types.INTEGER);
            cs.execute();

            Map<String, Object> result = new HashMap<>();
            result.put("balance",    cs.getDouble(2));
            result.put("resultCode", cs.getInt(3));
            return result;
        } catch (SQLException e) {
            System.out.println("[DAO] Error fetching balance: " + e.getMessage());
            Map<String, Object> err = new HashMap<>();
            err.put("balance", -1.0);
            err.put("resultCode", 4);
            return err;
        }
    }

    /**
     * [Task 8] Khóa tài khoản ngân hàng (chuyển state -> Blocked).
     * Proc kiểm tra: còn số dư thì từ chối; đã Blocked thì báo lỗi.
     */
    public static String deleteAccount(String numberAccount) {
        String sql = "{call deleteAccount(?,?)}";
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

    /**
     * [Task 10] Đổi mã PIN tài khoản ngân hàng.
     * PIN cũ đã được xác thực bằng verifyAccountPin() trước khi gọi hàm này.
     */
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

    /**
     * [Task 16] Mở khóa tài khoản ngân hàng đang bị Blocked.
     * Chỉ Staff mới được phép thực hiện thao tác này.
     */
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

    // =========================================================================
    // NHÓM 4: GIAO DỊCH (Transaction Operations)
    // =========================================================================

    /**
     * [Task 4] Chuyển tiền giữa 2 tài khoản trong hệ thống.
     * PIN đã được xác thực tại Java trước khi gọi — Proc chỉ lo thực thi giao dịch.
     */
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

    /**
     * [Task 6] Lấy lịch sử giao dịch của một tài khoản.
     * Proc trả về cả ResultSet lẫn OUT param — cần đọc RS trước, sau đó mới đọc OUT.
     * Trả về null nếu tài khoản không tồn tại hoặc bị khóa (dùng resultStatus để kiểm tra).
     */
    public static List<Map<String, Object>> checkTransaction(String numberAccount, String[] resultStatus) {
        String sql = "{call checkTransaction(?,?)}";
        try (Connection conn = dbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, numberAccount);
            cs.registerOutParameter(2, Types.VARCHAR);

            boolean hasResultSet = cs.execute();
            List<Map<String, Object>> rows = new ArrayList<>();

            // Duyệt qua tất cả result từ Proc (có thể có nhiều result set)
            do {
                if (hasResultSet) {
                    try (ResultSet rs = cs.getResultSet()) {
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("transactionId",      rs.getString("transactionId"));
                            row.put("created_at",          rs.getString("created_at"));
                            row.put("amount",              rs.getDouble("amount"));
                            row.put("state",               rs.getString("stateOfTransaction"));
                            row.put("type",                rs.getString("typeOfTransactionCode"));
                            row.put("numberAccount",       rs.getString("numberAccount"));
                            row.put("destinationAccount",  rs.getString("destinationAccount"));
                            rows.add(row);
                        }
                    }
                }
                hasResultSet = cs.getMoreResults();
            } while (hasResultSet || cs.getUpdateCount() != -1);

            // OUT param chỉ đọc được sau khi đã drain hết result set
            resultStatus[0] = cs.getString(2);
            return rows;
        } catch (Exception e) {
            resultStatus[0] = "Error: " + e.getMessage();
            return new ArrayList<>();
        }
    }

    /**
     * [Task 12] Lọc lịch sử giao dịch theo khoảng thời gian (từ ngày — đến ngày).
     * Format date input: "yyyy-MM-dd" hoặc "yyyy-MM-dd HH:mm:ss".
     */
    public static List<Map<String, Object>> searchTransactionByDate(
            String numberAccount, String fromDate, String toDate, String[] resultStatus) {
        String sql = "{call searchTransactionByDate(?,?,?,?)}";
        try (Connection conn = dbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, numberAccount);
            cs.setString(2, fromDate);
            cs.setString(3, toDate);
            cs.registerOutParameter(4, Types.VARCHAR);

            boolean hasResultSet = cs.execute();
            List<Map<String, Object>> rows = new ArrayList<>();

            do {
                if (hasResultSet) {
                    try (ResultSet rs = cs.getResultSet()) {
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("transactionId",      rs.getString("transactionId"));
                            row.put("created_at",          rs.getString("created_at"));
                            row.put("amount",              rs.getDouble("amount"));
                            row.put("state",               rs.getString("stateOfTransaction"));
                            row.put("type",                rs.getString("typeOfTransactionCode"));
                            row.put("numberAccount",       rs.getString("numberAccount"));
                            row.put("destinationAccount",  rs.getString("destinationAccount"));
                            rows.add(row);
                        }
                    }
                }
                hasResultSet = cs.getMoreResults();
            } while (hasResultSet || cs.getUpdateCount() != -1);

            resultStatus[0] = cs.getString(4);
            return rows;
        } catch (Exception e) {
            resultStatus[0] = "Error: " + e.getMessage();
            return new ArrayList<>();
        }
    }

    // =========================================================================
    // NHÓM 5: QUẢN TRỊ - DÀNH CHO STAFF (Admin Operations)
    // =========================================================================

    /**
     * [Task 13] Lấy danh sách toàn bộ khách hàng trong hệ thống.
     * Chỉ phục vụ tài khoản Staff — không lọc theo roleUser vì Staff đã được kiểm soát ở Controller.
     */
    public static List<Map<String, String>> getAllUsers() {
        String sql = "SELECT userID, userName, ID, birthDay, numberPhone, email, roleUser " +
                     "FROM USERACCOUNTS ORDER BY roleUser, userName";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            List<Map<String, String>> users = new ArrayList<>();
            while (rs.next()) {
                Map<String, String> u = new HashMap<>();
                u.put("userID",      rs.getString("userID"));
                u.put("userName",    rs.getString("userName"));
                u.put("ID",          rs.getString("ID"));
                u.put("birthDay",    rs.getString("birthDay"));
                u.put("numberPhone", rs.getString("numberPhone"));
                u.put("email",       rs.getString("email"));
                u.put("roleUser",    rs.getString("roleUser"));
                users.add(u);
            }
            return users;
        } catch (SQLException e) {
            System.out.println("[DAO] Error fetching all users: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * [Task 14] Tìm kiếm khách hàng theo số điện thoại hoặc CCCD (tìm gần đúng).
     * Trả về null nếu không tìm thấy (resultStatus[0] = "Not found").
     */
    public static List<Map<String, String>> searchUser(String keyword, String[] resultStatus) {
        String sql = "{call searchUser(?,?)}";
        try (Connection conn = dbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.setString(1, keyword);
            cs.registerOutParameter(2, Types.VARCHAR);

            boolean hasResultSet = cs.execute();
            List<Map<String, String>> users = new ArrayList<>();

            do {
                if (hasResultSet) {
                    try (ResultSet rs = cs.getResultSet()) {
                        while (rs.next()) {
                            Map<String, String> u = new HashMap<>();
                            u.put("userID",      rs.getString("userID"));
                            u.put("userName",    rs.getString("userName"));
                            u.put("ID",          rs.getString("ID"));
                            u.put("birthDay",    rs.getString("birthDay"));
                            u.put("numberPhone", rs.getString("numberPhone"));
                            u.put("email",       rs.getString("email"));
                            u.put("roleUser",    rs.getString("roleUser"));
                            users.add(u);
                        }
                    }
                }
                hasResultSet = cs.getMoreResults();
            } while (hasResultSet || cs.getUpdateCount() != -1);

            resultStatus[0] = cs.getString(2);
            return users;
        } catch (Exception e) {
            resultStatus[0] = "Error: " + e.getMessage();
            return new ArrayList<>();
        }
    }

    /**
     * [Task 15] Lấy toàn bộ lịch sử giao dịch hệ thống — chỉ dành cho Staff.
     */
    public static List<Map<String, Object>> getAllTransactions(String[] resultStatus) {
        String sql = "{call getAllTransactions(?)}";
        try (Connection conn = dbConnection.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {
            cs.registerOutParameter(1, Types.VARCHAR);

            boolean hasResultSet = cs.execute();
            List<Map<String, Object>> rows = new ArrayList<>();

            do {
                if (hasResultSet) {
                    try (ResultSet rs = cs.getResultSet()) {
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("transactionId",      rs.getString("transactionId"));
                            row.put("created_at",          rs.getString("created_at"));
                            row.put("amount",              rs.getDouble("amount"));
                            row.put("state",               rs.getString("stateOfTransaction"));
                            row.put("type",                rs.getString("typeOfTransactionCode"));
                            row.put("numberAccount",       rs.getString("numberAccount"));
                            row.put("destinationAccount",  rs.getString("destinationAccount"));
                            rows.add(row);
                        }
                    }
                }
                hasResultSet = cs.getMoreResults();
            } while (hasResultSet || cs.getUpdateCount() != -1);

            resultStatus[0] = cs.getString(1);
            return rows;
        } catch (Exception e) {
            resultStatus[0] = "Error: " + e.getMessage();
            return new ArrayList<>();
        }
    }

    // =========================================================================
    // PRIVATE HELPER
    // =========================================================================

    /**
     * Chuyển chuỗi rỗng "" thành null để Proc dùng COALESCE giữ nguyên giá trị cũ.
     * Tên viết tắt từ "null if empty".
     */
    private static String nvl(String value) {
        return (value != null && !value.isEmpty()) ? value : null;
    }
}
