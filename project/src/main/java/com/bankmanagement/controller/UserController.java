package com.bankmanagement.controller;

import com.bankmanagement.dao.UserAccoutsDAO;
import com.bankmanagement.function;
import com.bankmanagement.model.BankAccount;
import com.bankmanagement.model.UserAccount;
import com.bankmanagement.view.UserView;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Xử lý toàn bộ nghiệp vụ dành cho người dùng Client sau khi đăng nhập.
 *
 * Nguyên tắc bảo mật xuyên suốt: Xác thực PIN/mật khẩu tại Java (BCrypt)
 * trước khi gọi Stored Procedure thực thi nghiệp vụ.
 */
public class UserController {

    private UserAccount currentUser;
    private final UserView view = new UserView();
    private final Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);

    public UserController(UserAccount user) {
        this.currentUser = user;
    }

    // =========================================================================
    // Task 9 — Tạo tài khoản ngân hàng
    // =========================================================================

    public void createBankAccount(int numberOfAccounts) {
        view.createBankAccountMenu(numberOfAccounts);
        int choice = readInt();
        switch (choice) {
            case 1 -> createBankAccountRandom();
            case 2 -> createBankAccountWithPhone();
            case 0 -> System.out.println("Exit create bank account!");
            default -> System.out.println("! Invalid choice");
        }
    }

    private void createBankAccountRandom() {
        String newAccountNumber = function.generateStringRandom(10);
        String pin = view.getPinCode();
        String pinHash = org.mindrot.jbcrypt.BCrypt.hashpw(pin, org.mindrot.jbcrypt.BCrypt.gensalt());
        int result = UserAccoutsDAO.createBankAccount(newAccountNumber, pinHash, currentUser.getUserId());
        view.showCreateAccountResult(result, newAccountNumber);
    }

    private void createBankAccountWithPhone() {
        // Số tài khoản = SĐT + 2 chữ số ngẫu nhiên (đảm bảo dễ nhớ)
        String newAccountNumber = currentUser.getNumberPhone() + function.generateStringRandom(2);
        String pin = view.getPinCode();
        String pinHash = org.mindrot.jbcrypt.BCrypt.hashpw(pin, org.mindrot.jbcrypt.BCrypt.gensalt());
        int result = UserAccoutsDAO.createBankAccount(newAccountNumber, pinHash, currentUser.getUserId());
        view.showCreateAccountResult(result, newAccountNumber);
    }

    // =========================================================================
    // Task 5 — Kiểm tra số dư
    // =========================================================================

    public void getBalance(BankAccount[] bankAccounts) {
        if (!hasAnyAccount(bankAccounts)) {
            System.out.println("! You don't have any active bank account.");
            return;
        }

        while (true) {
            view.showBalanceMenu(bankAccounts);
            int choice = readInt();
            if (choice == 0) return;

            BankAccount selected = pickAccount(bankAccounts, choice);
            if (selected == null) {
                System.out.println("! Invalid choice.");
                continue;
            }

            String pin = view.enterPin();
            if (!UserAccoutsDAO.verifyAccountPin(selected.getNumberAccount(), pin)) {
                System.out.println("! Incorrect PIN.");
                continue;
            }

            Map<String, Object> res = UserAccoutsDAO.getBalance(selected.getNumberAccount());
            view.showBalanceResult((double) res.get("balance"), (int) res.get("resultCode"));
        }
    }

    // =========================================================================
    // Task 4 — Chuyển tiền
    // =========================================================================

    public void transferMoney(BankAccount[] bankAccounts) {
        if (!hasAnyAccount(bankAccounts)) {
            System.out.println("! You don't have any active bank account to transfer from.");
            return;
        }

        System.out.println("\n+------ TRANSFER MONEY ------+");
        view.showBalanceMenu(bankAccounts);
        System.out.print("Select source account: ");
        int choice = readInt();
        if (choice == 0) return;

        BankAccount source = pickAccount(bankAccounts, choice);
        if (source == null) {
            System.out.println("! Invalid selection.");
            return;
        }

        System.out.print("Destination account number: ");
        String destAccount = sc.nextLine().trim();
        if (destAccount.isEmpty()) {
            System.out.println("! Destination account cannot be empty.");
            return;
        }

        System.out.print("Amount to transfer: ");
        double amount = readDouble();
        if (amount <= 0) {
            System.out.println("! Amount must be greater than 0.");
            return;
        }

        // Xác thực PIN tại Java — Proc không kiểm tra PIN
        String pin = view.enterPin();
        if (!UserAccoutsDAO.verifyAccountPin(source.getNumberAccount(), pin)) {
            System.out.println("! Authentication failed. Incorrect PIN.");
            return;
        }

        String result = UserAccoutsDAO.transferMoney(source.getNumberAccount(), destAccount, amount);
        System.out.println("-> " + result);
    }

    // =========================================================================
    // Task 6 — Xem lịch sử giao dịch
    // =========================================================================

    public void checkTransaction(BankAccount[] bankAccounts) {
        if (!hasAnyAccount(bankAccounts)) {
            System.out.println("! You don't have any active bank account.");
            return;
        }

        view.showBalanceMenu(bankAccounts);
        System.out.print("Select account to view history: ");
        int choice = readInt();
        if (choice == 0) return;

        BankAccount selected = pickAccount(bankAccounts, choice);
        if (selected == null) {
            System.out.println("! Invalid selection.");
            return;
        }

        String[] status = new String[1];
        List<Map<String, Object>> transactions =
                UserAccoutsDAO.checkTransaction(selected.getNumberAccount(), status);

        if (!"Success".equals(status[0])) {
            System.out.println("! " + status[0]);
            return;
        }

        view.showTransactionTable(transactions, selected.getNumberAccount());
    }

    // =========================================================================
    // Task 10 — Đổi mã PIN tài khoản ngân hàng
    // =========================================================================

    public void changePin(BankAccount[] bankAccounts) {
        if (!hasAnyAccount(bankAccounts)) {
            System.out.println("! You don't have any active bank account.");
            return;
        }

        System.out.println("\n+------ CHANGE ACCOUNT PIN ------+");
        view.showBalanceMenu(bankAccounts);
        System.out.print("Select account to change PIN: ");
        int choice = readInt();
        if (choice == 0) return;

        BankAccount selected = pickAccount(bankAccounts, choice);
        if (selected == null) {
            System.out.println("! Invalid selection.");
            return;
        }

        // Bước 1: Xác thực PIN hiện tại
        String oldPin = view.enterPin();
        if (!UserAccoutsDAO.verifyAccountPin(selected.getNumberAccount(), oldPin)) {
            System.out.println("! Current PIN is incorrect. Operation cancelled.");
            return;
        }

        // Bước 2: Nhập và xác nhận PIN mới
        System.out.println("Enter your new PIN:");
        String newPin = view.getPinCode(); // getPinCode có confirm + validate 6 số
        String newPinHash = org.mindrot.jbcrypt.BCrypt.hashpw(newPin, org.mindrot.jbcrypt.BCrypt.gensalt());

        String result = UserAccoutsDAO.changeAccountPin(selected.getNumberAccount(), newPinHash);
        System.out.println("-> " + result);
    }

    // =========================================================================
    // Xem hồ sơ cá nhân
    // =========================================================================

    public void viewProfile() {
        Map<String, String> profile = UserAccoutsDAO.getProfile(currentUser.getUserId());
        if (profile == null) {
            System.out.println("! Cannot load profile. Please try again.");
            return;
        }
        view.showProfile(profile);
    }

    // =========================================================================
    // Task 11 — Tìm kiếm lịch sử giao dịch theo ngày
    // =========================================================================

    public void searchTransactionByDate(BankAccount[] bankAccounts) {
        if (!hasAnyAccount(bankAccounts)) {
            System.out.println("! You don't have any active bank account.");
            return;
        }

        view.showBalanceMenu(bankAccounts);
        System.out.print("Select account: ");
        int choice = readInt();
        if (choice == 0) return;

        BankAccount selected = pickAccount(bankAccounts, choice);
        if (selected == null) {
            System.out.println("! Invalid selection.");
            return;
        }

        System.out.print("From date (yyyy-MM-dd): ");
        String fromDate = sc.nextLine().trim();
        System.out.print("To date   (yyyy-MM-dd): ");
        String toDate = sc.nextLine().trim();

        if (fromDate.isEmpty() || toDate.isEmpty()) {
            System.out.println("! Date range cannot be empty.");
            return;
        }

        // Thêm giờ để đảm bảo BETWEEN bao trùm cả ngày cuối
        String[] status = new String[1];
        List<Map<String, Object>> transactions = UserAccoutsDAO.searchTransactionByDate(
                selected.getNumberAccount(), fromDate + " 00:00:00", toDate + " 23:59:59", status);

        if (!"Success".equals(status[0])) {
            System.out.println("! " + status[0]);
            return;
        }

        view.showTransactionTable(transactions, selected.getNumberAccount());
    }

    // =========================================================================
    // Task 2b — Cập nhật thông tin cá nhân
    // =========================================================================

    public void updateInfo() {
        System.out.println("\n+---------------------------------------+");
        System.out.println("|      === UPDATE USER PROFILE ===      |");
        System.out.println("| Press Enter to keep current value     |");
        System.out.println("+---------------------------------------+");

        System.out.print("1. Full Name  [Current: " + currentUser.getUserName() + "]: ");
        String newName = sc.nextLine().trim();

        System.out.print("2. CCCD/ID    : ");
        String newId = sc.nextLine().trim();

        System.out.print("3. Birthday   (yyyy-MM-dd): ");
        String newBirthday = sc.nextLine().trim();

        System.out.print("4. Phone      : ");
        String newPhone = sc.nextLine().trim();

        System.out.print("5. Email      : ");
        String newEmail = sc.nextLine().trim();

        String newPassword = readPassword("6. New login password (Enter to keep): ");

        // Xác thực danh tính bắt buộc trước khi thay đổi
        String oldPassword = readPassword("=> Current password (required): ");
        if (!UserAccoutsDAO.verifyUserPassword(currentUser.getUserId(), oldPassword)) {
            System.out.println("(!) Failed: Incorrect current password.");
            return;
        }

        // Hash mật khẩu mới nếu người dùng có nhập — null thì COALESCE giữ hash cũ
        String newPasswordHash = newPassword.isEmpty()
            ? null
            : org.mindrot.jbcrypt.BCrypt.hashpw(newPassword, org.mindrot.jbcrypt.BCrypt.gensalt());

        String result = UserAccoutsDAO.updateInfo(
            currentUser.getUserId(), newName, newId, newBirthday, newPhone, newEmail, newPasswordHash
        );

        System.out.println("-> " + result);

        // Cập nhật ngay trong bộ nhớ để menu hiển thị thông tin mới
        if ("Success".equalsIgnoreCase(result)) {
            if (!newName.isEmpty())  currentUser.setUserName(newName);
            if (!newPhone.isEmpty()) currentUser.setNumberPhone(newPhone);
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    private boolean hasAnyAccount(BankAccount[] accounts) {
        if (accounts == null) return false;
        for (BankAccount acc : accounts) {
            if (acc != null) return true;
        }
        return false;
    }

    /**
     * Trả về tài khoản tại vị trí người dùng chọn (1-indexed sang 0-indexed).
     * Trả về null nếu chỉ số không hợp lệ.
     */
    private BankAccount pickAccount(BankAccount[] accounts, int choice) {
        int idx = choice - 1;
        if (idx < 0 || idx >= accounts.length || accounts[idx] == null) return null;
        return accounts[idx];
    }

    private int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private double readDouble() {
        try {
            return Double.parseDouble(sc.nextLine().trim());
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
}