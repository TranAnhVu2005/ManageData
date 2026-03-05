package com.bankmanagement.view.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField accountField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button loginBtn;

    // =========================================
    // MOCK DATA — Xóa khi kết nối DB thật
    // =========================================
    private static final String MOCK_ACCOUNT = "ACC0000001";
    private static final String MOCK_PASSWORD = "123456";
    private static final String MOCK_ADMIN_ACCOUNT = "ACC0000003";
    private static final String MOCK_ADMIN_PASSWORD = "admin123";
    // =========================================

    @FXML
    private void handleLogin() {
        String account = accountField.getText().trim();
        String password = passwordField.getText().trim();

        // Validate rỗng
        if (account.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // ---- MOCK: Giả lập đăng nhập Client ----
        if (account.equals(MOCK_ACCOUNT)
                && password.equals(MOCK_PASSWORD)) {
            navigateTo("/fxml/UserMenu.fxml", "Menu người dùng");
            return;
        }

        showError("Sai tài khoản hoặc mật khẩu!");

        // ---- Khi có DB thật, thay bằng: ----
        /*
         * String role = AuthService.login(account, password);
         * if (role == null) {
         * showError("Sai tài khoản hoặc mật khẩu!");
         * } else if (role.equals("Client")) {
         * navigateTo("/fxml/UserMenu.fxml", "Menu người dùng");
         * }
         */
    }

    @FXML
    private void handleAdminLogin() {
        String account = accountField.getText().trim();
        String password = passwordField.getText().trim();

        if (account.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin!");
            return;
        }

        // ---- MOCK: Giả lập đăng nhập Admin ----
        if (account.equals(MOCK_ADMIN_ACCOUNT)
                && password.equals(MOCK_ADMIN_PASSWORD)) {
            navigateTo("/fxml/AdminMenu.fxml", "Menu Admin");
            return;
        }

        showError("Sai tài khoản Admin!");
    }

    // ---- Helper methods ----
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource(fxmlPath));
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (Exception e) {
            // Nếu màn hình tiếp theo chưa tạo → thông báo
            showError("Màn hình '" + title + "' chưa được tạo!");
        }
    }
}