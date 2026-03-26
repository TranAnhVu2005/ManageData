package com.bankmanagement.view;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.bankmanagement.controller.UserController;
import com.bankmanagement.model.UserAccount;

public class UserView {

    private Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
    
    public void showMenu(UserAccount currentUser) {
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
            System.out.println("|  6. Create bank account          |");
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
                case 6 -> new UserController(currentUser).createBankAccount();
                case 0 -> {
                    System.out.println("Logged out!");
                    return;
                }
                default -> System.out.println("! Invalid choice");
            }
        }
    }

    public void createBankAccountMenu() {
        System.out.println("[Create Bank Account]");
        System.out.println("Create a new bank account based on random: 1");
        System.out.println("Create a new bank account based on phone number with 2 random digits: 2");
        System.out.println("Exit: 0");
        System.out.print("Choose: ");
    }

    private int readInt() {
        try {
            return Integer.parseInt(
                    sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
