package com.bankmanagement.view;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Handles all console interactions for user authentication (Login/Register).
 */
public class LoginView {

    // Sử dụng bộ mã UTF-8 để hỗ trợ nhận diện Tiếng Việt từ bàn phím
    private Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);

    /**
     * Hiển thị danh mục thao tác chính của hệ thống.
     */
    public void showMainMenu() {
        System.out.println();
        System.out.println("==================================");
        System.out.println("        BANK MANAGEMENT           ");
        System.out.println("==================================");
        System.out.println("  1. Login");
        System.out.println("  2. Register new account");
        System.out.println("  0. Exit");
        System.out.println("==================================");
        System.out.print("Choose: ");
    }

    public void showSuccess(String message) {
        System.out.println("[Success] " + message);
    }

    public void showError(String message) {
        System.out.println("[Error] " + message);
    }

    private String readPassword(String prompt) {
        if (System.console() != null) {
            char[] pwd = System.console().readPassword(prompt);
            return pwd != null ? new String(pwd).trim() : "";
        } else {
            System.out.print(prompt);
            return sc.nextLine().trim();
        }
    }

    public String[] getLoginInput() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Phone number   : ");
        String account = sc.nextLine().trim();
        String password = readPassword("Password       : ");
        return new String[] { account, password };
    }

    /**
     * Lấy các thông tin cần thiết từ người dùng để bắt đầu quy trình đăng ký 1 tài khoản mới.
     * @return Mảng dữ liệu lần lượt: Tên, CCCD, Ngày Sinh, SĐT, Email, Password, ConfirmPassword
     */
    public String[] getRegisterInput() {
        System.out.println("\n--- REGISTER ACCOUNT ---");

        System.out.print("Full Name              : ");
        String userName = sc.nextLine().trim();

        System.out.print("Identity Card (12 num) : ");
        String id = sc.nextLine().trim();

        System.out.print("Date of Birth (y-m-d)  : ");
        String birthDay = sc.nextLine().trim();

        System.out.print("Phone Number (10 num)  : ");
        String phone = sc.nextLine().trim();

        System.out.print("Email                  : ");
        String email = sc.nextLine().trim();

        String password = readPassword("Password               : ");

        String confirm = readPassword("Confirm Password       : ");

        return new String[] {
                userName, id, birthDay, phone, email, password, confirm
        };
    }

    public int getChoice() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}