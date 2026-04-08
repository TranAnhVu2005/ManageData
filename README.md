# Bank Account Management System / Hệ Thống Quản Lý Tài Khoản Ngân Hàng

> **Language / Ngôn ngữ:** [English](#english-version) | [Tiếng Việt](#phiên-bản-tiếng-việt)

---

# English Version

## Overview

A Java console application that simulates core banking operations. The system connects to a MySQL database and executes business logic through Stored Procedures.

Built for the CT476 - Database Management course at Can Tho University.

**Team:**
- Nguyen Huy Loi
- Tran Anh Vu — B2306603

---

## Architecture

The project follows a standard MVC pattern adapted for console applications.

```
Main
 └── AuthController           Entry point for login/register flow
       ├── UserController      Handles all Client-side features
       └── AdminController     Handles all Staff-side features

Each Controller talks to:
  └── DAO layer               Calls Stored Procedures via JDBC
        └── MySQL Database    Executes the actual SQL logic
```

**Data flow for a typical operation (e.g. Check Balance):**

```
User enters PIN
  → UserController calls UserAccoutsDAO.verifyAccountPin()
      → Java fetches pinCodeHash from DB, runs BCrypt.checkpw()
          → If correct: calls UserAccoutsDAO.getBalance()
              → Calls Stored Procedure checkBalance(accountNumber)
                  → Returns balance and result code
                      → UserView prints the formatted result
```

The key design decision: **Stored Procedures do not verify passwords or PINs.** All authentication happens in Java using BCrypt. Procedures only execute the business logic (update, transfer, query).

---

## Security Model

All passwords and PINs are stored as BCrypt hashes with a random salt. There is no plain-text credential anywhere in the database.

**Registration / Password change:**
```
raw_password → BCrypt.hashpw(raw, gensalt()) → stored in passWordHash column
```

**Verification at login or PIN check:**
```
raw_input + storedHash → BCrypt.checkpw(raw_input, storedHash) → true / false
```

This means Stored Procedures only receive the hash of the *new* value when updating. They never receive the old password or PIN to compare against.

---

## Project Structure

```
ManageData/
├── README.md
├── sql/
│   └── script.sql                  Full schema + all 16 Stored Procedures
└── project/
    └── src/main/java/com/bankmanagement/
        ├── Main.java
        ├── function.java               Utility: random string generator
        ├── config/
        │   └── dbConnection.java       MySQL JDBC connection setup
        ├── model/
        │   ├── UserAccount.java        Maps to USERACCOUNTS table
        │   ├── BankAccount.java        Maps to ACCOUNTBANK table
        │   ├── BankTransactions.java   Maps to BANKTRANSACTIONS table
        │   ├── Cards.java              Maps to CARDS table
        │   └── TypeOfTransaction.java  Maps to TYPEOFTRANSACTION table
        ├── dao/
        │   ├── AuthDAO.java            login(), register()
        │   └── UserAccoutsDAO.java     All other DB operations (14 methods)
        ├── controller/
        │   ├── AuthController.java     Login/register flow + role-based routing
        │   ├── UserController.java     Client features (Tasks 2b, 4, 5, 6, 9, 10, 11, 12)
        │   └── AdminController.java    Staff features (Tasks 8, 13, 14, 15, 16)
        └── view/
            ├── LoginView.java          Login/register console UI
            └── UserView.java           Client console UI (main menu + all sub-screens)
```

---

## Database Schema

| Table | Purpose |
|-------|---------|
| `USERACCOUNTS` | System accounts: login credentials, personal info, role (Client/Staff) |
| `ACCOUNTBANK` | Bank accounts linked to users: balance, PIN hash, state (Active/Blocked) |
| `CARDS` | Physical cards linked to bank accounts |
| `BANKTRANSACTIONS` | Transaction ledger: every transfer, deposit, withdraw is recorded here |
| `TYPEOFTRANSACTION` | Reference codes for transaction types (e.g. D001 = Deposit) |

A user in `USERACCOUNTS` can have multiple bank accounts in `ACCOUNTBANK`. Every money movement creates a record in `BANKTRANSACTIONS`.

---

## Feature List

### Authentication (no role required)

**Task 1 — Login**
1. User enters phone number and password.
2. `AuthDAO.login()` fetches the user record by phone number, then BCrypt verifies the password hash.
3. On success, `AuthController` reads the `roleUser` field and routes to `UserController` (Client) or `AdminController` (Staff).

**Task 2 — Register**
1. User fills in: full name, 12-digit ID card, date of birth, phone number, email, password, confirm password.
2. `AuthController` validates all fields (format, length, match) before proceeding.
3. Password is hashed with BCrypt and passed to the `createUserAccount` Stored Procedure.
4. `userID` is auto-generated as `"U" + last 6 digits of current timestamp`.

---

### Client Features (menu options 1–9)

The main menu reloads the bank account list from the database before each render to reflect any changes made in the previous action.

**Task 2b — Update Personal Information (option 1)**
1. User fills in any fields they want to change. Pressing Enter skips a field and keeps the current value.
2. User enters current password — required to authorize any change.
3. Password is verified with BCrypt at the Java layer. The Stored Procedure is not called if verification fails.
4. If correct, `updateInfo` Stored Procedure runs `UPDATE USERACCOUNTS SET ... WHERE userID = ?`.
5. Fields left blank are passed as `NULL`. The procedure uses `COALESCE(new_value, current_value)` to preserve unchanged fields.
6. On success, the in-memory `currentUser` object is updated so the menu immediately reflects the new name and phone number.

**Task 4 — Transfer Money (option 2)**
1. User selects the source bank account from their list.
2. User enters the destination account number and the amount.
3. User enters their PIN — verified with BCrypt at the Java layer.
4. If PIN is correct, `transferMoney` Stored Procedure runs the transaction: debit source account, credit destination account, and insert a record into `BANKTRANSACTIONS`.
5. The procedure uses `START TRANSACTION / COMMIT / ROLLBACK` — if any SQL error occurs, all changes are automatically rolled back.

**Task 6 — Transaction History (option 3)**
1. User selects a bank account.
2. `checkTransaction` Stored Procedure returns all rows in `BANKTRANSACTIONS` where the account is either the sender (`numberAccount`) or the receiver (`destinationAccount`).
3. Results are displayed as a formatted table. Each row shows direction (IN / OUT) and the counterpart account number.

**Task 5 — Check Balance (option 4)**
1. User selects a bank account.
2. User enters PIN — verified with BCrypt at the Java layer.
3. `checkBalance` Stored Procedure returns the current balance and a result code.
4. Result codes: `0` = success, `1` = account not found, `3` = account blocked, `4` = server error.

**Task 3 — Withdraw Money (option 5)**
- Assigned to Loi. The `withDrawMoney` Stored Procedure is complete. The Java layer is pending.

**Task 9 — Create Bank Account (option 6)**
1. User chooses between two account number formats:
   - Fully random (10 digits)
   - Based on their phone number (phone + 2 random digits, easier to remember)
2. User enters and confirms a new 6-digit PIN. The format is validated before accepting.
3. PIN is hashed with BCrypt and passed to `createBankAccount` Stored Procedure.
4. Result codes: `0` = success, `1` = user not found, `2` = server error, `3` = duplicate account number (reattempt automatically).

**Task 11 — View Profile (option 7)**
- Fetches and displays the current user's full profile directly from `USERACCOUNTS` via a `SELECT` query. No Stored Procedure is needed for a simple read.

**Task 12 — Search Transactions by Date (option 8)**
1. User selects a bank account.
2. User enters a date range in `yyyy-MM-dd` format (from date, to date).
3. `searchTransactionByDate` Stored Procedure filters `BANKTRANSACTIONS` using `BETWEEN from AND to`, where time bounds are extended to `00:00:00` and `23:59:59` to include the full day.
4. Results use the same table format as the transaction history view.

**Task 10 — Change Bank Account PIN (option 9)**
1. User selects a bank account.
2. User enters the current PIN — verified with BCrypt at the Java layer.
3. User enters and confirms the new 6-digit PIN.
4. New PIN is hashed with BCrypt and passed to `changeAccountPin` Stored Procedure.
5. The procedure checks that the account is `Active` before updating the `pinCodeHash` field.

---

### Staff Features (menu options 1–7)

Options 1 and 2 (Create Bank Account, Deposit Money) are placeholders for Loi's implementation.

**Task 8 — Block Account (option 3)**
1. Staff enters a bank account number.
2. `deleteAccount` Stored Procedure checks the account state and balance.
   - If balance > 0: rejects the request and returns the remaining balance in the error message.
   - If balance = 0 and state = Active: sets state to `Blocked`.

**Task 16 — Unblock Account (option 4)**
1. Staff enters a bank account number.
2. `unblockAccount` Stored Procedure checks the current state.
   - If state = Blocked: sets state back to `Active`.
   - If state = Active: returns an error saying the account is already active.

**Task 13 — View All Customers (option 5)**
- Fetches all rows from `USERACCOUNTS`, ordered by role then by name.
- Displayed as a table: UserID, Full Name, Phone, Role, Birthday.

**Task 14 — Search Customer (option 6)**
1. Staff enters a keyword (full or partial phone number or ID card number).
2. `searchUser` Stored Procedure runs a `LIKE` search on both `numberPhone` and `ID` columns simultaneously.
3. Results use the same table format as option 5.

**Task 15 — View All Transactions (option 7)**
- `getAllTransactions` Stored Procedure returns all rows from `BANKTRANSACTIONS`, ordered by date descending.
- Displayed as a table: TransactionID, Date, Amount, Status, From Account, To Account.

---

## How to Run

**Requirements:** Java 17+, MySQL 8.0+, Maven 3.x

```bash
# Step 1: Initialize the database
mysql -u root -p < sql/script.sql

# Step 2: Configure the connection
# Open: project/src/main/java/com/bankmanagement/config/dbConnection.java
# Update the JDBC URL, username, and password

# Step 3: Run
cd project
mvn exec:java
```

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| `mysql-connector-j` | 9.x | JDBC driver for MySQL |
| `jbcrypt` | 0.4 | BCrypt hashing for passwords and PINs |

---

## Work Division

**Nguyen Huy Loi** — Database schema, core infrastructure
- Schema design for all 5 tables
- Stored Procedures: `createUserAccount`, `createBankAccount`, `withDrawMoney`, `depositMoney`, `checkBalance`
- Java: login, register, create bank account, check balance
- Pending: Java layer for withdraw (Task 3) and deposit (Task 7)

**Tran Anh Vu** — Account management, money movement, admin operations
- Stored Procedures: `updateInfo`, `transferMoney`, `checkTransaction`, `deleteAccount`, `changeAccountPin`, `searchTransactionByDate`, `searchUser`, `getAllTransactions`, `unblockAccount`
- Java: update info, transfer money, transaction history, change PIN, view profile, search by date, block/unblock account, view all customers, search customer, view all transactions
- Security architecture: BCrypt authentication pattern applied across all features

---
---

# Phiên Bản Tiếng Việt

## Tổng Quan

Ứng dụng console Java mô phỏng các nghiệp vụ cơ bản của ngân hàng. Hệ thống kết nối MySQL và thực thi logic nghiệp vụ thông qua Stored Procedure.

Được xây dựng cho môn học CT476 - Quản trị Cơ sở Dữ liệu, Đại học Cần Thơ.

**Nhóm thực hiện:**
- Nguyễn Huy Lợi
- Trần Anh Vũ — B2306603

---

## Kiến Trúc Hệ Thống

Dự án theo mô hình MVC (Model - View - Controller) cho ứng dụng console.

```
Main
 └── AuthController           Điểm vào cho luồng đăng nhập/đăng ký
       ├── UserController      Xử lý toàn bộ chức năng phía Client
       └── AdminController     Xử lý toàn bộ chức năng phía Staff

Mỗi Controller kết nối xuống:
  └── Tầng DAO                Gọi Stored Procedure thông qua JDBC
        └── MySQL Database    Thực thi truy vấn SQL thực sự
```

**Luồng dữ liệu cho một thao tác điển hình (ví dụ: Kiểm tra số dư):**

```
Người dùng nhập mã PIN
  → UserController gọi UserAccoutsDAO.verifyAccountPin()
      → Java lấy pinCodeHash từ DB, chạy BCrypt.checkpw()
          → Nếu đúng: gọi UserAccoutsDAO.getBalance()
              → Gọi Stored Procedure checkBalance(accountNumber)
                  → Trả về số dư và mã kết quả
                      → UserView in kết quả ra màn hình
```

Quyết định thiết kế cốt lõi: **Stored Procedure không xác thực mật khẩu hay mã PIN.** Toàn bộ xác thực diễn ra ở tầng Java bằng BCrypt. Procedure chỉ lo thực thi nghiệp vụ (update, chuyển tiền, truy vấn).

---

## Cơ Chế Bảo Mật

Mọi mật khẩu và mã PIN được lưu dưới dạng BCrypt hash với salt ngẫu nhiên. Không có thông tin nhạy cảm nào được lưu dạng plain-text trong database.

**Khi đăng ký hoặc đổi mật khẩu:**
```
mật_khẩu_gốc → BCrypt.hashpw(raw, gensalt()) → lưu vào cột passWordHash
```

**Khi xác thực đăng nhập hoặc kiểm tra PIN:**
```
input_gốc + storedHash → BCrypt.checkpw(input_gốc, storedHash) → true / false
```

Điều này có nghĩa: Stored Procedure chỉ nhận hash của giá trị *mới* khi cập nhật. Chúng không bao giờ nhận mật khẩu/PIN cũ để so sánh.

---

## Cấu Trúc Dự Án

```
ManageData/
├── README.md
├── sql/
│   └── script.sql                  Toàn bộ schema + 16 Stored Procedure
└── project/
    └── src/main/java/com/bankmanagement/
        ├── Main.java
        ├── function.java               Tiện ích: tạo chuỗi ngẫu nhiên
        ├── config/
        │   └── dbConnection.java       Cấu hình kết nối MySQL JDBC
        ├── model/
        │   ├── UserAccount.java        Ánh xạ bảng USERACCOUNTS
        │   ├── BankAccount.java        Ánh xạ bảng ACCOUNTBANK
        │   ├── BankTransactions.java   Ánh xạ bảng BANKTRANSACTIONS
        │   ├── Cards.java              Ánh xạ bảng CARDS
        │   └── TypeOfTransaction.java  Ánh xạ bảng TYPEOFTRANSACTION
        ├── dao/
        │   ├── AuthDAO.java            login(), register()
        │   └── UserAccoutsDAO.java     Toàn bộ nghiệp vụ còn lại (14 phương thức)
        ├── controller/
        │   ├── AuthController.java     Luồng đăng nhập/đăng ký + phân quyền
        │   ├── UserController.java     Chức năng Client (Task 2b, 4, 5, 6, 9, 10, 11, 12)
        │   └── AdminController.java    Chức năng Staff (Task 8, 13, 14, 15, 16)
        └── view/
            ├── LoginView.java          Giao diện console đăng nhập/đăng ký
            └── UserView.java           Giao diện console Client (menu + tất cả màn hình con)
```

---

## Sơ Đồ Cơ Sở Dữ Liệu

| Bảng | Vai trò |
|------|---------|
| `USERACCOUNTS` | Tài khoản hệ thống: thông tin đăng nhập, hồ sơ cá nhân, phân quyền (Client/Staff) |
| `ACCOUNTBANK` | Tài khoản ngân hàng gắn với người dùng: số dư, hash PIN, trạng thái (Active/Blocked) |
| `CARDS` | Thẻ vật lý gắn với tài khoản ngân hàng |
| `BANKTRANSACTIONS` | Sổ cái giao dịch: mọi chuyển khoản, nạp tiền, rút tiền đều được ghi lại đây |
| `TYPEOFTRANSACTION` | Mã tham chiếu loại giao dịch (ví dụ: D001 = Nạp tiền) |

Một người dùng trong `USERACCOUNTS` có thể có nhiều tài khoản ngân hàng trong `ACCOUNTBANK`. Mọi giao dịch tiền tệ đều tạo bản ghi trong `BANKTRANSACTIONS`.

---

## Danh Sách 16 Chức Năng

### Xác thực (không yêu cầu phân quyền)

**Task 1 — Đăng nhập**
1. Người dùng nhập số điện thoại và mật khẩu.
2. `AuthDAO.login()` lấy bản ghi người dùng theo số điện thoại, sau đó BCrypt xác thực hash mật khẩu.
3. Nếu thành công, `AuthController` đọc trường `roleUser` và điều hướng đến `UserController` (Client) hoặc `AdminController` (Staff).

**Task 2 — Đăng ký**
1. Người dùng điền: họ tên, CCCD 12 số, ngày sinh, số điện thoại, email, mật khẩu, xác nhận mật khẩu.
2. `AuthController` kiểm tra định dạng toàn bộ các trường trước khi tiến hành.
3. Mật khẩu được hash bằng BCrypt và truyền vào Stored Procedure `createUserAccount`.
4. `userID` được sinh tự động theo công thức `"U" + 6 chữ số cuối của timestamp hiện tại`.

---

### Chức Năng Client (lựa chọn menu 1–9)

Menu chính tải lại danh sách tài khoản ngân hàng từ database trước mỗi lần hiển thị để phản ánh mọi thay đổi từ thao tác trước.

**Task 2b — Cập nhật thông tin cá nhân (lựa chọn 1)**
1. Người dùng điền vào trường muốn thay đổi. Nhấn Enter để bỏ qua và giữ nguyên giá trị cũ.
2. Người dùng nhập mật khẩu hiện tại — bắt buộc để phép bất kỳ thay đổi nào được thực hiện.
3. Mật khẩu được xác thực bằng BCrypt tại tầng Java. Stored Procedure không được gọi nếu xác thực thất bại.
4. Nếu đúng, Stored Procedure `updateInfo` chạy `UPDATE USERACCOUNTS SET ... WHERE userID = ?`.
5. Trường để trống được truyền vào dưới dạng `NULL`. Procedure dùng `COALESCE(giá_trị_mới, giá_trị_hiện_tại)` để giữ lại các trường không thay đổi.
6. Sau khi thành công, object `currentUser` trong bộ nhớ được cập nhật ngay để menu hiển thị tên và số điện thoại mới.

**Task 4 — Chuyển tiền (lựa chọn 2)**
1. Người dùng chọn tài khoản nguồn từ danh sách của mình.
2. Người dùng nhập số tài khoản đích và số tiền chuyển.
3. Người dùng nhập mã PIN — được xác thực bằng BCrypt tại tầng Java.
4. Nếu PIN đúng, Stored Procedure `transferMoney` thực hiện giao dịch: trừ tiền tài khoản nguồn, cộng tiền tài khoản đích, ghi bản ghi vào `BANKTRANSACTIONS`.
5. Procedure dùng `START TRANSACTION / COMMIT / ROLLBACK` — nếu có lỗi SQL xảy ra, toàn bộ thay đổi sẽ được tự động hoàn tác.

**Task 6 — Lịch sử giao dịch (lựa chọn 3)**
1. Người dùng chọn một tài khoản ngân hàng.
2. Stored Procedure `checkTransaction` trả về toàn bộ bản ghi trong `BANKTRANSACTIONS` mà tài khoản đó là bên gửi (`numberAccount`) hoặc bên nhận (`destinationAccount`).
3. Kết quả hiển thị dạng bảng. Mỗi dòng có chiều giao dịch (IN / OUT) và số tài khoản đối phương.

**Task 5 — Kiểm tra số dư (lựa chọn 4)**
1. Người dùng chọn một tài khoản ngân hàng.
2. Người dùng nhập mã PIN — được xác thực bằng BCrypt tại tầng Java.
3. Stored Procedure `checkBalance` trả về số dư hiện tại và mã kết quả.
4. Mã kết quả: `0` = thành công, `1` = không tìm thấy tài khoản, `3` = tài khoản bị khóa, `4` = lỗi server.

**Task 3 — Rút tiền (lựa chọn 5)**
- Do Lợi đảm nhận. Stored Procedure `withDrawMoney` đã hoàn chỉnh. Phần Java đang chờ.

**Task 9 — Tạo tài khoản ngân hàng (lựa chọn 6)**
1. Người dùng chọn một trong hai định dạng số tài khoản:
   - Hoàn toàn ngẫu nhiên (10 chữ số)
   - Dựa trên số điện thoại (SĐT + 2 chữ số ngẫu nhiên, dễ nhớ hơn)
2. Người dùng nhập và xác nhận mã PIN 6 chữ số. Định dạng được kiểm tra trước khi chấp nhận.
3. PIN được hash bằng BCrypt và truyền vào Stored Procedure `createBankAccount`.
4. Mã kết quả: `0` = thành công, `1` = không tìm thấy user, `2` = lỗi server, `3` = trùng số tài khoản.

**Task 11 — Xem hồ sơ cá nhân (lựa chọn 7)**
- Lấy và hiển thị toàn bộ thông tin hồ sơ người dùng hiện tại từ bảng `USERACCOUNTS` bằng câu `SELECT` thông thường. Không cần Stored Procedure cho một thao tác đọc đơn giản như vậy.

**Task 12 — Tìm kiếm giao dịch theo ngày (lựa chọn 8)**
1. Người dùng chọn một tài khoản ngân hàng.
2. Người dùng nhập khoảng thời gian theo định dạng `yyyy-MM-dd` (từ ngày, đến ngày).
3. Stored Procedure `searchTransactionByDate` lọc `BANKTRANSACTIONS` bằng điều kiện `BETWEEN from AND to`. Giờ được mở rộng từ `00:00:00` đến `23:59:59` để bao trùm cả ngày cuối.
4. Kết quả dùng cùng định dạng bảng như màn hình lịch sử giao dịch.

**Task 10 — Đổi mã PIN tài khoản ngân hàng (lựa chọn 9)**
1. Người dùng chọn một tài khoản ngân hàng.
2. Người dùng nhập mã PIN hiện tại — được xác thực bằng BCrypt tại tầng Java.
3. Người dùng nhập và xác nhận mã PIN mới 6 chữ số.
4. PIN mới được hash bằng BCrypt và truyền vào Stored Procedure `changeAccountPin`.
5. Procedure kiểm tra tài khoản đang ở trạng thái `Active` trước khi cập nhật trường `pinCodeHash`.

---

### Chức Năng Staff (lựa chọn menu 1–7)

Lựa chọn 1 và 2 (Tạo tài khoản, Nạp tiền) là placeholder cho phần của Lợi.

**Task 8 — Khóa tài khoản ngân hàng (lựa chọn 3)**
1. Staff nhập số tài khoản ngân hàng cần khóa.
2. Stored Procedure `deleteAccount` kiểm tra trạng thái và số dư tài khoản.
   - Nếu số dư > 0: từ chối và trả về thông báo kèm số dư còn lại.
   - Nếu số dư = 0 và đang Active: chuyển trạng thái sang `Blocked`.

**Task 16 — Mở khóa tài khoản ngân hàng (lựa chọn 4)**
1. Staff nhập số tài khoản ngân hàng cần mở khóa.
2. Stored Procedure `unblockAccount` kiểm tra trạng thái hiện tại.
   - Nếu đang Blocked: chuyển trạng thái về `Active`.
   - Nếu đang Active: trả về thông báo lỗi tài khoản đã hoạt động.

**Task 13 — Xem danh sách toàn bộ khách hàng (lựa chọn 5)**
- Lấy toàn bộ bản ghi từ `USERACCOUNTS`, sắp xếp theo role rồi đến tên.
- Hiển thị dạng bảng: UserID, Họ tên, Số điện thoại, Phân quyền, Ngày sinh.

**Task 14 — Tìm kiếm khách hàng (lựa chọn 6)**
1. Staff nhập từ khóa tìm kiếm (số điện thoại hoặc số CCCD, đầy đủ hoặc một phần).
2. Stored Procedure `searchUser` thực hiện tìm kiếm `LIKE` đồng thời trên cả hai cột `numberPhone` và `ID`.
3. Kết quả dùng cùng định dạng bảng như lựa chọn 5.

**Task 15 — Xem toàn bộ giao dịch hệ thống (lựa chọn 7)**
- Stored Procedure `getAllTransactions` trả về toàn bộ bản ghi trong `BANKTRANSACTIONS`, sắp xếp theo ngày giảm dần.
- Hiển thị dạng bảng: Mã GD, Ngày, Số tiền, Trạng thái, Tài khoản gửi, Tài khoản nhận.

---

## Hướng Dẫn Chạy Ứng Dụng

**Yêu cầu:** Java 17+, MySQL 8.0+, Maven 3.x

```bash
# Bước 1: Khởi tạo database
mysql -u root -p < sql/script.sql

# Bước 2: Cấu hình kết nối
# Mở file: project/src/main/java/com/bankmanagement/config/dbConnection.java
# Cập nhật JDBC URL, username và password phù hợp với MySQL cục bộ

# Bước 3: Chạy ứng dụng
cd project
mvn exec:java
```

---

## Dependencies

| Thư viện | Phiên bản | Mục đích |
|---------|-----------|---------|
| `mysql-connector-j` | 9.x | JDBC driver kết nối MySQL |
| `jbcrypt` | 0.4 | Hash BCrypt cho mật khẩu và mã PIN |

---

## Phân Công Công Việc

**Nguyễn Huy Lợi** — Schema database, cơ sở hạ tầng cốt lõi
- Thiết kế schema toàn bộ 5 bảng
- Stored Procedure: `createUserAccount`, `createBankAccount`, `withDrawMoney`, `depositMoney`, `checkBalance`
- Java: luồng đăng nhập, đăng ký, tạo tài khoản ngân hàng, kiểm tra số dư
- Còn lại: tầng Java cho rút tiền (Task 3) và nạp tiền (Task 7)

**Trần Anh Vũ** — Các nghiệp vụ quản lý tài khoản, giao dịch và quản trị
- Stored Procedure: `updateInfo`, `transferMoney`, `checkTransaction`, `deleteAccount`, `changeAccountPin`, `searchTransactionByDate`, `searchUser`, `getAllTransactions`, `unblockAccount`
- Java: cập nhật thông tin, chuyển tiền, lịch sử giao dịch, đổi PIN, xem hồ sơ, tìm GD theo ngày, khóa/mở khóa tài khoản, xem danh sách khách hàng, tìm kiếm khách hàng, xem toàn bộ GD hệ thống
- Kiến trúc bảo mật: mô hình "Java xác thực — SQL thực thi" với BCrypt xuyên suốt toàn bộ hệ thống