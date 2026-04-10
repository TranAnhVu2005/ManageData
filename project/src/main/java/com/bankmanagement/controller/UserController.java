package com.bankmanagement.controller;

import com.bankmanagement.dao.UserAccountsDAO;
import com.bankmanagement.model.BankAccount;
import com.bankmanagement.model.Cards;
import com.bankmanagement.model.UserAccount;
import com.bankmanagement.view.UserView;
import com.bankmanagement.function;

import java.util.List;
import java.util.Map;

/**
 * Xử lý nghiệp vụ dành cho người dùng Client.
 * Đã refactor sang chuẩn MVC.
 */
public class UserController {

    private final UserAccount currentUser;
    private final UserView view = new UserView();

    public UserController(UserAccount user) {
        this.currentUser = user;
    }

    public void showMenu() {
        while (true) {
            List<BankAccount> accounts = UserAccountsDAO.getActiveAccountByUserId(currentUser.getUserId());
            view.showMenu(currentUser, accounts);
            int choice = view.getChoice();

            switch (choice) {
                case 1 -> updateInfo();
                case 2 -> transferMoney(accounts);
                case 3 -> checkTransaction(accounts);
                case 4 -> getBalance(accounts);
                case 5 -> withdrawMoney();
                case 6 -> createBankAccount(accounts.size());
                case 7 -> viewProfile();
                case 8 -> searchTransactionByDate(accounts);
                case 9 -> changePin(accounts);
                case 10 -> createCard(accounts);
                case 0 -> {
                    view.showMessage("Logging out...");
                    return;
                }
                default -> view.showError("Invalid choice!");
            }
        }
    }

    private void createBankAccount(int count) {
        if (count >= 10) {
            view.showError("Maximum 10 accounts reached.");
            return;
        }

        view.showMessage("Creating new account...");
        String newAcc;
        // Logic: random or phone based
        int sub = view.getAccountGenerationChoice();
        if (sub == 1) {
            newAcc = function.generateStringRandom(10, "ACCOUNTBANK", "numberAccount", null);
        } else if (sub == 2) {
            newAcc = currentUser.getNumberPhone().substring(2) + function.generateStringRandom(2, null, null, null);
        } else
            return;

        String pin = view.enterPin("Set new 6-digit PIN");
        if (!pin.matches("\\d{6}")) {
            view.showError("PIN must be 6 digits!");
            return;
        }

        String pinHash = org.mindrot.jbcrypt.BCrypt.hashpw(pin, org.mindrot.jbcrypt.BCrypt.gensalt());
        int res = UserAccountsDAO.createBankAccount(newAcc, pinHash, currentUser.getUserId());
        if (res == 0)
            view.showSuccess("Account created: " + newAcc);
        else
            view.showError("Failed to create account.");
    }

    private void withdrawMoney() {
        List<Cards> cards = UserAccountsDAO.getCardsByUserId(currentUser.getUserId());
        if (cards.isEmpty()) {
            view.showError("No active cards found.");
            return;
        }

        int idx = view.selectCard(cards, "Withdraw Money");
        if (idx == -1)
            return;
        Cards selCard = cards.get(idx);

        String[] input = null;
        String pin = null;
        double amount;
        while (true) {
            input = view.getWithdrawInput(selCard.getCardNumber()); // pin, amount
            pin = input[0];
            if (!pin.matches("\\d{6}")) {
                view.showError("PIN must be 6 digits.");
                continue;
            }
            if (input[1].isEmpty()) {
                view.showError("Amount cannot be empty.");
                continue;
            }
            if (!UserAccountsDAO.verifyCardPin(selCard.getCardNumber(), pin)) {
                view.showError("Incorrect Card PIN.");
                continue;
            }
            try {
                amount = Double.parseDouble(input[1]);
                if (amount <= 0) {
                    view.showError("Amount must be positive.");
                    continue;
                }
            } catch (NumberFormatException e) {
                view.showError("Invalid amount format.");
                continue;
            }
            break;
        }

        String tid = function.generateStringRandom(29, "BANKTRANSACTIONS", "transactionId", "W");
        int res = UserAccountsDAO.withdrawMoney(selCard.getCardNumber(), amount, tid);
        if (res == 0)
            view.showSuccess("Withdrawal successful! Please collect your money. Then check your balance or transactions for details.");
        else
            view.showError("Withdrawal failed. Error code: " + res);
    }

    private void getBalance(List<BankAccount> accounts) {
        int idx = view.selectAccount(accounts, "Check Balance");
        if (idx == -1)
            return;

        BankAccount sel = accounts.get(idx);
        String pin = view.enterPin("Enter Account PIN");
        if (UserAccountsDAO.verifyAccountPin(sel.getNumberAccount(), pin)) {
            Map<String, Object> res = UserAccountsDAO.getBalance(sel.getNumberAccount());
            view.showSuccess("Current Balance: " + String.format("%,.2f", (Double) res.get("balance")) + " VND");
        } else {
            view.showError("Incorrect PIN.");
        }
    }

