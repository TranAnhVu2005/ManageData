package com.bankmanagement;

import com.bankmanagement.controller.AuthController;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Lớp khởi chạy chính của Ứng dụng Quản lý Ngân hàng.
 *
 * Lưu ý chạy đúng trên Windows: Terminal (cmd/PowerShell) cần được set codepage UTF-8.
 * Chạy trước khi start ứng dụng: chcp 65001
 * Hoặc thêm VM options khi chạy qua Maven: -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8
 */
public class Main {
    public static void main(String[] args) {
        // Buộc JVM dùng UTF-8 cho cả stdout lẫn stderr — cần thiết trên Windows
        // vì mặc định Windows dùng CP1252 (Latin-1) nên ký tự tiếng Việt thành "?"
        System.setProperty("stdout.encoding", "UTF-8");
        System.setProperty("stderr.encoding", "UTF-8");
        try {
            System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        } catch (Exception e) {
            // Không crash nếu terminal không hỗ trợ — chỉ cảnh báo
            System.err.println("Warning: Cannot set UTF-8 output.");
        }

        // Khởi tạo controller phân luồng quá trình đăng nhập/đăng ký
        new AuthController().start();
    }
}