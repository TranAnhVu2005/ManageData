// package com.bankmanagement.config;

// import java.sql.Connection;
// import java.sql.DriverManager;

// public class dbConnection {
//     public static Connection getConnection() {
//         String databaseName = "MANAGEBANKACCOUNT";
//         String userName = "root";
//         String passWord = "";
//         Connection conn = null;
//         try {
//             Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
//             conn = DriverManager.getConnection(
//                     "jdbc:mysql://localhost/" + databaseName,
//                     userName,
//                     passWord);
//             System.out.println("Noi ket thanh cong");

//         } catch (Exception ex) {
//             System.out.println("Noi ket khong thanh cong");
//             ex.printStackTrace();
//         }
//         return conn;
//     }
// }

package com.bankmanagement.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class dbConnection {

    private static HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();

            config.setJdbcUrl("jdbc:mysql://localhost:3306/MANAGEBANKACCOUNT");
            config.setUsername("root");
            config.setPassword("");

            // cấu hình pool
            config.setMaximumPoolSize(10);   // tối đa 10 connection
            config.setMinimumIdle(2);        // giữ sẵn 2 connection
            config.setIdleTimeout(30000);    // 30s
            config.setMaxLifetime(1800000);  // 30 phút
            config.setConnectionTimeout(30000); // 30s

            dataSource = new HikariDataSource(config);

            System.out.println("HikariCP initialized successfully");

        } catch (Exception e) {
            System.out.println("Failed to initialize HikariCP");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}