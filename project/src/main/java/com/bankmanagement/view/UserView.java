package com.bankmanagement.view;

import com.bankmanagement.model.BankAccount;
import com.bankmanagement.model.UserAccount;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Giao diện console dành cho Client.
 * CHỈ chứa logic hiển thị và thu thập input.
 */
public class UserView {

    private final Scanner sc = new Scanner(System.in);

    public void showMenu(UserAccount user, List<BankAccount> accounts) {
        System.out.println("\n--- CLIENT BANKING SERVICES ---");
        System.out.println("Welcome back, " + user.getUserName());

        if (accounts.isEmpty()) {
            System.out.println("[!] You don't have any active bank accounts yet.");
        } else {
            System.out.print("Active Accounts: ");
            for (BankAccount acc : accounts) {
                System.out.print("[" + acc.getNumberAccount() + "] ");
            }
            System.out.println();
        }

        System.out.println("\n==================================");
        System.out.println("            MAIN MENU             ");
        System.out.println("==================================");
        System.out.println("  1. Update Profile Information (updateInfo)");
        System.out.println("  2. Transfer Money");
        System.out.println("  3. View Transaction History (checkTransaction)");
        System.out.println("  4. Check Balance");
        System.out.println("  5. Withdraw Money");
        System.out.println("  6. Create New Bank Account");
        System.out.println("  7. My Personal Profile");
        System.out.println("  8. Search Transactions by Date");
        System.out.println("  9. Change Account PIN");
        System.out.println("  10. Create Card for Account");
        System.out.println("  0. Logout");
        System.out.println("==================================");
        System.out.print("Your Choice: ");
    }

