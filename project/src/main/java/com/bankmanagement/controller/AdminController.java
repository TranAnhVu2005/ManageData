package com.bankmanagement.controller;

import com.bankmanagement.dao.UserAccoutsDAO;
import com.bankmanagement.model.UserAccount;
import com.bankmanagement.model.BankAccount;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Xử lý toàn bộ nghiệp vụ quản trị dành cho tài khoản Staff.
 *
 * Staff có thể: Khóa/Mở khóa tài khoản, xem danh sách khách hàng,
 * tìm kiếm, xem toàn bộ giao dịch hệ thống.
 * Các chức năng Tạo tài khoản (Task 9) và Nạp tiền (Task 7) do Lợi cài đặt
 * riêng.
 */
public class AdminController {

    private final UserAccount currentUser;
    private final Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);

    public AdminController(UserAccount user) {
        this.currentUser = user;
    }

    public void showMenu() {
        while (true) {
            printMenu();
            int choice = readInt();
            switch (choice) {
                case 1 -> createBankAccount();
                case 2 -> depositMoney();
                case 3 -> blockAccount();
                case 4 -> viewAllCustomers();
                case 5 -> searchCustomer();
                case 6 -> viewSystemStatistics();
                case 0 -> {
                    System.out.println("Logged out!");
                    return;
                }
                default -> System.out.println("! Invalid choice.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n+------------------------------------------+");
        System.out.printf("|  Staff: %-34s|%n", currentUser.getUserName());
        System.out.println("+------------------------------------------+");
        System.out.println("|  1. Create bank account                  |");
        System.out.println("|  2. Deposit money                        |");
        System.out.println("|  3. Block account                        |");
        System.out.println("|  4. View all customers                   |");
        System.out.println("|  5. Search customer                      |");
        System.out.println("|  6. View system statistics               |");
        System.out.println("|  0. Logout                               |");
        System.out.println("+------------------------------------------+");
        System.out.print("Choose: ");
    }

    private void viewSystemStatistics() {
        System.out.println("\n--- SYSTEM STATISTICS ---");
        Map<String, String> stats = UserAccoutsDAO.getSystemStatistics();
        if (stats == null || stats.isEmpty()) {
            System.out.println("! Unable to fetch system statistics.");
            return;
        }
        System.out.println("Total Users: " + stats.getOrDefault("totalUsers", "N/A"));
        System.out.println("Total Bank Accounts: " + stats.getOrDefault("totalAccounts", "N/A"));
        System.out.println("Total Balance in System: " + stats.getOrDefault("totalBalance", "N/A"));
        while (true) {
            System.out.println("Want to exist? (Y)");
            String choice = sc.nextLine().trim().toUpperCase();
            if (choice.equals("Y")) {
                break;
            } else {
                System.out.println("! Invalid choice.");
            }
        }
    }

    // =========================================================================
    // Task 9 — Tạo tài khoản ngân hàng (Lợi)
    // =========================================================================

    private void createBankAccount() {
        System.out.println("\n+------ CREATE BANK ACCOUNT (ADMIN) ------+");
        System.out.print("Enter Customer UserID: ");
        String userId = sc.nextLine().trim();
        if (userId.isEmpty()) {
            System.out.println("! UserID cannot be empty.");
            return;
        }

        System.out.print("Enter New Account Number (10 chars): ");
        String accountNumber = sc.nextLine().trim();
        if (accountNumber.length() != 10) {
            System.out.println("! Account number must be exactly 10 characters.");
            return;
        }

        System.out.print("Enter Initial PIN (6 digits): ");
        String pin = sc.nextLine().trim();
        if (!pin.matches("\\d{6}")) {
            System.out.println("! PIN must be exactly 6 digits.");
            return;
        }

        String pinHash = org.mindrot.jbcrypt.BCrypt.hashpw(pin, org.mindrot.jbcrypt.BCrypt.gensalt());
        int result = UserAccoutsDAO.createBankAccount(accountNumber, pinHash, userId);

        switch (result) {
            case 0 -> System.out.println("-> Bank account created successfully! Account number: " + accountNumber);
            case 1 -> System.out.println("! User not found.");
            case 2 -> System.out.println("! Server error. Please try again later.");
            default -> System.out.println("! Account number conflict or limits reached.");
        }
    }

    // =========================================================================
    // Task 7 — Nạp tiền (Lợi)
    // =========================================================================

    private void depositMoney() {
        BankAccount[] staffAccounts = UserAccoutsDAO.getActiveAccountByUserId(currentUser.getUserId());
        if (staffAccounts == null || staffAccounts[0] == null) {
            System.out.println(
                    "! You must create a Bank Account for yourself (Staff Account) before you can deposit money.");
            return;
        }
        String staffAccount = staffAccounts[0].getNumberAccount();

        System.out.println("\n+------ DEPOSIT MONEY ------+");
        System.out.print("Enter Customer Account Number: ");
        String targetAccount = sc.nextLine().trim();
        if (targetAccount.isEmpty()) {
            System.out.println("! Account number cannot be empty.");
            return;
        }

        System.out.print("Enter Deposit Amount: ");
        double amount = -1;
        try {
            amount = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("! Invalid amount format.");
            return;
        }

        if (amount <= 0) {
            System.out.println("! Amount must be greater than 0.");
            return;
        }

        String transactionId = com.bankmanagement.function.generateStringRandom(29, null, null, "D"); // Mã giao dịch bắt đầu bằng 'D' để dễ phân biệt
        int result = UserAccoutsDAO.depositMoney(staffAccount, targetAccount, transactionId, amount);

        switch (result) {
            case 0 -> System.out.println("-> Deposit successful! Amount added to: " + targetAccount);
            case 1 -> System.out.println("! Account not found.");
            case 2 -> System.out.println("! Server error. Transaction failed.");
            case 5 -> System.out.println("! Authorization failed. You are not Staff.");
            default -> System.out.println("! Unknown error.");
        }
    }

    // =========================================================================
    // Task 8 — Khóa tài khoản ngân hàng
    // =========================================================================

    private void blockAccount() {
        System.out.println("\n--- BLOCK ACCOUNT ---");
        System.out.print("Account number to block: ");
        String numberAccount = sc.nextLine().trim();
        if (numberAccount.isEmpty()) {
            System.out.println("! Account number cannot be empty.");
            return;
        }
        String result = UserAccoutsDAO.blockAccount(numberAccount);
        System.out.println("-> " + result);
    }

    // =========================================================================
    // Xem danh sách toàn bộ khách hàng
    // =========================================================================

    private void viewAllCustomers() {
        System.out.println("\n--- ALL CUSTOMERS ---");
        List<Map<String, String>> users = UserAccoutsDAO.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        printUserTable(users);
    }

    // =========================================================================
    // Task 12 — Tìm kiếm khách hàng theo SĐT hoặc CCCD
    // =========================================================================

    private void searchCustomer() {
        System.out.println("\n--- SEARCH CUSTOMER ---");
        System.out.print("Enter phone number or ID card: ");
        String keyword = sc.nextLine().trim();
        if (keyword.isEmpty()) {
            System.out.println("! Search keyword cannot be empty.");
            return;
        }

        String[] status = new String[1];
        List<Map<String, String>> users = UserAccoutsDAO.searchUser(keyword, status);

        if ("Not found".equals(status[0])) {
            System.out.println("No customer found matching: " + keyword);
            return;
        }
        if (status[0] != null && status[0].startsWith("Error")) {
            System.out.println("! " + status[0]);
            return;
        }
        printUserTable(users);
    }

    // =========================================================================
    // HELPERS — In dữ liệu ra console dạng bảng
    // =========================================================================

    private void printUserTable(List<Map<String, String>> users) {
        System.out.printf("%-10s | %-25s | %-12s | %-10s | %-8s%n",
                "UserID", "Name", "Phone", "Role", "BirthDay");
        System.out.println("-".repeat(80));
        for (Map<String, String> u : users) {
            System.out.printf("%-10s | %-25s | %-12s | %-10s | %-8s%n",
                    u.get("userID"),
                    u.get("userName"),
                    u.get("numberPhone"),
                    u.get("roleUser"),
                    u.get("birthDay"));
        }
        System.out.println("Total: " + users.size() + " record(s).");
    }

    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}