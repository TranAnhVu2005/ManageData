package com.bankmanagement.controller;

import com.bankmanagement.model.BankAccount;
import com.bankmanagement.model.UserAccount;
import com.bankmanagement.view.UserView;
import com.bankmanagement.dao.UserAccoutsDAO;
import com.bankmanagement.function;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

/**
 * Controller xử lý các tính năng dành cho người dùng thông thường (Client).
 */
public class UserController {

    private UserAccount currentUser;
    private UserView view = new UserView();

    public UserController(UserAccount user) {
        this.currentUser = user;
    }

    private Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);

    public void createBankAccount(int numberOfAccounts) {
        view.createBankAccountMenu(numberOfAccounts);
        int choice = readInt();
        switch (choice) {
            case 1 -> createBankAccountRandom();
            case 2 -> createBankAccountWithPhone();
            case 0 -> {
                System.out.println("Exit create bank account!");
                return;
            }
            default -> System.out.println("! Invalid choice");
        }
    }

    public void getBalance(BankAccount[] bankAccounts) {
        while (true) {
            view.showBalanceMenu(bankAccounts);
            int choice = readInt();
            if (choice == 0) {
                System.out.println("Exit check balance!");
                return;
            }
            if (choice < 1 || choice > bankAccounts.length || bankAccounts[choice - 1] == null) {
                System.out.println("! Invalid choice. Enter a number between 1 and " + bankAccounts.length);
                continue;
            }
            System.out.println(bankAccounts[choice - 1].getPinCodeHash());
            String pinCodeString = view.getPinCode();
            if (pinCodeString == null || pinCodeString.trim().isEmpty()) {
                System.out.println("! PIN code cannot be empty.");
                continue;
            }

            System.out.println(bankAccounts[choice - 1].getPinCodeHash());
            // Cách 1: Hiển thị số dư trực tiếp từ mảng đã lấy ban đầu (cách này có thể
            // không cập nhật số dư mới nhất nếu có giao dịch vừa xảy ra)
            // Nên dùng thay vì phải truy vấn đến CSDL
            // BankAccount selectedAccount = bankAccounts[choice - 1];
            // System.out.println("Balance of account " + selectedAccount.getNumberAccount()
            // + ": " + selectedAccount.getBalance());
            // Cách 2: Để học cách sử dụng thủ tục được lưu trữ trong CSDL để lấy số dư mới
            // nhất, sẽ gọi đến DAO để thực hiện truy vấn lại số dư của tài khoản đã chọn
            // So sánh Pin 2 lần gây dư thừa, nhưng vì để học cách gọi thủ tục lưu trữ trong
            // CSDL nên sẽ giữ nguyên cách này, nếu muốn tối ưu có thể sửa lại thành 1 lần
            // so sánh pin rồi sau đó mới gọi thủ tục lấy số dư
            if (org.mindrot.jbcrypt.BCrypt.checkpw(pinCodeString, bankAccounts[choice - 1].getPinCodeHash())) {
                Map<String, Object> balanceResult = UserAccoutsDAO.getBalance(
                        bankAccounts[choice - 1].getNumberAccount(),
                        bankAccounts[choice - 1].getPinCodeHash());
                double balance = (double) balanceResult.get("balance");
                int resultCode = (int) balanceResult.get("resultCode");
                view.showBalanceResult(balance, resultCode);
            } else {
                System.out.println("! Incorrect PIN code");
            }
        }
    }

    private int readInt() {
        try {
            return Integer.parseInt(
                    sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void createBankAccountRandom() {
        String newAccountNumber = function.generateStringRandom(10);
        String newPinCode = view.getPinCode();
        String pinCodeHash = org.mindrot.jbcrypt.BCrypt.hashpw(newPinCode, org.mindrot.jbcrypt.BCrypt.gensalt());

        int result = UserAccoutsDAO.createBankAccount(newAccountNumber, pinCodeHash, currentUser.getUserId());
        view.showCreateAccountResult(result, newAccountNumber);
    }

    private void createBankAccountWithPhone() {
        String newAccountNumber = currentUser.getNumberPhone() + function.generateStringRandom(2);
        String newPinCode = view.getPinCode();
        String pinCodeHash = org.mindrot.jbcrypt.BCrypt.hashpw(newPinCode, org.mindrot.jbcrypt.BCrypt.gensalt());

        int result = UserAccoutsDAO.createBankAccount(newAccountNumber, pinCodeHash, currentUser.getUserId());
        view.showCreateAccountResult(result, newAccountNumber);
    }

}