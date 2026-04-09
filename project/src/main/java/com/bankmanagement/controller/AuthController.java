package com.bankmanagement.controller;

import com.bankmanagement.dao.AuthDAO;
import com.bankmanagement.model.UserAccount;
import com.bankmanagement.view.LoginView;

public class AuthController {

    private final LoginView view = new LoginView();

    public void start() {
        while (true) {
            view.showMainMenu();
            int choice = view.getChoice();
            switch (choice) {
                case 1 -> handleLogin();
                case 2 -> handleRegister();
                case 0 -> {
                    System.out.println("Goodbye! Thank you for using our banking services.");
                    return;
                }
                default -> view.showError("Invalid choice!");
            }
        }
    }

    private void handleLogin() {
        String[] input = view.getLoginInput();
        String account = input[0];
        String password = input[1];

        if (account.isEmpty() || password.isEmpty()) {
            view.showError("Please fill in all fields!");
            return;
        }

        UserAccount user = AuthDAO.login(account, password);
        if (user == null) {
            view.showError("Incorrect account or password!");
            return;
        }

        view.showSuccess("Welcome, " + user.getUserName());

        if ("Staff".equals(user.getRoleUser())) {
            new AdminController(user).showMenu();
        } else {
            new UserController(user).showMenu();
        }
    }

    private void handleRegister() {
        String[] input = view.getRegisterInput();
        String userName = input[0];
        String id = input[1];
        String birthDay = input[2];
        String phone = input[3];
        String email = input[4];
        String password = input[5];
        String confirm = input[6];

        if (userName.isEmpty() || id.isEmpty() || birthDay.isEmpty() || 
            phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            view.showError("Please fill in all required fields!");
            return;
        }
        
        if (!id.matches("\\d{12}")) {
            view.showError("Identity Card must be 12 digits!");
            return;
        }
        if (!phone.matches("\\d{10}")) {
            view.showError("Phone number must be 10 digits!");
            return;
        }
        if (!birthDay.matches("\\d{4}-\\d{2}-\\d{2}")) {
            view.showError("Date of Birth must follow yyyy-MM-dd format!");
            return;
        }
        if (password.length() < 6) {
            view.showError("Password must be at least 6 characters!");
            return;
        }
        if (!password.equals(confirm)) {
            view.showError("Password confirmation does not match!");
            return;
        }

        String userID = com.bankmanagement.function.generateStringRandom(6, "USERACCOUNTS", "userID", "U");
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
        
        String result = AuthDAO.register(userID, userName, id, hashedPassword, birthDay, phone, email);

        if ("Success".equals(result)) {
            view.showSuccess("Registration successful! You can now login.");
        } else {
            view.showError(result);
        }
    }
}