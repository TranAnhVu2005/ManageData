package com.bankmanagement.view;

import com.bankmanagement.controller.UserController;
import com.bankmanagement.dao.UserAccoutsDAO;
import com.bankmanagement.model.BankAccount;
import com.bankmanagement.model.UserAccount;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Toàn bộ giao diện console dành cho người dùng Client.
 * Phụ trách: Hiển thị menu, thu thập input, in kết quả ra màn hình.
 * Không chứa bất kỳ logic nghiệp vụ nào — mọi xử lý đều đẩy lên Controller.
 */
public class UserView {

    private final Scanner sc;

    public UserView() {
        this.sc = new Scanner(System.in, StandardCharsets.UTF_8);
    }

    public UserView(Scanner sc) {
        this.sc = sc;
    }

    /**
     * Vòng lặp menu chính của Client — tự động tải lại danh sách tài khoản sau mỗi
     * thao tác
     * để đảm bảo số dư và danh sách luôn phản ánh trạng thái mới nhất từ DB.
     */
    public void showMenu(UserAccount currentUser) {
        while (true) {
            // Tải lại mỗi lần để phản ánh thay đổi sau khi tạo tài khoản mới
            BankAccount[] bankAccounts = UserAccoutsDAO.getActiveAccountByUserId(currentUser.getUserId());
            boolean hasAccount = hasAny(bankAccounts);
            int numberOfAccounts = countAccounts(bankAccounts);

            printHeader(currentUser, bankAccounts, hasAccount);

            int choice = readInt();
            switch (choice) {
                case 1 -> new UserController(currentUser, sc).updateInfo();
                case 2 -> new UserController(currentUser, sc).transferMoney(bankAccounts);
                case 3 -> new UserController(currentUser, sc).checkTransaction(bankAccounts);
                case 4 -> new UserController(currentUser, sc).getBalance(bankAccounts);
                case 5 -> new UserController(currentUser, sc).withdrawMoney();
                case 6 -> new UserController(currentUser, sc).createBankAccount(numberOfAccounts);
                case 7 -> new UserController(currentUser, sc).viewProfile();
                case 8 -> new UserController(currentUser, sc).searchTransactionByDate(bankAccounts);
                case 9 -> new UserController(currentUser, sc).changePin(bankAccounts);
                case 10 -> new UserController(currentUser, sc).createCard(bankAccounts);
                case 0 -> {
                    System.out.println("Logged out!");
                    return;
                }
                default -> System.out.println("! Invalid choice.");
            }
        }
    }

    private void printHeader(UserAccount user, BankAccount[] accounts, boolean hasAccount) {
        System.out.println("\n+------------------------------------+");
        System.out.printf("|  Hello: %-26s|%n", user.getUserName());
        System.out.println("+------------------------------------+");
        if (hasAccount) {
            for (BankAccount acc : accounts) {
                if (acc != null) {
                    System.out.printf("|  ACC: %-29s|%n", acc.getNumberAccount());
                }
            }
        } else {
            System.out.println("|  No active bank account            |");
        }
        System.out.println("+------------------------------------+");
        System.out.println("|  1.  Update information            |");
        System.out.println("|  2.  Transfer money                |");
        System.out.println("|  3.  Transaction history           |");
        System.out.println("|  4.  Check balance                 |");
        System.out.println("|  5.  Withdraw money                |");
        System.out.println("|  6.  Create bank account           |");
        System.out.println("|  7.  View profile                  |");
        System.out.println("|  8.  Search transactions by date   |");
        System.out.println("|  9.  Change bank PIN               |");
        System.out.println("|  10.  Create Card for Bank Account |");
        System.out.println("|  0.  Logout                        |");
        System.out.println("+------------------------------------+");
        System.out.print("Choose: ");
    }

    // =========================================================================
    // Hiển thị menu chọn tài khoản (dùng chung nhiều tính năng)
    // =========================================================================

    public void showBalanceMenu(BankAccount[] bankAccounts) {
        int i = 1;
        System.out.println("\nYour bank accounts:");
        System.out.printf("  %-6s | %-12s%n", "No.", "Account");
        System.out.println("  " + "-".repeat(22));
        for (BankAccount acc : bankAccounts) {
            if (acc != null) {
                System.out.printf("  %-6d | %-12s%n", i++, acc.getNumberAccount());
            }
        }
        System.out.println("  0      | Exit");
        System.out.print("Choose: ");
    }

    // =========================================================================
    // Hiển thị kết quả các nghiệp vụ
    // =========================================================================

    /**
     * In kết quả kiểm tra số dư.
     * result code đồng bộ với checkBalance Proc: 0=OK, 1=not found, 3=blocked,
     * 4=error.
     */
    public void showBalanceResult(double balance, int resultCode) {
        switch (resultCode) {
            case 0 -> System.out.printf("Balance: %,.2f VND%n", balance);
            case 1 -> System.out.println("! Account not found. Please contact support.");
            case 3 -> System.out.println("! Account is Blocked. Please contact support.");
            case 4 -> System.out.println("! Server error. Please try again later.");
            default -> System.out.println("! Unknown error. Please contact support.");
        }
    }

    /**
     * In kết quả tạo tài khoản ngân hàng.
     * result code đồng bộ với createBankAccount Proc:
     * 0=OK, 1=user not found, 2=server error, 3=duplicate account.
     */
    public void showCreateAccountResult(int result, String newAccountNumber) {
        switch (result) {
            case 0 -> System.out.println("Bank account created! Account number: " + newAccountNumber);
            case 1 -> System.out.println("! User not found. Please contact support.");
            case 2 -> System.out.println("! Server error. Please try again later.");
            default -> System.out.println("! Account number conflict. Please try again.");
        }
    }

