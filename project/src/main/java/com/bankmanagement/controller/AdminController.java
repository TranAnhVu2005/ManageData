package com.bankmanagement.controller;

import com.bankmanagement.model.UserAccount;

/**
 * Controller điều hướng và xử lý các nghiệp vụ dành riêng cho Nhân viên/Quản trị viên (Staff).
 */
public class AdminController {

    private UserAccount currentUser;

    public AdminController(UserAccount user) {
        this.currentUser = user;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n+----------------------------------+");
            System.out.printf("|  Admin: %-25s|\n",
                    currentUser.getUserName());
            System.out.println("+----------------------------------+");
            System.out.println("|  1. Create bank account          |");
            System.out.println("|  2. Deposit money                |");
            System.out.println("|  3. Delete account               |");
            System.out.println("|  0. Logout                       |");
            System.out.println("+----------------------------------+");
            System.out.print("Choose: ");

            int choice = readInt();
            switch (choice) {
                case 1 -> System.out.println("[Create Account - Task 1]");
                case 2 -> System.out.println("[Deposit Money - Task 7]");
                case 3 -> System.out.println("[Delete Account - Task 8]");
                case 0 -> {
                    System.out.println("Logged out!");
                    return;
                }
                default -> System.out.println("! Invalid choice!");
            }
        }
    }

    private int readInt() {
        try {
            return Integer.parseInt(
                    new java.util.Scanner(System.in).nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}