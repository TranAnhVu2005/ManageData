# QUẢN LÝ TÀI KHOẢN NGÂN HÀNG
**Nhóm sinh viên thực hiện:**
- Nguyễn Huy Lợi
- Trần Anh Vũ - B2306603

---

## PHÂN CÔNG CHỨC NĂNG DỰA TRÊN STORED PROCEDURE & DAO
Dưới đây là phần phân công công việc đã được chuẩn hoá lại đúng với thực trạng code trong Database (`script.sql`) và DAO (`Java`).

### 1. Nguyễn Huy Lợi
Đảm nhận các tính năng liên quan đến tài khoản người dùng, tạo tài khoản và các nghiệp vụ giao dịch trực tiếp với ngân hàng:
- Đăng nhập (`login`)
- Đăng ký / Tạo tài khoản người dùng (`createUserAccount`)
- Tạo tài khoản ngân hàng / Thẻ (`createBankAccount`)
- Rút tiền (`withDrawMoney`)
- Nạp tiền / Gửi tiền vào tài khoản (`depositMoney`)
- Kiểm tra số dư (`checkBalance`)

### 2. Trần Anh Vũ
Đảm nhận các tính năng liên quan đến bảo trì tài khoản và giao dịch luân chuyển giữa các khách hàng:
- Cập nhật thông tin tài khoản (`updateInfo`)
- Chuyển tiền giữa các tài khoản (`transferMoney`)
- Kiểm tra lịch sử giao dịch (`checkTransaction`)
- Xóa / Khóa tài khoản ngân hàng (`deleteAccount`)

---

## WORKFLOW GIAI ĐOẠN PHÁT TRIỂN

### Giai đoạn 1: Viết Model (Thực thể CSDL)
- Bảng của ai đảm nhận thì người đó viết class Model tương ứng.

### Giai đoạn 2: Viết DAO (Data Access Object)
- **`UserAccoutsDAO.java` (Lợi đảm nhận):** 
  Viết các logic gọi Stored Procedure: `login()`, `register()`, `createBankAccount()`, `getBalance()`, `withdraw()`, `deposit()`.
- **`AccountDAO.java` (Vũ đảm nhận):** 
  Viết các logic gọi Stored Procedure: `updateInfo()`, `transferMoney()`, `checkTransaction()`, `deleteAccount()`.

### Giai đoạn 3: Viết Console Menu (Đã hoàn thành)
Bước 1: Viết khung menu + MOCK data trước
- ConsoleMenu.java có các hàm điều hướng (start, showUserMenu, showAdminMenu) nhưng trả về dữ liệu ảo.

Bước 2: Chạy thử luồng giao diện Console
- Test điều hướng Client/Staff hợp lý qua `mvn exec:java`.

Bước 3: Gắn DAO thật thay cho MOCK
- `handleLogin()` -> `UserAccoutsDAO.login()`
- `handleRegister()` -> `UserAccoutsDAO.register()`
- Các chức năng khác tương tự ánh xạ vào đúng DAO tương ứng ở Giai đoạn 2.

### Giai đoạn 4: Chuyển đổi ứng dụng lên Web (Đang tiến hành)
- **Công nghệ mô hình MVC:** Chuyển từ Java Console sang Web Framework (Spring Boot).
- Bọc lại tất cả các hàm DAO hiện có bằng **Service Layer**.
- Xây dựng **Controller** (ví dụ: `AccountController`, `AuthController`) gắn với giao diện HTML/Thymeleaf. Lấy mã lỗi từ procedure (`p_result`) hiển thị thành cảnh báo thân thiện lên cửa sổ trình duyệt (Ví dụ: `Không đủ số dư`, `Chuyển tiền thành công`).