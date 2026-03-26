package com.bankmanagement.controller;

import com.bankmanagement.model.UserAccount;
import com.bankmanagement.view.UserView;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Controller xử lý các tính năng dành cho người dùng thông thường (Client).
 */
public class UserController {

    private UserAccount currentUser;
    private UserView view = new UserView();

    public UserController(UserAccount user) {
        this.currentUser = user;
    }

    private Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);

    public void createBankAccount() {
        while (true) {
            view.createBankAccountMenu();
            int choice = readInt();
            switch (choice) {
                case 1 -> System.out.println("[Create with random]");
                case 2 -> System.out.println("[Create with phone number]");
                case 0 -> {
                    System.out.println("Exit create bank account!");
                    return;
                }
                default -> System.out.println("! Invalid choice");
            }
        }
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