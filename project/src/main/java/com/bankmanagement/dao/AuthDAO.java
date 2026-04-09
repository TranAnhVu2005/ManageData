package com.bankmanagement.dao;

import com.bankmanagement.config.dbConnection;
import com.bankmanagement.model.UserAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.CallableStatement;

/**
 * Data Access Object phụ trách luồng nghiệp vụ Xác thực Tài Khoản.
 */
public class AuthDAO {

    /**
     * Xác thực thông tin đăng nhập của người dùng.
     * 
     * @param phone    Số điện thoại đăng nhập.
     * @param password Mật khẩu đăng nhập.
     * @return Đối tượng UserAccount nếu thành công, null nếu thất bại.
     */
    public static UserAccount login(String phone, String password) {
        // Lấy đủ thông tin cần thiết cho phiên làm việc: userId, tên, phân quyền,
        // số điện thoại (để tạo tài khoản NH theo SĐT) và số TK đang active đầu tiên.
        String sql = "SELECT u.userId, u.userName, u.roleUser, u.numberPhone, u.passWordHash, a.numberAccount " +
                "FROM   USERACCOUNTS u " +
                "LEFT JOIN ACCOUNTBANK a ON u.userId = a.userID AND a.state = 'Active' " +
                "WHERE  u.numberPhone = ?";
        try {
            Connection connection = dbConnection.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("passWordHash");
                if (org.mindrot.jbcrypt.BCrypt.checkpw(password, storedHash)) {
                    UserAccount user = new UserAccount();
                    user.setUserId(rs.getString("userId"));
                    user.setUserName(rs.getString("userName"));
                    user.setRoleUser(rs.getString("roleUser"));
                    user.setNumberPhone(rs.getString("numberPhone"));
                    user.setNumberAccount(rs.getString("numberAccount"));
                    return user;
                }
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Khởi tạo tài khoản hệ thống mới.
     * Gọi thủ tục (Stored Procedure) createUserAccount từ DB.
     * 
     * @return "Success" nếu tạo mới thành công, hoặc thông báo lỗi cụ thể nếu xảy
     *         ra Exception.
     */
    public static String register(
            String userID,
            String userName,
            String id,
            String password,
            String birthDay,
            String phone,
            String email) {

        String sql = "{call createUserAccount(?,?,?,?,?,?,?,?,?)}";

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
            cs.registerOutParameter(9, java.sql.Types.VARCHAR);

            cs.execute();
            String result = cs.getString(9);
            if (result != null && result.equalsIgnoreCase("Success")) {
                return "Success";
            } else {
                return "Error: " + (result != null ? result : "Unknown database error");
            }

        } catch (SQLException e) {
            if (e.getMessage().contains("Duplicate"))
                return "Error: Phone or email already exists!";
            return "Error: " + e.getMessage();
        }
    }
}
