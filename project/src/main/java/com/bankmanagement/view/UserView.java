package com.bankmanagement.view;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import com.bankmanagement.controller.UserController;
import com.bankmanagement.dao.UserAccoutsDAO;
import com.bankmanagement.model.BankAccount;
import com.bankmanagement.model.UserAccount;

public class UserView {

    private Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
    // Các phương thức hiển thị menu và thu thập thông tin từ người dùng sẽ được định nghĩa ở đây
    
    // Hiển thi menu chính dành cho người dùng sau khi đăng nhập thành công
    public void showMenu(UserAccount currentUser) {
        while (true) {
            System.out.println("\n+----------------------------------+");
            System.out.printf("|  Hello: %-25s|\n",
                    currentUser.getUserName());
            // Kiểm tra nếu người dùng có tài khoản ngân hàng nào đang hoạt động hay không
            // Lấy tài khoản tiền tại đây vì khi tạo tài khoản mới, số lượng tài khoản có thể thay đổi, nên cần cập nhật lại mỗi lần hiển thị menu
            BankAccount[] bankAccounts = UserAccoutsDAO.getActiveAccountByUserId(currentUser.getUserId());
            boolean hasAccount = bankAccounts != null &&
                    java.util.Arrays.stream(bankAccounts)
                            .anyMatch(acc -> acc != null);
            // Nếu không có tài khoản nào, hiển thị "N/A", nếu có thì liệt kê tất cả tài khoản đang hoạt động của người dùng
            // Đếm số lượng tài khoản đang hoạt động để hiển thị thông tin cho người dùng
            int numberOfAccounts = hasAccount ? (int) java.util.Arrays.stream(bankAccounts)
                    .filter(acc -> acc != null)
                    .count() : 0;
            System.out.println("Number of active bank accounts: " + numberOfAccounts);
            if (!hasAccount) {
                System.out.println("N/A");
            } else {
                for (BankAccount acc : bankAccounts) {
                    if (acc != null) {
                        System.out.printf("|  ACC: %-27s|\n", acc.getNumberAccount());
                    }
                }
            }
            System.out.println("+----------------------------------+");
            System.out.println("|  1. Update information           |");
            System.out.println("|  2. Transfer money               |");
            System.out.println("|  3. View transaction history     |");
            System.out.println("|  4. Check balance                |");
            System.out.println("|  5. Withdraw money               |");
            System.out.println("|  6. Create bank account          |");
            System.out.println("|  0. Logout                       |");
            System.out.println("+----------------------------------+");
            System.out.print("Choose: ");

            int choice = readInt();
            switch (choice) {
                case 1 -> System.out.println("[Update Info - Task 2]");
                case 2 -> System.out.println("[Transfer - Task 4]");
                case 3 -> System.out.println("[History - Task 6]");
                case 4 -> new UserController(currentUser).getBalance(bankAccounts); // Chuyển sang controller để xử lý chức năng xem số dư tài khoản
                case 5 -> System.out.println("[Withdraw - Task 3]");
                case 6 -> new UserController(currentUser).createBankAccount(numberOfAccounts);// Truyền số lượng tài khoản hiện tại để kiểm tra giới hạn khi tạo tài khoản mới
                case 0 -> {
                    System.out.println("Logged out!");
                    return;
                }
                default -> System.out.println("! Invalid choice");
            }
        }
    }

    // Menu cho chức năng 5 - Xem số dư tài khoản
    public void showBalanceMenu(BankAccount[] bankAccounts) {
        //Dùng lại code phần hiển thị số lượng tài khoản và danh sách tài khoản đang hoạt động của người dùng để người dùng chọn tài khoản muốn xem số dư
            boolean hasAccount = bankAccounts != null &&
                    java.util.Arrays.stream(bankAccounts)
                            .anyMatch(acc -> acc != null);
            int numberOfAccounts = hasAccount ? (int) java.util.Arrays.stream(bankAccounts)
                    .filter(acc -> acc != null)
                    .count() : 0;
            System.out.println("Number of active bank accounts: " + numberOfAccounts);
            if (!hasAccount) {
                System.out.println("N/A");
            } else {
                int i = 1;
                System.out.println("|ORDER|  ACCOUNT NUMBER              |");
                for (BankAccount acc : bankAccounts) {
                    if (acc != null) {
                        System.out.printf("|  %d. |ACC: %-27s|\n", i++, acc.getNumberAccount());
                    }
                }
            }
        System.out.println("[Select Money Account to Check Balance - Select by Order]");
        System.out.println("Exit: 0");
        System.out.print("Choose: ");
    }

    // Hiển thị kết quả lấy số dư tài khoản, hoặc thông báo lỗi nếu có
    public void showBalanceResult(double balance, int resultCode) {
        switch (resultCode) {
            case 0 -> System.out.println("Balance: " + balance);
            case 1 -> System.out.println("Account does not exist. Please contact support.");
            case 2 -> System.out.println("Account is Blocked. Please contact support.");
            case 3 -> System.out.println("Incorrect PIN code. Please try again.");
            case 4 -> System.out.println("Server error occurred. Please try again later.");
            default -> System.out.println("Unknown error occurred. Please contact support.");
        }
    }

    // Menu tạo tài khoản ngân hàng mới cho người dùng chức năng 6
    public void createBankAccountMenu(int numberOfAccounts) {
        System.out.println("[Create Bank Account]");
        System.out.println("You will have maximun 10 bank accounts.");
        System.out.println("Presently, you,ve had " + numberOfAccounts + " bank account(s). You can create up to " + (10 - numberOfAccounts) + " more.");
        if (numberOfAccounts >= 10) {
            System.out.println("You have reached the maximum number of bank accounts. Please delete an existing account to create a new one.");
            return;
        }
        System.out.println("Create a new bank account based on random: 1");
        System.out.println("Create a new bank account based on phone number with 2 random digits: 2");
        System.out.println("Exit: 0");
        System.out.print("Choose: ");
    }
    // Hiển thị kết quả tao tài khoản mới, hoặc thông báo lỗi nếu có
    public void showCreateAccountResult(int result, String newAccountNumber) {
        switch (result) {
            case 0 -> System.out.println("Bank account created successfully! Your new account number is: " + newAccountNumber);
            case 1 -> System.out.println("User does not exist. Please contact support.");
            case 2 -> System.out.println("Server error occurred. Please try again later.");
            default -> System.out.println("Account number already exists. Please try again.");
        }
    }   

    // Phương thức thu thập mã PIN mới từ người dùng, đảm bảo định dạng và xác nhận lại
    public String getPinCode() {
        String pinCode = "";
        while (true) {
            pinCode = readPassword("Enter new 6-digit PIN code: ");
            if (pinCode.matches("\\d{6}")) {
                String confirmPin = readPassword("Confirm new PIN code: ");
                if (pinCode.equals(confirmPin)) {
                    return pinCode;
                } else {
                    System.out.println("! PIN codes do not match. Please try again.");
                }
            } else {
                System.out.println("! Invalid PIN format. Please enter exactly 6 digits.");
            }
        }
    }
    // Phương thức đọc lựa chọn số nguyên từ người dùng, xử lý lỗi định dạng
    private int readInt() {
        try {
            return Integer.parseInt(
                    sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    // Phương thức đọc mật khẩu từ console, nếu không có console (IDE), sẽ đọc như bình thường
    private String readPassword(String prompt) {
        if (System.console() != null) {
            char[] pwd = System.console().readPassword(prompt);
            return pwd != null ? new String(pwd).trim() : "";
        } else {
            System.out.print(prompt);
            return sc.nextLine().trim();
        }
    }
}
