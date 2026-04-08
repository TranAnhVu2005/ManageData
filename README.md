# 🏦 Hệ Thống Quản Lý Ngân Hàng (Bank Management System)
> Dự án môn học CT476 - Quản trị Dữ liệu (Trường ĐH Cần Thơ)
> **Thực hiện bởi:** Trần Anh Vũ & Lợi

## 📝 Giới thiệu
Đây là một dự án mô phỏng hệ thống quản trị ngân hàng bằng ngôn ngữ **Java** (Console Application) kết nối trực tiếp với Cơ sở dữ liệu **MySQL**. Hệ thống phân tách thành hai quyền hạn rõ rệt: **Khách hàng** (Client) và **Quản trị viên** (Staff). 

Đặc biệt, nhằm tối ưu hóa hiệu suất và đảm bảo tính toàn vẹn dữ liệu, **toàn bộ logic nghiệp vụ lõi đều được thực thi tại Database thông qua hệ thống Stored Procedures**, kết hợp với khả năng bảo mật nội tại bằng thuật toán **BCrypt** ở phía Java đóng vai trò như một lớp áo giáp xác thực.

---

## 🚀 12 Chức năng Cốt lõi (Workflow)
Hệ thống tuân thủ chặt chẽ 12 tác vụ nghiệp vụ quan trọng theo đúng yêu cầu đề bài:

1. **Đăng ký tài khoản hệ thống (`createUserAccount`)** - *Vũ*  
   *Khách hàng tạo tài khoản đăng nhập vào hệ thống ứng dụng.*
2. **Cập nhật thông tin & Xem hồ sơ (`updateInfo / viewProfile`)** - *Vũ*  
   *Cập nhật chi tiết các thông tin như CCCD, Ngày sinh, Mật khẩu hệ thống (được băm bằng BCrypt).*
3. **Mở tài khoản ngân hàng (`createBankAccount`)** - *Lợi (Admin) & Vũ (Client)*  
   *Mỗi khách hàng có tối đa 10 thẻ/tài khoản. Admin cũng có quyền khởi tạo cho khách từ màn hình quản trị.*
4. **Kiểm tra số dư (`checkBalance`)** - *Vũ*  
   *Xác thực bằng mã PIN 6 số của riêng tài khoản đó để truy vấn.*
5. **Đổi mã PIN ngân hàng (`changeAccountPin`)** - *Vũ*  
   *Yêu cầu nhập đúng PIN hiện tại (xác thực tại Java) trước khi MySQL cập nhật Hash PIN mới.*
6. **Chuyển khoản nội bộ (`transferMoney`)** - *Vũ*  
   *Chuyển số dư từ tài khoản này sang tài khoản khác. Áp dụng Transaction DB để tránh lỗi trừ tiền nhưng tiền không tới.*
7. **Rút tiền qua thẻ (`withdrawMoney`)** - *Lợi*  
   *Thực hiện rút tiền trực tiếp yêu cầu 16 số trên Thẻ ngân hàng thật.*
8. **Nạp tiền vào tài khoản (`depositMoney`)** - *Lợi*  
   *Admin nhập số tiền để bơm trực tiếp vào tài khoản được yêu cầu.*
9. **Tra cứu lịch sử giao dịch (`checkTransaction`)** - *Vũ*  
   *In ra toàn bộ bản sao kê (Sao kê chuyển tiền, nhận tiền, rút tiền, báo cáo).*
10. **Lọc giao dịch theo ngày (`searchTransactionByDate`)** - *Vũ*  
    *Truy xuất sao kê từ khoảng thời gian tự chọn (Từ ngày - Đến ngày).*
11. **Khóa tài khoản ngân hàng (`deleteAccount`)** - *Vũ*  
    *Hành động của Admin. Chỉ được khóa khi số dư trong tài khoản đã bằng 0.*
12. **Tra cứu toàn bộ khách hàng & Tìm kiếm (`getAllUsers / searchUser`)** - *Vũ*  
    *Nhân viên ngân hàng xem tổng đài dữ liệu khách hàng hoặc dò tìm bằng số SĐT/CCCD.*

*(Phân chia công việc: Lợi đảm nhận các chức năng Rút tiền, Nạp tiền, Tạo Account từ GUI Admin; Các phần còn lại và cấu trúc khung luồng do Vũ đảm nhận).*

---

## 🛡️ Kiến trúc & Bảo Mật (Technical Highlights)
- **Tầng Bảo Mật (Security):** Mật khẩu người dùng (Password) và mã PIN giao dịch đều được băm bằng thuật toán `BCrypt` (kèm Salt tự động) ngay trên Java Controller. Database MySQL không bao giờ lưu trữ mật khẩu thuần (plaintext), đề phòng lỗ hổng Dump DB.
- **Tính vẹn toàn của Dữ liệu (ACID):** Sử dụng `Transactions` (`COMMIT` / `ROLLBACK`) bên trong Stored Procedures để kiểm soát tiền trong luồng chuyển khoản/rút tiền, tránh trình trạng tiền bị trừ đi nhưng do lỗi mạng hệ thống khiến người nhận không nhận được.
- **Workflow MVC rõ ràng:** 
  - `View`: Chỉ dùng vòng lặp, `Scanner` làm công cụ thu thập phím chạm.
  - `Controller`: Xác thực tài khoản, kiểm tra độ dài input (16 số thẻ, 6 số PIN), điều phối logic.
  - `DAO (Data Access Object)`: Chuyển dữ liệu cho SQL qua cổng Callablestatement.

---

## 💽 Hướng dẫn Cài đặt & Vận hành
1. Mở hệ quản trị **MySQL Workbench** hoặc Datagrip.
2. Mở file `sql/script.sql` và nhấn biểu tượng Sét để thực thi toàn bộ đoạn mã dài hơn 600 dòng. Thao tác này sẽ tự động tạo `Schema`, các Bảng dữ liệu, Khóa ngoại, và cài đặt toàn bộ `Stored Procedures` hệ thống.
3. Chạy lệnh `mvn clean compile` tại thư mục gốc của `/project` để build dự án Java và đối chiếu thư viện `Mindrot jBcrypt` và `mysql-connector-java`.
4. Điều chỉnh User/Password trong file `com/bankmanagement/config/dbConnection.java` cho hợp lệ với Local MySQL Port (3306) của bạn.
5. Cửa sổ Command Line: Khởi chạy file `Main.java` để vào menu.
   - Để dùng chức năng Admin, bạn hãy đăng nhập bằng tài khoản có Flag thuộc role `Staff`. Thao tác đổi Flag này thực hiện trực tiếp trong MySQL: `UPDATE USERACCOUNTS SET roleUser = 'Staff' WHERE userID='...';`