package com.bankmanagement;

import com.bankmanagement.controller.AuthController;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Lớp khởi chạy chính của Ứng dụng Quản lý Ngân hàng.
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Thiết lập Encoding UTF-8 để hiển thị chính xác các ký tự Tiếng Việt trên
            // console
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8.name()));
        } catch (Exception e) {
            System.err.println("Warning: Cannot set UTF-8 for console.");
        }

        // Khởi tạo controller phân luồng quá trình đăng nhập/đăng ký
        new AuthController().start();
    }
}