    public int getChoice() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            return -1;
        }
    }

    public String[] getUpdateInfoInput() {
        System.out.println("\n--- Update Your Information ---");
        System.out.println("(Leave blank to keep current value)");
        System.out.print("New Name: ");
        String name = sc.nextLine().trim();
        System.out.print("New Phone: ");
        String phone = sc.nextLine().trim();
        System.out.print("New Email: ");
        String email = sc.nextLine().trim();
        System.out.print("New Password: ");
        String pass = sc.nextLine().trim();
        System.out.print("Current Password: ");
        String passCurrent = sc.nextLine().trim();
        return new String[] { name, phone, email, pass, passCurrent };
    }

    public String[] getTransferInput() {
        System.out.print("Destination Account: ");
        String dest = sc.nextLine().trim();
        System.out.print("Amount to transfer: ");
        String amount = sc.nextLine().trim();
        return new String[] { dest, amount };
    }

    public String[] getWithdrawInput(String cardNumber) {
        System.out.println("\n--- Withdraw Money from Card: " + cardNumber + " ---");
        System.out.print("Enter Card PIN: ");
        String pin = sc.nextLine().trim();
        System.out.print("Amount to withdraw: ");
        String amount = sc.nextLine().trim();
        return new String[] { pin, amount };
    }

    public void displayProfile(Map<String, String> profile) {
        System.out.println("\n--- Your Identity Profile ---");
        // System.out.printf(" %-15s : %s%n", "User ID", profile.get("userID"));
        System.out.printf("  %-15s : %s%n", "Full Name", profile.get("userName"));
        System.out.printf("  %-15s : %s%n", "ID Card", profile.get("ID"));
        System.out.printf("  %-15s : %s%n", "Phone", profile.get("numberPhone"));
        System.out.printf("  %-15s : %s%n", "Email", profile.get("email"));
        System.out.printf("  %-15s : %s%n", "Role", profile.get("roleUser"));
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }

    public int selectAccount(List<BankAccount> accounts, String title) {
        if (accounts.isEmpty())
            return -1;
        System.out.println("\n--- " + title + " ---");
        for (int i = 0; i < accounts.size(); i++) {
            System.out.printf("%d. %s\n", (i + 1),
                    accounts.get(i).getNumberAccount());
        }
        System.out.println("0. Cancel");
        System.out.print("Choose account: ");
        int sel = getChoice();
        if (sel > 0 && sel <= accounts.size())
            return sel - 1;
        return -1;
    }

    public int selectCard(List<com.bankmanagement.model.Cards> cards, String title) {
        if (cards.isEmpty())
            return -1;
        System.out.println("\n--- " + title + " ---");
        for (int i = 0; i < cards.size(); i++) {
            System.out.printf("%d. Card: %s (Attached to Acc: %s)%n",
                    (i + 1), cards.get(i).getCardNumber(), cards.get(i).getNumberAccount());
        }
        System.out.println("0. Cancel");
        System.out.print("Choose card: ");
        int sel = getChoice();
        if (sel > 0 && sel <= cards.size())
            return sel - 1;
        return -1;
    }

    public void showTransactionTable(List<Map<String, Object>> transactions, String myAccount) {
        if (transactions == null || transactions.isEmpty()) {
            System.out.println("\n[!] No transactions found for account: " + myAccount);
            return;
        }

        System.out
                .println("\n=========================================================================================");
        System.out.printf(" TRANSACTION HISTORY: %-60s %n", myAccount);
        System.out.println("=========================================================================================");
        System.out.printf("%-20s | %-16s | %-10s | %-15s | %-10s%n",
                "Time", "Amount (VND)", "Type", "Counterpart", "State");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (Map<String, Object> t : transactions) {
            String from = (String) t.get("numberAccount");
            String to = (String) t.get("destinationAccount");
            String typeCode = (String) t.get("type"); // W001, T001, D001
            double amount = (Double) t.get("amount");
            String state = (String) t.get("state");
            String time = (String) t.get("created_at");

            String displayAmount = "";
            String actionName = "";
            String peer = ""; // Đối tác giao dịch

            // Phân loại giao dịch dựa trên mã Type Code
            if ("D001".equals(typeCode)) {
                // NẠP TIỀN
                actionName = "Deposit";
                displayAmount = String.format("+%,.0f", amount); // Tiền vào (+)
                peer = "Cash/Staff";
            } else if ("W001".equals(typeCode)) {
                // RÚT TIỀN
                actionName = "Withdraw";
                displayAmount = String.format("-%,.0f", amount); // Tiền ra (-)
                peer = "ATM/Card";
            } else if ("T001".equals(typeCode)) {
                // CHUYỂN TIỀN
                actionName = "Transfer";
                if (myAccount.equals(from)) {
                    // Mình là người gửi (Tiền ra)
                    displayAmount = String.format("-%,.0f", amount);
                    peer = (to != null) ? to : "Unknown";
                } else {
                    // Mình là người nhận (Tiền vào)
                    displayAmount = String.format("+%,.0f", amount);
                    peer = from;
                }
            } else {
                // Loại giao dịch khác (Dự phòng)
                actionName = typeCode;
                displayAmount = String.format(" %,.0f", amount);
                peer = "Unknown";
            }

            System.out.printf("%-20s | %-16s | %-10s | %-15s | %-10s%n",
                    time, displayAmount, actionName, peer, state);
        }
        System.out.println("=========================================================================================");
    }

    public String[] getDateRange() {
        System.out.print("From Date (yyyy-MM-dd): ");
        String from = sc.nextLine().trim();
        System.out.print("To Date (yyyy-MM-dd): ");
        String to = sc.nextLine().trim();
        return new String[] { from, to };
    }

    public String enterPin(String prompt) {
        System.out.print(prompt + ": ");
        return sc.nextLine().trim();
    }

    public String[] getCreateCardInput() {
        System.out.print("New Card Number (16 digits): ");
        String num = sc.nextLine().trim();
        System.out.print("Set Card PIN (6 digits): ");
        String pin = sc.nextLine().trim();
        System.out.print("Security Code (3 digits): ");
        String ccv = sc.nextLine().trim();
        return new String[] { num, pin, ccv };
    }

    public int getAccountGenerationChoice() {
        System.out.println("\n--- Choose Account Number Generation Strategy ---");
        System.out.println("  1. Random 10-digit number");
        System.out.println("  2. Phone-based number (Phone + 2 random digits)");
        System.out.print("Your choice: ");
        return getChoice();
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
