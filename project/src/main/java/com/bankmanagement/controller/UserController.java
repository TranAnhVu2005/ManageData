package com.bankmanagement.controller;

import com.bankmanagement.model.UserAccount;

/**
 * Controller xử lý các tính năng dành cho người dùng thông thường (Client).
 */
public class UserController {

    private UserAccount currentUser;

    public UserController(UserAccount user) {
        this.currentUser = user;
    }

    public void showMenu() {
        while (true) {
            System.out.println("\n+----------------------------------+");
            System.out.printf("|  Hello: %-25s|\n",
                    currentUser.getUserName());
            System.out.printf("|  ACC: %-27s|\n",
                    currentUser.getNumberAccount() != null ? currentUser.getNumberAccount() : "N/A");
            System.out.println("+----------------------------------+");
            System.out.println("|  1. Update information           |");
            System.out.println("|  2. Transfer money               |");
            System.out.println("|  3. View transaction history     |");
            System.out.println("|  4. Check balance                |");
            System.out.println("|  5. Withdraw money               |");
            System.out.println("|  0. Logout                       |");
            System.out.println("+----------------------------------+");
            System.out.print("Choose: ");

            int choice = readInt();
            switch (choice) {
                case 1 -> System.out.println("[Update Info - Task 2]");
                case 2 -> System.out.println("[Transfer - Task 4]");
                case 3 -> System.out.println("[History - Task 6]");
                case 4 -> System.out.println("[Balance - Task 5]");
                case 5 -> System.out.println("[Withdraw - Task 3]");
                case 0 -> {
                    System.out.println("Logged out!");
                    return;
                }
                default -> System.out.println("! Invalid choice");
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