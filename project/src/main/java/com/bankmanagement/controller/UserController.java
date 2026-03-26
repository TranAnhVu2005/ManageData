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

    public void createBankAccount(int numberOfAccounts) {
        view.createBankAccountMenu(numberOfAccounts);
        int choice = readInt();
        switch (choice) {
            case 1 -> createBankAccountRandom();
            case 2 -> createBankAccountWithPhone();
            case 0 -> {
                System.out.println("Exit create bank account!");
                return;
            }
            default -> System.out.println("! Invalid choice");
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
        view.showCreateAccountResult(result, newAccountNumber);
    }

    private void createBankAccountWithPhone() {
        String newAccountNumber = currentUser.getNumberPhone() + function.generateStringRandom(2);
        String newPinCode = view.getPinCode();
        String pinCodeHash = org.mindrot.jbcrypt.BCrypt.hashpw(newPinCode, org.mindrot.jbcrypt.BCrypt.gensalt());

        int result = UserAccoutsDAO.createBankAccount(newAccountNumber, pinCodeHash, currentUser.getUserId());
        view.showCreateAccountResult(result, newAccountNumber);
    }

}