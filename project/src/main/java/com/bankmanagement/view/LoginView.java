package com.bankmanagement.view;

import java.util.Scanner;

/**
 * Handles all console interactions for user authentication (Login/Register).
 */
public class LoginView {

    private final Scanner sc = new Scanner(System.in);

    public void showMainMenu() {
        System.out.println();
        System.out.println("==================================");
        System.out.println("        BANK MANAGEMENT           ");
        System.out.println("==================================");
        System.out.println("  1. Login to system");
        System.out.println("  2. Create Account (Register)");
        System.out.println("  0. Exit");
        System.out.println("==================================");
        System.out.print("Your Choice: ");
    }

    public void showSuccess(String message) {
        System.out.println("[SUCCESS] " + message);
    }

    public void showError(String message) {
        System.out.println("[ERROR] " + message);
    }

    public String[] getLoginInput() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Phone number: ");
        String account = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();
        return new String[]{account, password};
    }

    public String[] getRegisterInput() {
        System.out.println("\n--- REGISTER ACCOUNT ---");
        System.out.print("Full Name: ");
        String userName = sc.nextLine().trim();
        System.out.print("Identity Card (12 digits): ");
        String id = sc.nextLine().trim();
        System.out.print("Birth Date (yyyy-MM-dd): ");
        String birthDay = sc.nextLine().trim();
        System.out.print("Phone Number: ");
        String phone = sc.nextLine().trim();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        System.out.print("Password (min 6 chars): ");
        String password = sc.nextLine().trim();
        System.out.print("Confirm Password: ");
        String confirm = sc.nextLine().trim();

        return new String[]{userName, id, birthDay, phone, email, password, confirm};
    }

    public int getChoice() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }
}