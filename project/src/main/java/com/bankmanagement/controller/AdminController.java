package com.bankmanagement.controller;

import com.bankmanagement.dao.UserAccoutsDAO;
import com.bankmanagement.model.UserAccount;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Xử lý toàn bộ nghiệp vụ quản trị dành cho tài khoản Staff.
 *
 * Staff có thể: Khóa/Mở khóa tài khoản, xem danh sách khách hàng,
 * tìm kiếm, xem toàn bộ giao dịch hệ thống.
 * Các chức năng Tạo tài khoản (Task 9) và Nạp tiền (Task 7) do Lợi cài đặt riêng.
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
                case 1 -> System.out.println("[Create Bank Account - Task 9 - Loi]");
                case 2 -> System.out.println("[Deposit Money      - Task 7 - Loi]");
                case 3 -> blockAccount();
                case 4 -> unblockAccount();
                case 5 -> viewAllCustomers();
                case 6 -> searchCustomer();
                case 7 -> viewAllTransactions();
                case 0 -> { System.out.println("Logged out!"); return; }
                default -> System.out.println("! Invalid choice.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n+------------------------------------------+");
        System.out.printf("|  Staff: %-34s|%n", currentUser.getUserName());
        System.out.println("+------------------------------------------+");
        System.out.println("|  1. Create bank account   [Loi]          |");
        System.out.println("|  2. Deposit money         [Loi]          |");
        System.out.println("|  3. Block account                        |");
        System.out.println("|  4. Unblock account                      |");
        System.out.println("|  5. View all customers                   |");
        System.out.println("|  6. Search customer                      |");
        System.out.println("|  7. View all transactions                |");
        System.out.println("|  0. Logout                               |");
        System.out.println("+------------------------------------------+");
        System.out.print("Choose: ");
    }

    // =========================================================================
    // Task 8 — Khóa tài khoản ngân hàng
    // =========================================================================

    private void blockAccount() {
        System.out.println("\n--- BLOCK ACCOUNT ---");
        System.out.println("Note: Account must have 0 balance before blocking.");
        System.out.print("Account number to block: ");
        String numberAccount = sc.nextLine().trim();
        if (numberAccount.isEmpty()) {
            System.out.println("! Account number cannot be empty.");
            return;
        }
        String result = UserAccoutsDAO.deleteAccount(numberAccount);
        System.out.println("-> " + result);
    }

    // =========================================================================
    // Task 16 — Mở khóa tài khoản ngân hàng
    // =========================================================================

    private void unblockAccount() {
        System.out.println("\n--- UNBLOCK ACCOUNT ---");
        System.out.print("Account number to unblock: ");
        String numberAccount = sc.nextLine().trim();
        if (numberAccount.isEmpty()) {
            System.out.println("! Account number cannot be empty.");
            return;
        }
        String result = UserAccoutsDAO.unblockAccount(numberAccount);
        System.out.println("-> " + result);
    }

    // =========================================================================
    // Task 13 — Xem danh sách toàn bộ khách hàng
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
    // Task 14 — Tìm kiếm khách hàng theo SĐT hoặc CCCD
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
    // Task 15 — Xem toàn bộ giao dịch hệ thống
    // =========================================================================

    private void viewAllTransactions() {
        System.out.println("\n--- ALL SYSTEM TRANSACTIONS ---");
        String[] status = new String[1];
        List<Map<String, Object>> transactions = UserAccoutsDAO.getAllTransactions(status);

        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        printTransactionTable(transactions);
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

    private void printTransactionTable(List<Map<String, Object>> transactions) {
        System.out.printf("%-30s | %-20s | %-15s | %-12s | %-10s | %-10s%n",
            "TransactionID", "Date", "Amount", "Status", "From", "To");
        System.out.println("-".repeat(110));
        for (Map<String, Object> t : transactions) {
            System.out.printf("%-30s | %-20s | %,15.2f | %-12s | %-10s | %-10s%n",
                t.get("transactionId"),
                t.get("created_at"),
                t.get("amount"),
                t.get("state"),
                t.get("numberAccount"),
                t.getOrDefault("destinationAccount", "N/A"));
        }
        System.out.println("Total: " + transactions.size() + " transaction(s).");
    }

    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}