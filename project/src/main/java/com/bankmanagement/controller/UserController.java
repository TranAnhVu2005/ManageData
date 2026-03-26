package com.bankmanagement.controller;

import com.bankmanagement.model.UserAccount;
import com.bankmanagement.view.UserView;
import com.bankmanagement.dao.UserAccoutsDAO;
import com.bankmanagement.function;

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
                case 1 -> createBankAccountRandom();
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

    private void createBankAccountRandom() {
        String newAccountNumber = function.generateStringRandom(10);
        String newPinCode = view.getPinCode();
        String pinCodeHash = org.mindrot.jbcrypt.BCrypt.hashpw(newPinCode, org.mindrot.jbcrypt.BCrypt.gensalt());

        int result = UserAccoutsDAO.createBankAccount(newAccountNumber, pinCodeHash, currentUser.getUserId());
        if(result == 0) {
            System.out.println("Bank account created successfully! Your new account number is: " + newAccountNumber);
        } else if(result == 1) {
            System.out.println("User does not exist. Please contact support.");
        } else if(result == 2) {
            System.out.println("Server error occurred. Please try again later.");
        } else {
            System.out.println("Account number already exists. Please try again." + result);
        }
    }

}