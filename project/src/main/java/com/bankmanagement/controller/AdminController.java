package com.bankmanagement.controller;

import com.bankmanagement.dao.AdminDAO;
import com.bankmanagement.dao.UserAccountsDAO;
import com.bankmanagement.model.UserAccount;
import com.bankmanagement.view.AdminView;

import java.util.List;
import java.util.Map;

/**
 * Xử lý nghiệp vụ quản trị dành cho Staff.
 * Đã được refactor sang chuẩn MVC (Model-View-Controller) và sử dụng AdminDAO chuyên biệt.
 */
public class AdminController {

    private final UserAccount currentUser;
    private final AdminView view = new AdminView();

    public AdminController(UserAccount user) {
        this.currentUser = user;
    }

    public void showMenu() {
        while (true) {
            view.showMenu(currentUser);
            int choice = view.getChoice();
            switch (choice) {
                case 1 -> depositMoney();
                case 2 -> blockAccount();
                case 3 -> unblockAccount();
                case 4 -> searchCustomer();
                case 5 -> viewAuditLogs();
                case 6 -> viewSystemStatistics();
                case 7 -> updateProfile();
                case 8 -> viewAllCustomers();
                case 0 -> {
                    view.showMessage("Logging out...");
                    return;
                }
                default -> view.showError("Invalid choice!");
            }
        }
    }

    private void updateProfile() {
        String[] input = view.getUpdateProfileInput();
        String name = input[0];
        String phone = input[1];
        String email = input[2];
        String pass = input[3];

        String passHash = null;
        if (!pass.isEmpty()) {
            if (pass.length() < 6) {
                view.showError("Password must be at least 6 characters!");
                return;
            }
            passHash = org.mindrot.jbcrypt.BCrypt.hashpw(pass, org.mindrot.jbcrypt.BCrypt.gensalt());
        }

        String result = UserAccountsDAO.updateInfo(currentUser.getUserId(), name, null, null, phone, email, passHash);
        if ("Success".equals(result)) {
            view.showSuccess("Profile updated successfully!");
            if (!name.isEmpty()) currentUser.setUserName(name);
            AdminDAO.logStaffAction(currentUser.getUserId(), "Update Self Profile", "Changed basic info/password");
        } else {
            view.showError(result);
        }
    }

    private void viewSystemStatistics() {
        Map<String, String> stats = AdminDAO.getSystemStatistics();
        if (stats.isEmpty()) {
            view.showError("Unable to fetch system statistics.");
            return;
        }
        view.displayStats(stats);
    }

    private void depositMoney() {
        String[] input = view.getDepositInput();
        String targetAccount = input[0];
        double amount;
        try {
            amount = Double.parseDouble(input[1]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            view.showError("Invalid amount.");
            return;
        }

        String transactionId = "D" + System.currentTimeMillis(); 
        int result = AdminDAO.depositMoney(currentUser.getUserId(), targetAccount, transactionId, amount);

        switch (result) {
            case 0 -> {
                view.showSuccess("Deposit successful to " + targetAccount);
                AdminDAO.logStaffAction(currentUser.getUserId(), "Deposit", "Amount: " + amount + " to " + targetAccount);
            }
            case 1 -> view.showError("Target account not found.");
            default -> view.showError("Transaction failed.");
        }
    }

    private void blockAccount() {
        String account = view.getAccountNumber("block");
        if (account.isEmpty()) return;
        String result = AdminDAO.blockAccount(account);
        if ("Success".equals(result)) {
            view.showSuccess("Account " + account + " blocked.");
            AdminDAO.logStaffAction(currentUser.getUserId(), "Block Account", "Account: " + account);
        } else {
            view.showError(result);
        }
    }

    private void unblockAccount() {
        String account = view.getAccountNumber("unblock");
        if (account.isEmpty()) return;
        String result = AdminDAO.unblockAccount(account);
        if ("Success".equals(result)) {
            view.showSuccess("Account " + account + " unblocked.");
            AdminDAO.logStaffAction(currentUser.getUserId(), "Unblock Account", "Account: " + account);
        } else {
            view.showError(result);
        }
    }

    private void viewAllCustomers() {
        List<Map<String, String>> users = AdminDAO.getAllUsers();
        view.displayUsers(users);
    }

    private void searchCustomer() {
        String keyword = view.getSearchKeyword();
        if (keyword.isEmpty()) return;

        String[] status = new String[1];
        List<Map<String, String>> users = AdminDAO.searchUser(keyword, status);

        if ("Not found".equals(status[0])) {
            view.showError("No customer found for: " + keyword);
        } else if (status[0] != null && status[0].startsWith("Error")) {
            view.showError(status[0]);
        } else {
            view.displayUsers(users);
        }
    }

    private void viewAuditLogs() {
        String userId = view.getAuditLogUserId();
        if (userId.isEmpty()) return;

        String[] status = new String[1];
        List<Map<String, Object>> logs = AdminDAO.viewAuditLogs(userId, status);

        if ("User not found".equals(status[0])) {
            view.showError(status[0]);
        } else if (logs.isEmpty()) {
            view.showMessage("No logs recorded for this user.");
        } else {
            view.displayAuditLogs(logs);
        }
    }
}