    private void transferMoney(List<BankAccount> accounts) {
        int idx = view.selectAccount(accounts, "Transfer Money");
        if (idx == -1)
            return;

        BankAccount source = accounts.get(idx);
        String[] input = view.getTransferInput(); // dest, amount
        String dest = input[0];
        double amount;
        try {
            amount = Double.parseDouble(input[1]);
        } catch (Exception e) {
            view.showError("Invalid amount.");
            return;
        }

        String pin = view.enterPin("Confirm with PIN");
        if (UserAccountsDAO.verifyAccountPin(source.getNumberAccount(), pin)) {
            String res = UserAccountsDAO.transferMoney(source.getNumberAccount(), dest, amount);
            if ("Success".equals(res))
                view.showSuccess("Transfer completed!");
            else
                view.showError(res);
        } else {
            view.showError("Incorrect PIN.");
        }
    }

    private void checkTransaction(List<BankAccount> accounts) {
        int idx = view.selectAccount(accounts, "Transaction History");
        if (idx == -1)
            return;

        BankAccount sel = accounts.get(idx);
        String[] status = new String[1];
        List<Map<String, Object>> tx = UserAccountsDAO.checkTransaction(sel.getNumberAccount(), status);
        view.showTransactionTable(tx, sel.getNumberAccount());
    }

    private void searchTransactionByDate(List<BankAccount> accounts) {
        int idx = view.selectAccount(accounts, "Search by Date");
        if (idx == -1)
            return;

        BankAccount sel = accounts.get(idx);
        String[] range = view.getDateRange();
        String[] status = new String[1];
        List<Map<String, Object>> tx = UserAccountsDAO.searchTransactionByDate(
                sel.getNumberAccount(), range[0] + " 00:00:00", range[1] + " 23:59:59", status);
        view.showTransactionTable(tx, sel.getNumberAccount());
    }

    private void changePin(List<BankAccount> accounts) {
        int idx = view.selectAccount(accounts, "Change PIN");
        if (idx == -1)
            return;

        BankAccount sel = accounts.get(idx);
        String oldPin = view.enterPin("Current PIN");
        if (!UserAccountsDAO.verifyAccountPin(sel.getNumberAccount(), oldPin)) {
            view.showError("Incorrect current PIN.");
            return;
        }

        String newPin = view.enterPin("New 6-digit PIN");
        if (!newPin.matches("\\d{6}")) {
            view.showError("PIN must be 6 digits.");
            return;
        }

        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(newPin, org.mindrot.jbcrypt.BCrypt.gensalt());
        String res = UserAccountsDAO.changeAccountPin(sel.getNumberAccount(), hash);
        if ("Success".equals(res))
            view.showSuccess("PIN changed successfully.");
        else
            view.showError(res);
    }

    private void viewProfile() {
        Map<String, String> prof = UserAccountsDAO.getProfile(currentUser.getUserId());
        if (prof != null)
            view.displayProfile(prof);
    }

    private void updateInfo() {
        String[] input = view.getUpdateInfoInput();
        String hash = null;
        if (!input[3].isEmpty()) {
            hash = org.mindrot.jbcrypt.BCrypt.hashpw(input[3], org.mindrot.jbcrypt.BCrypt.gensalt());
        }

        String res = UserAccountsDAO.updateInfo(currentUser.getUserId(), input[0], null, null, input[1], input[2],
                hash);
        if ("Success".equals(res)) {
            view.showSuccess("Profile updated.");
            if (!input[0].isEmpty())
                currentUser.setUserName(input[0]);
        } else {
            view.showError(res);
        }
    }

    private void createCard(List<BankAccount> accounts) {
        int idx = view.selectAccount(accounts, "Create Card");
        if (idx == -1)
            return;

        BankAccount sel = accounts.get(idx);
        String pin = null;
        while (true) {
            pin = view.enterPin("Set 6-digit Bank Account PIN");
            if (pin.matches("\\d{6}"))
                break;
            view.showError("PIN must be 6 digits.");
        }

        String cardNumber = function.generateStringRandom(16, "CARDS", "cardNumber", null);
        String cardPin = null;
        while (true) {
            cardPin = view.enterPin("Set 6-digit Card PIN");
            if (cardPin.matches("\\d{6}"))
                break;
            view.showError("PIN must be 6 digits.");
        }
        String ccv = function.generateStringRandom(3, null, null, null);
        int res = UserAccountsDAO.createCard(cardNumber, sel.getNumberAccount(), cardPin, ccv);
        if (res == 0)
            view.showSuccess("Card created: " + cardNumber);
        else
            view.showError("Failed to create card." + res);
    }
}