    // =========================================================================
    // Hiển thị bảng giao dịch
    // =========================================================================

    /**
     * In lịch sử giao dịch dạng bảng — dùng chung cho checkTransaction và
     * searchTransactionByDate.
     * numberAccount giúp phân biệt giao dịch đến (IN) hay đi (OUT).
     */
    public void showTransactionTable(List<Map<String, Object>> transactions, String numberAccount) {
        if (transactions.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }

        System.out.println();
        System.out.printf("%-14s | %-20s | %-15s | %-10s | %-6s | %-12s%n",
                "ID", "Date", "Amount (VND)", "Status", "Dir", "Counterpart");
        System.out.println("-".repeat(95));

        for (Map<String, Object> t : transactions) {
            String from = (String) t.get("numberAccount");
            String to = t.get("destinationAccount") != null ? (String) t.get("destinationAccount") : "";

            // Chiều giao dịch: OUT = tiền đi, IN = tiền đến
            // Nếu cái mình chọn bằng với cái trong transaction thì out, ngược lại thì in
            String direction = numberAccount.equals(from) ? "OUT" : "IN";
            // Logic tương tự
            String counterpart = numberAccount.equals(from) ? (to.isEmpty() ? "N/A" : to) : from;

            System.out.printf("%-14s | %-20s | %,15.2f | %-10s | %-6s | %-12s%n",
                    shorten((String) t.get("transactionId"), 14),
                    t.get("created_at"),
                    t.get("amount"),
                    t.get("state"),
                    direction,
                    counterpart);
        }
        System.out.println("-".repeat(95));
        System.out.println("Total: " + transactions.size() + " transaction(s).");
    }

    // =========================================================================
    // Hiển thị hồ sơ cá nhân (Task 11)
    // =========================================================================

    public void showProfile(Map<String, String> profile) {
        System.out.println("\n+---- YOUR PROFILE ----+");
        System.out.println("  User ID   : " + profile.get("userID"));
        System.out.println("  Full Name : " + profile.get("userName"));
        System.out.println("  ID Card   : " + profile.get("ID"));
        System.out.println("  Birthday  : " + profile.get("birthDay"));
        System.out.println("  Phone     : " + profile.get("numberPhone"));
        System.out.println("  Email     : " + profile.get("email"));
        System.out.println("  Role      : " + profile.get("roleUser"));
        System.out.println("+----------------------+");
    }

    // =========================================================================
    // Thu thập mã PIN từ người dùng
    // =========================================================================

    /**
     * Dùng khi TẠO PIN mới: Nhập 2 lần để xác nhận, validate đúng 6 chữ số.
     */
    public String getPinCode() {
        while (true) {
            String pin = readPassword("New 6-digit PIN: ");
            if (!pin.matches("\\d{6}")) {
                System.out.println("! PIN must be exactly 6 digits.");
                continue;
            }
            String confirm = readPassword("Confirm PIN    : ");
            if (pin.equals(confirm))
                return pin;
            System.out.println("! PINs do not match. Try again.");
        }
    }

    /**
     * Dùng khi XÁC THỰC PIN hiện tại: Chỉ nhập 1 lần, không validate định dạng.
     */
    public String enterPin() {
        while (true) {
            String pin = readPassword("Enter current PIN: ");
            if (pin.isEmpty()) {
                System.out.println("! PIN cannot be empty.");
                continue;
            }
            if (!pin.matches("\\d{6}")) {
                System.out.println("! Warning: PIN should be 6 digits.");
                continue;
            }
            return pin;
        }
    }

    // =========================================================================
    // Menu tạo tài khoản ngân hàng
    // =========================================================================

    public void createBankAccountMenu(int numberOfAccounts) {
        System.out.println("\n[Create Bank Account]");
        System.out.printf("You have %d/10 account(s). Can create %d more.%n",
                numberOfAccounts, 10 - numberOfAccounts);

        if (numberOfAccounts >= 10) {
            System.out.println("! Maximum accounts reached. Block an existing one first.");
            return;
        }
        System.out.println("  1. Random account number");
        System.out.println("  2. Based on phone number (+ 2 digits)");
        System.out.println("  0. Exit");
        System.out.print("Choose: ");
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private boolean hasAny(BankAccount[] accounts) {
        if (accounts == null)
            return false;
        for (BankAccount acc : accounts) {
            if (acc != null)
                return true;
        }
        return false;
    }

    private int countAccounts(BankAccount[] accounts) {
        if (accounts == null)
            return 0;
        int count = 0;
        for (BankAccount acc : accounts) {
            if (acc != null)
                count++;
        }
        return count;
    }

    /** Cắt ngắn chuỗi TransactionID dài cho vừa cột bảng. */
    private String shorten(String s, int maxLen) {
        if (s == null)
            return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 2) + "..";
    }

    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String readPassword(String prompt) {
        if (System.console() != null) {
            char[] pwd = System.console().readPassword(prompt);
            return pwd != null ? new String(pwd).trim() : "";
        }
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    public void showCreateCardResult(int result, String cardNumber) {
        switch (result) {
            case 0 -> System.out.println("Card created! Card number: " + cardNumber);
            case 1 -> System.out.println("! Account not found. Please contact support.");
            case 2 -> System.out.println("! Server error. Please try again later.");
            default -> System.out.println("! Unknown error. Please contact support." + result);
        }
    }
}
