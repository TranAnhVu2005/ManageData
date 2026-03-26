Nguyễn Huy Lợi - 
Trần Anh Vũ - B2306603

Task cần làm:
Đăng nhập
Đăng ký

Tao tai khoan nguoi dung- Loi
Cap nhat tai khoan tien- Vu
Rut tien - Loi
Chuyen tien - Vu
Kiem tra so du - Loi
Kiem tra giao dich - Vu
Gui tien vao tai khoan - Loi
Xoa tai khoan - Vu

Giai doan 1: Viet model
Bảng thằng nào thằng đó viết


Giai doan 2: Viet dao

AccountDao  Vũ               UserDAO Lợi
createUserAccount()         updateInfo()
withdrawMoney()             transferMoney()
checkBalance()              checkTransaction()
depositMoney()              deleteAccount()

AuthDAO
login()
register()

Giai doan 3: Viet console menu

Bước 1: Viết khung menu + MOCK data trước
──────────────────────────────────────────
ConsoleMenu.java
  start()
    → printMainMenu()
    → handleLogin()       MOCK: trả về User cứng
    → handleRegister()    MOCK: in "Thành công"

  showUserMenu()
    → handleUpdateInfo()        in "Chưa làm"
    → handleTransfer()          in "Chưa làm"
    → handleTransactionHistory  in "Chưa làm"
    → handleCheckBalance()      in "Chưa làm"
    → handleWithdraw()          in "Chưa làm"

  showAdminMenu()
    → handleCreateAccount()     in "Chưa làm"
    → handleDeposit()           in "Chưa làm"
    → handleDeleteAccount()     in "Chưa làm"

Bước 2: Chạy được toàn bộ menu với MOCK
──────────────────────────────────────────
mvn exec:java
→ Menu hiển thị đúng
→ Chọn được các option
→ Điều hướng đúng Client/Staff

Bước 3: Thay MOCK bằng DAO thật
──────────────────────────────────────────
handleLogin()     → AuthDAO.login()
handleRegister()  → AuthDAO.register()