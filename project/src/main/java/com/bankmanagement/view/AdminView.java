package com.bankmanagement.view;

import com.bankmanagement.model.UserAccount;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AdminView {

    private final Scanner sc = new Scanner(System.in);

    public void showMenu(UserAccount user) {
        System.out.println("\n==================================");
        System.out.println("     STAFF MANAGEMENT DASHBOARD   ");
        System.out.println("==================================");
        System.out.println("Welcome, " + user.getUserName() + " (Staff)");
        System.out.println("  1. Deposit Money (depositMoney)");
        System.out.println("  2. Block Bank Account");
        System.out.println("  3. Unblock Bank Account");
        System.out.println("  4. Search Customer (searchUser)");
        System.out.println("  5. View Audit Logs");
        System.out.println("  6. System Statistics & Reports");
        System.out.println("  7. Update My Profile (Staff)");
        System.out.println("  8. View All Customers");
        System.out.println("  0. Logout");
        System.out.println("==================================");
        System.out.print("Your Choice: ");
    }

    public String[] getDepositInput() {
        System.out.println("\n--- Deposit Money ---");
        System.out.print("Target Account Number: ");
        String acc = sc.nextLine().trim();
        System.out.print("Amount to Deposit: ");
        String amount = sc.nextLine().trim();
        return new String[] { acc, amount };
    }

    public String getAccountNumber(String action) {
        System.out.print("\nEnter Account Number to " + action + ": ");
        return sc.nextLine().trim();
    }

    public String getSearchKeyword() {
        System.out.print("\nEnter phone number or Identity Card to search: ");
        return sc.nextLine().trim();
    }

    public String getAuditLogUserId() {
        System.out.print("\nEnter UserID to view change history: ");
        return sc.nextLine().trim();
    }

    public void displayStats(Map<String, String> stats) {
        System.out.println("\n--- System Statistics ---");
        System.out.printf("  %-25s: %s%n", "Total Registered Users", stats.get("totalUsers"));
        System.out.printf("  %-25s: %s%n", "Total Bank Accounts", stats.get("totalAccounts"));
        System.out.printf("  %-25s: %s %s%n", "Total System Balance", stats.get("totalBalance"), "VND");
        System.out.println("\nPress Enter to return...");
        sc.nextLine();
    }

    public void displayUsers(List<Map<String, String>> users) {
        System.out.println(
                "\n===============================================================================================================");
        System.out.printf("%-10s | %-20s | %-12s | %-12s | %-5s | %-35s%n",
                "User ID", "Full Name", "ID Card", "Phone", "Accs", "Bank Accounts (State)");
        System.out.println(
                "---------------------------------------------------------------------------------------------------------------");

        for (Map<String, String> u : users) {
            System.out.printf("%-10s | %-20s | %-12s | %-12s | %-5s | %-35s%n",
                    u.get("userID"),
                    u.get("userName"),
                    u.get("ID"),
                    u.get("numberPhone"),
                    u.get("totalAccounts"), // Số lượng tài khoản
                    u.get("accountList") // Chuỗi danh sách tài khoản
            );
        }
        System.out.println(
                "===============================================================================================================");
    }

    public void displayAuditLogs(List<Map<String, Object>> logs, String targetUser) {
        if (logs == null || logs.isEmpty()) {
            System.out.println("\n[!] No audit logs found for customer: " + targetUser);
            return;
        }

        System.out.println(
                "\n=====================================================================================================================");
        System.out.printf(" AUDIT LOGS FOR CUSTOMER (Phone/ID): %-76s %n", targetUser);
        System.out.println(
                "=====================================================================================================================");
        // Nới rộng cột Value lên 30 ký tự để chứa vừa Email dài
        System.out.printf("%-6s | %-16s | %-30s | %-30s | %-20s%n", "Log ID", "Action", "Old Value", "New Value",
                "Time");
        System.out.println(
                "---------------------------------------------------------------------------------------------------------------------");

        for (Map<String, Object> log : logs) {
            // Xử lý an toàn: Nếu dữ liệu null thì in ra dấu "-" thay vì chữ "null"
            String oldVal = log.get("oldValue") != null ? log.get("oldValue").toString() : "-";
            String newVal = log.get("newValue") != null ? log.get("newValue").toString() : "-";
            String time = log.get("changedAt") != null ? log.get("changedAt").toString() : "-";

            // Cắt bớt chuỗi nếu quá 30 ký tự (Chống vỡ bảng Console)
            if (oldVal.length() > 30)
                oldVal = oldVal.substring(0, 27) + "...";
            if (newVal.length() > 30)
                newVal = newVal.substring(0, 27) + "...";

            // Xóa đuôi ".0" ở phần millisecond của biến Timestamp (nếu có) cho đẹp
            if (time.endsWith(".0"))
                time = time.substring(0, time.length() - 2);

            System.out.printf("%-6s | %-16s | %-30s | %-30s | %-20s%n",
                    log.get("logId"),
                    log.get("actionType"),
                    oldVal,
                    newVal,
                    time);
        }
        System.out.println(
                "=====================================================================================================================");
    }

    public String[] getUpdateProfileInput() {
        System.out.println("\n--- Update Your Staff Profile ---");
        System.out.println("(Leave blank to keep current value)");
        System.out.print("New Name: ");
        String name = sc.nextLine().trim();
        System.out.print("New Phone: ");
        String phone = sc.nextLine().trim();
        System.out.print("New Email: ");
        String email = sc.nextLine().trim();
        System.out.print("New Password (min 6 chars): ");
        String pass = sc.nextLine().trim();
        return new String[] { name, phone, email, pass };
    }

    public int getChoice() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public void showMessage(String msg) {
        System.out.println("-> " + msg);
    }

    public void showError(String msg) {
        System.out.println("[ERROR] " + msg);
    }

    public void showSuccess(String msg) {
        System.out.println("[SUCCESS] " + msg);
    }
}
