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
        return new String[]{acc, amount};
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

    @SuppressWarnings("unchecked")
    public void displayUsers(List<Map<String, String>> users) {
        System.out.println("\n--- Customer List ---");
        System.out.printf("%-12s | %-20s | %-12s | %-10s%n", "UserID", "Full Name", "Phone", "Role");
        System.out.println("------------------------------------------------------------------");
        for (Map<String, String> u : users) {
            System.out.printf("%-12s | %-20s | %-12s | %-10s%n",
                    u.get("userID"), u.get("userName"), u.get("numberPhone"), u.get("roleUser"));
        }
        System.out.println("Total: " + users.size() + " records found.");
    }

    public void displayAuditLogs(List<Map<String, Object>> logs) {
        System.out.println("\n--- Audit Logs ---");
        System.out.printf("%-5s | %-15s | %-18s | %-18s | %-20s%n",
                "ID", "Action", "Old Value", "New Value", "Time");
        System.out.println("-------------------------------------------------------------------------------------");
        for (Map<String, Object> log : logs) {
            System.out.printf("%-5s | %-15s | %-18s | %-18s | %-20s%n",
                    log.get("logId"),
                    log.get("actionType"),
                    log.get("oldValue"),
                    log.get("newValue"),
                    log.get("changedAt"));
        }
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
        return new String[]{name, phone, email, pass};
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
