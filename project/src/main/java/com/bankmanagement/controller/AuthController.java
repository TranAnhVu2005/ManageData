package com.bankmanagement.controller;

import com.bankmanagement.dao.AuthDAO;
import com.bankmanagement.model.UserAccount;
import com.bankmanagement.view.LoginView;
import com.bankmanagement.view.UserView;

public class AuthController {

    private LoginView view = new LoginView();

    public void start() {
        while (true) {
            view.showMainMenu();
            int choice = view.getChoice();
            switch (choice) {
                case 1 -> handleLogin();
                case 2 -> handleRegister();
                case 0 -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> view.showError("Invalid choice!");
            }
        }
    }

    /**
     * Thu thập thông tin đăng nhập từ View, tiến hành xác thực qua DAO.
     * Nếu thành công, điều hướng người dùng tới Menu tương ứng với phân quyền (Staff/Client).
     */
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

        if (user.getRoleUser().equals("Staff")) {
            new AdminController(user).showMenu();
        } else {
            new UserView().showMenu(user);
        }
    }

    /**
     * Thu thập thông tin đăng ký mới từ View.
     * Tiến hành kiểm tra định dạng dữ liệu, băm mật khẩu và lưu vào cơ sở dữ liệu.
     */
    private void handleRegister() {
        String[] input = view.getRegisterInput();
        String userName = input[0];
        String id = input[1];
        String birthDay = input[2];
        String phone = input[3];
        String email = input[4];
        String password = input[5];
        String confirm = input[6];

        if (userName.isEmpty() || id.isEmpty() ||
                birthDay.isEmpty() || phone.isEmpty() ||
                email.isEmpty() || password.isEmpty()) {
            view.showError("Please fill in all fields!");
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
            view.showError("Date of Birth must format yyyy-MM-dd!");
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

        // Sinh userID ngẫu nhiên dựa trên thời gian thực
        String userID = "U" + System.currentTimeMillis() % 1000000;

        // Mã hóa mật khẩu bảo mật bằng BCrypt trước khi gọi procedure lưu trữ
        String hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt());
        String result = AuthDAO.register(
                userID, userName, id, hashedPassword, birthDay, phone, email);

        if (result.equals("Success")) {
            view.showSuccess("Registration successful! Please login.");
        } else {
            view.showError(result);
        }
    }
}