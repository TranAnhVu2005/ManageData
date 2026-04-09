DROP DATABASE IF EXISTS MANAGEBANKACCOUNT;
create database MANAGEBANKACCOUNT;
use MANAGEBANKACCOUNT;
/*Start create and modify database*/
select * from accountbank;
create table USERACCOUNTS (	
	userID varchar(10) primary key,
    userName varchar(200) not null,
    ID char(12) not null unique,
    passWordHash varchar(200) not null,
    birthDay date not null,
    numberPhone varchar(10) not null unique,
    email varchar(100) not null unique,
    roleUser varchar(20) not null	
);

/* 
 * Bảng STAFF_AUDIT_LOGS: Lưu lại các hành động quản trị của nhân viên (Staff).
 * Giúp Admin kiểm soát được ai đã khoá/mở khoá tài khoản nào, khi nào.
 */
create table STAFF_AUDIT_LOGS (
    logId int auto_increment primary key,
    staffID varchar(10) not null,
    actionType varchar(100) not null,
    targetInfo varchar(500),
    actionAt datetime default CURRENT_TIMESTAMP,
    foreign key (staffID) references USERACCOUNTS(userID) on update cascade
);

select * from useraccounts;
update useraccounts set roleUser = "Admin" where userID = "U203469";
#Tài khoản Nhân viên chèn cứng mk: 123456
INSERT INTO USERACCOUNTS
(userID, userName, ID, passWordHash, birthDay, numberPhone, email, roleUser)
VALUES (
    "STAFF00001",
    "Van Teo",
    "111111111111",
    "$2a$10$BuHGpV5BogFkqHpYz0H0sO2unKxmx/cLqd2EDM4VI.g7v.Zk2fYYq",
    "2000-12-12",
    "0123456789",
    "vanteo@gmail.com",
    "Staff"
);
INSERT INTO ACCOUNTBANK
(numberAccount, userID, pinCodeHash, balance)
VALUES (
    "0000000001",
    "STAFF00001",
    "$2a$10$BuHGpV5BogFkqHpYz0H0sO2unKxmx/cLqd2EDM4VI.g7v.Zk2fYYq",
    0
);
/* 
 * Bảng AUDIT_LOG_USER: Lưu lại vết thay đổi thông tin định danh của khách hàng (CIF).
 * Thực tế: Ngân hàng không bao giờ xoá lịch sử khi đổi SĐT/Email để phòng chống gian lận.
 * - actionType: Loại hành động (VD: 'Change Phone', 'Change Email').
 * - oldValue: Dữ liệu cũ trước khi đổi.
 * - newValue: Dữ liệu mới.
 */
create table AUDIT_LOG_USER (
    logId int auto_increment primary key,
    userID varchar(10) not null,
    actionType varchar(50) not null,
    oldValue varchar(500),
    newValue varchar(500),
    changedAt datetime default CURRENT_TIMESTAMP,
    foreign key (userID) references USERACCOUNTS(userID) on update cascade on delete cascade
);

select * from AUDIT_LOG_USER;
delimiter $$
/* 
 * Trigger log_user_update: Chạy SIÊU TỐC và TỰ ĐỘNG (AFTER UPDATE) mỗi khi bảng USERACCOUNTS bị đổi dữ liệu.
 * - NEW: Dữ liệu mới truyền vào, OLD: Dữ liệu cũ đang nằm trong máy chủ.
 * - Nếu phát hiện SĐT bị đổi (<>), tự động chèn 1 dòng log vào bảng AUDIT_LOG_USER.
 */
create trigger log_user_update
after update on USERACCOUNTS
for each row
begin
    if OLD.numberPhone <> NEW.numberPhone then
        insert into AUDIT_LOG_USER (userID, actionType, oldValue, newValue)
        values (NEW.userID, 'Change Phone', OLD.numberPhone, NEW.numberPhone);
    end if;
    if OLD.email <> NEW.email then
        insert into AUDIT_LOG_USER (userID, actionType, oldValue, newValue)
        values (NEW.userID, 'Change Email', OLD.email, NEW.email);
    end if;
end$$
delimiter ;



create table ACCOUNTBANK(
	numberAccount varchar(10) primary key,
    userID varchar(10) not null,
    pinCodeHash varchar(64) not null,
    balance decimal(15,2) not null default 0 check (balance >=0),
    state enum("Active", "Blocked") not null DEFAULT "Active",
    created_at datetime DEFAULT NOW(),
    foreign key (userID) references USERACCOUNTS(userID) on update cascade on delete cascade
);

create table CARDS (
	cardNumber char(16) primary key,
    cardPinCodeHash varchar(64) not null,
    created_at date not null,
    expire_at date not null,
    secureCode char(3) not null,
    numberAccount varchar(10) not null,
    foreign key (numberAccount) references ACCOUNTBANK(numberAccount) on update cascade on delete cascade
);

create table TYPEOFTRANSACTION (
	typeOfTransactionCode char(4) primary key,
    nameTypeOfTransaction varchar(100) not null,
    description varchar(100) not null
);
insert into typeoftransaction values ('W001', 'Rút tiền', 'Khách hàng rút tiền từ tài khoản'),
('T001', 'Chuyển tiền', 'Chuyển tiền giữa các tài khoản'),
('D001', 'Nạp tiền', 'Nạp tiền vào tài khoản');

create table BANKTRANSACTIONS(
	transactionId char(30) primary key,
    created_at datetime not null default current_timestamp,
    amount decimal(15,2) not null default 0 check (amount >=0),
    stateOfTransaction enum("Processing", "Success", "Cancel") not null default "Processing",
    typeOfTransactionCode char(4) not null,
    numberAccount varchar(10) not null,
    destinationAccount varchar(10),
    foreign key (typeOfTransactionCode) references TYPEOFTRANSACTION(typeOfTransactionCode) on update cascade on delete cascade,
    foreign key (numberAccount) references ACCOUNTBANK(numberAccount) on update cascade on delete cascade,
    foreign key  (destinationAccount) references accountbank(numberAccount) on update cascade on delete cascade
);



/*End create and modify database*/


/*Create random code function */
delimiter $$
create function random_string (
    p_length int
)
returns varchar(30)
deterministic
reads sql data
begin
	declare chars VARCHAR(62) DEFAULT 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    declare rs varchar(30) default '';
    declare i int default 0;
    
    while i < p_length do
		set rs = concat(rs, SUBSTRING(chars, FLOOR(1 + RAND() * 62), 1));
        set i = i+1;
	end while;
    return rs;
end$$
delimiter ;

/*Start Task 1: Create account* - Lợi*/

delimiter $$
create procedure createUserAccount (
	IN p_userID varchar(10),
	IN p_userName varchar(200),
    IN p_ID char(12),
    IN p_passwordHash varchar(2000),
    IN p_birthday date,
    IN p_numberPhone varchar(15),
    IN p_email varchar(100),
    IN p_roleUser varchar(20)
)
begin
	insert into useraccounts values (p_userID, p_userName, p_ID, p_passwordHash, p_birthday, p_numberPhone, p_email, p_roleUser);
end$$
delimiter ;

/*End Task 1: Create account* - Lợi*/

/*Start Task 9: Create accountBank* - Lợi*/

DELIMITER $$


CREATE PROCEDURE createBankAccount (
    IN  p_numberAccount VARCHAR(12),
    IN  p_pinCodeHash   VARCHAR(64),
    IN  p_userID        VARCHAR(10),
    OUT p_result INT
)
proc: BEGIN

    DECLARE v_exist INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result = 2; -- server error
    END;

    SET p_result = 2;

    START TRANSACTION;

    -- 1. Kiểm tra user tồn tại
    SELECT COUNT(*) INTO v_exist
    FROM USERACCOUNTS
    WHERE userID = p_userID;

    IF v_exist = 0 THEN
        SET p_result = 1;
        ROLLBACK;
        LEAVE proc;
    END IF;

    -- 2. Kiểm tra trùng số tài khoản
    SELECT COUNT(*) INTO v_exist
    FROM ACCOUNTBANK
    WHERE numberAccount = p_numberAccount;

    IF v_exist > 0 THEN
        SET p_result = 3;
        ROLLBACK;
        LEAVE proc;
    END IF;

    -- 3. Insert
    INSERT INTO ACCOUNTBANK
    (numberAccount, userID, pinCodeHash,
     balance, state, created_at)
    VALUES
    (p_numberAccount, p_userID, p_pinCodeHash,
     0, 'Active', NOW());

    SET p_result = 0; -- success
    COMMIT;

END$$
DELIMITER ;

/*End Task 9: Create accountBank* - Lợi*/


/*Start Task 2: Update account* - Vũ*/
delimiter $$
create procedure updateInfo(
	IN p_userID varchar(10),
	IN p_userName varchar(200),
    IN p_ID char(12),
    IN p_birthDay date,
    IN p_numberPhone varchar(15),
    IN p_email varchar(100),
    IN p_passwordHashNew varchar(200),
    OUT p_result varchar(200)
)
begin
    declare exit handler for sqlexception
    begin
		set p_result = "Error";
    end;
    
    UPDATE USERACCOUNTS SET
        userName = COALESCE(p_userName, userName),
        ID = COALESCE(p_ID, ID),
        birthDay = COALESCE(p_birthDay, birthDay),
        numberPhone = COALESCE(p_numberPhone, numberPhone),
        email = COALESCE(p_email, email),
        passWordHash = COALESCE(p_passwordHashNew, passWordHash)
        WHERE userID = p_userID;

    if ROW_COUNT() > 0 then
        set p_result = "Success";
    else
        set p_result = "Not found user or no changes made";
    end if;
end$$
delimiter ;

/*End Task 2: Update account* - Vũ */


/*Start Task 3: Withdraw money* - Lợi*/
delimiter $$
create procedure withDrawMoney (
	IN p_cardNumber char(16),
    IN p_amount decimal(15, 2),
    IN p_transactionId char(30),
    OUT p_result INT # 0: success, 1: no card number, 3: not enough balance, 4: error by server, 5: not authorization
)
proc: begin
    declare v_numberAccount varchar(10) default NULL;
    declare v_balance decimal(15, 2) default 0;
    declare v_role varchar(20) default NULL;
    declare exit handler for sqlexception
    begin
		rollback;
        update BANKTRANSACTIONS
			set stateOfTransaction = 'Cancel'
			where transactionId = p_transactionId;
		set p_result = 4;
    end;
    
    set p_result = 4;
    
    if p_amount <= 0 then
		set p_result = 4;
		leave proc;
	end if;
    
    select numberAccount
    into v_numberAccount
    from CARDS
    where cardNumber = p_cardNumber;

    if v_numberAccount is null then
        set p_result = 1;
        LEAVE proc;
    END IF;
    
    select u.roleUser
		into v_role
		from ACCOUNTBANK a
		join USERACCOUNTS u on a.userID = u.userID
		where a.numberAccount = v_numberAccount;
    if v_role <> 'Client' then
        set p_result = 5;
        leave proc;
	end if;
    
    INSERT INTO BANKTRANSACTIONS
    VALUES (p_transactionId, NOW(), p_amount, 'Processing', 'W001', v_numberAccount, v_numberAccount);
    
    start transaction;
    
    select balance
    into v_balance
    from ACCOUNTBANK
    where numberAccount = v_numberAccount
    for update;
    
    if v_balance < p_amount then
		set p_result = 3;
        rollback;
        update BANKTRANSACTIONS
			set stateOfTransaction = 'Cancel'
			where transactionId = p_transactionId;
        leave proc;
	end if;
    
    update ACCOUNTBANK set balance = balance - p_amount where numberAccount = v_numberAccount;
    update BANKTRANSACTIONS set stateOfTransaction = 'Success' where transactionId = p_transactionId;
    set p_result = 0;
    commit;
end$$
delimiter ;
/*End Task 3: Withdraw money - Lợi*/



/*Start Task 4: Transfer money* - Vũ*/
delimiter $$
create procedure transferMoney(
	in p_numberAccount varchar(10),
	in p_destinationAccount varchar(10),
    in p_amount decimal(15,2),
    out p_result varchar(200)
)
begin
    declare v_balance decimal(15,2);
    declare v_state enum("Active","Blocked");
    declare v_state_des enum("Active","Blocked");
    declare exit handler for sqlexception 
    begin
		rollback;
        set p_result = "Error transaction";
    end;
    
    select balance, state into v_balance, v_state from ACCOUNTBANK where numberAccount = p_numberAccount;
	select state into v_state_des from ACCOUNTBANK where numberAccount = p_destinationAccount;
    IF v_state is null
    THEN set p_result ="Not found account";
    ELSEIF v_state_des is null
    THEN set p_result ="Not found account destination";
    ELSEIF v_state_des != "Active"
    THEN set p_result = "The account destination is not active";
    ELSEIF v_state!="Active"
    THEN set p_result = "This account is blocked";
	ELSEIF p_amount > v_balance 
		THEN set p_result = "Not enough balance";
	ELSE
		start transaction;
			update ACCOUNTBANK set balance = balance - p_amount where numberAccount =  p_numberAccount;
            update ACCOUNTBANK set balance = balance + p_amount where numberAccount =  p_destinationAccount;
            insert into BANKTRANSACTIONS(
					transactionID,
                    amount, 
                    stateOfTransaction, 
                    typeofTransactionCode,
                    numberAccount,
                    destinationAccount
            ) values
            (
				CONCAT("TRSF",random_string(10)),
                p_amount,
                "Success",
                "T001",
                p_numberAccount,
                p_destinationAccount
            );
		commit;
        set p_result = "Success";
	END IF;
end $$
delimiter ;

/*End Task 4: Transfer money* - Vũ*/



/*Start Task 5: Check balance* - Lợi*/
delimiter $$
create procedure checkBalance (
	IN p_numberAccount varchar(10),
    OUT p_balance decimal(15, 2),
    OUT p_result int # 0:success, 1:no account, 3:blocked, 4: server error
)
proc: begin
    DECLARE v_state ENUM('Active','Blocked');
    
    set p_result = 4;
    set p_balance = null;
    
    select state, balance
    into v_state, p_balance
    from ACCOUNTBANK
    where numberAccount = p_numberAccount;
    
    if v_state is null then
		set p_result = 1;
        set p_balance = NULL;
        leave proc;
	end if;
    
    if v_state = "Blocked" then
		set p_result = 3;
        set p_balance = NULL;
        leave proc;
	end if;
    
    set p_result = 0;
end$$
delimiter ;
/*End Task 5: Check balance* - Lợi*/



/*Start Task 6: Check transaction - Vũ*/
delimiter $$
create procedure checkTransaction(
	in p_numberAccount varchar(10),
    out p_result varchar(200)
)
begin
	declare v_state enum("Active","Blocked");
	select state into v_state from ACCOUNTBANK where numberAccount = p_numberAccount;
    IF v_state is null
    THEN set p_result ="Not found account";
    ELSEIF v_state!="Active"
    THEN set p_result = "This account is blocked";
	ELSE 
		select * from BANKTRANSACTIONS where numberAccount = p_numberAccount or destinationAccount = p_numberAccount;
        set p_result = "Success";
	END IF;
end $$
delimiter ;
/*End Task 6: Check transaction* - Vũ*/


/*Start Task 7: Deposit money into an account * - Lợi*/
delimiter $$
DROP PROCEDURE IF EXISTS depositMoney$$
CREATE PROCEDURE depositMoney(
    IN p_staffID VARCHAR(10), -- Chỉ cần ID nhân viên để ghi log (Ai thực hiện nạp)
    IN p_numberAccountUser VARCHAR(10), 
    IN p_transactionId CHAR(30), 
    IN p_amount DECIMAL(15,2), 
    OUT p_result INT
)
proc: BEGIN
    DECLARE v_role VARCHAR(20) DEFAULT NULL;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION 
    BEGIN 
        ROLLBACK; 
        UPDATE BANKTRANSACTIONS SET STATEOFTRANSACTION = 'Cancel' WHERE TRANSACTIONID = p_transactionId; 
        SET p_result = 2; 
    END;
    
    SET p_result = 2;
    IF p_amount <= 0 THEN LEAVE proc; END IF;

    -- Kiểm tra tài khoản khách hàng
    SELECT u.ROLEUSER INTO v_role 
    FROM ACCOUNTBANK a JOIN USERACCOUNTS u ON a.USERID = u.USERID 
    WHERE a.NUMBERACCOUNT = p_numberAccountUser;
    
    IF v_role IS NULL THEN SET p_result = 1; LEAVE proc; END IF;
    IF v_role <> 'Client' THEN SET p_result = 5; LEAVE proc; END IF;

    -- Kiểm tra người thực hiện có phải Staff không (Dựa vào ID)
    SELECT ROLEUSER INTO v_role FROM USERACCOUNTS WHERE USERID = p_staffID;
    IF v_role <> 'Staff' THEN SET p_result = 5; LEAVE proc; END IF;

    -- Ghi log giao dịch (destinationAccount = null hoặc 'CASH')
    INSERT INTO BANKTRANSACTIONS (TRANSACTIONID, AMOUNT, STATEOFTRANSACTION, TYPEOFTRANSACTIONCODE, NUMBERACCOUNT, DESTINATIONACCOUNT)
    VALUES (p_transactionId, p_amount, 'Processing', 'D001', p_numberAccountUser, NULL);

    START TRANSACTION;
        -- CHỈ CỘNG TIỀN VÀO TÀI KHOẢN KHÁCH HÀNG (Không trừ của ai cả)
        UPDATE ACCOUNTBANK SET BALANCE = BALANCE + p_amount WHERE NUMBERACCOUNT = p_numberAccountUser;
        
        IF ROW_COUNT() = 0 THEN
            SET p_result = 2; ROLLBACK;
            UPDATE BANKTRANSACTIONS SET STATEOFTRANSACTION = 'Cancel' WHERE TRANSACTIONID = p_transactionId;
            LEAVE proc;
        END IF;
        
        UPDATE BANKTRANSACTIONS SET STATEOFTRANSACTION = 'Success' WHERE TRANSACTIONID = p_transactionId;
        SET p_result = 0;
    COMMIT;
END$$
delimiter ;

/*End Task 7: Deposit money into an account * - Lợi*/



/*Start Task 8: Block account* - Vũ*/
delimiter $$
create procedure blockAccount(
	in p_numberAccount varchar(10),
    out p_result varchar(200)
)
begin
    declare v_state enum("Active","Blocked");
	declare v_balance decimal(15,2);
    select state, balance into v_state,v_balance from ACCOUNTBANK where numberAccount = p_numberAccount;
    IF v_state is null
    THEN set p_result ="Not found account";
    ELSEIF v_state != "Active"
    THEN set p_result = "This account is already blocked";
	ELSE
		update ACCOUNTBANK set state = "Blocked" where numberAccount = p_numberAccount;
        -- Ghi log hành động của staff (Cần truyền staffID vào, nhưng hiện tại SP chưa có tham số này. 
        -- Để đơn giản trong đồ án, ta sẽ cải tiến SP này sau hoặc ghi log tại tầng Java).
        set p_result = "Success";
	END IF;
end$$
delimiter ;
/*End Task 8: Block account* - Vũ*/
select * from accountbank;

/*Start Task 10: Change Account PIN* - Vũ*/
delimiter $$
/* 
 * Stored Procedure changeAccountPin: Đổi mã PIN của Tài khoản Ngân hàng.
 * Thuộc phạm trù ACCOUNTBANK -> Chỉ cho phép đổi PIN khi Tài Khoản Ngân Hàng đang 'Active'.
 */
create procedure changeAccountPin(
	IN p_numberAccount varchar(10),
    IN p_newPinHash varchar(64),
    OUT p_result varchar(200)
)
begin
    declare v_state enum("Active","Blocked");
    
    declare exit handler for sqlexception
    begin
		set p_result = "Error";
    end;
    
    -- Lấy trạng thái từ database lên
    SELECT state INTO v_state FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;
    
    IF v_state is null THEN
		set p_result = "Not found account";
	ELSEIF v_state != "Active" THEN
		set p_result = "This account is blocked";
	ELSE
        -- Đủ điều kiện: Cập nhật sang mã PIN mới
		UPDATE ACCOUNTBANK SET pinCodeHash = p_newPinHash WHERE numberAccount = p_numberAccount;
		set p_result = "Success";
	END IF;
end$$
delimiter ;
/*End Task 10: Change Account PIN* - Vũ*/



/*Start Task 11: Search transaction by date range* - Lợi*/
delimiter $$
/*
 * searchTransactionByDate: Lọc lịch sử giao dịch của một tài khoản theo khoảng thời gian.
 * Trả về ResultSet + OUT p_result để client phân biệt lỗi / thành công.
 */
create procedure searchTransactionByDate(
    IN p_numberAccount varchar(10),
    IN p_fromDate datetime,
    IN p_toDate datetime,
    OUT p_result varchar(200)
)
begin
    declare v_state enum("Active","Blocked");

    select state into v_state from ACCOUNTBANK where numberAccount = p_numberAccount;

    IF v_state is null THEN
        set p_result = "Not found account";
    ELSEIF v_state != "Active" THEN
        set p_result = "This account is blocked";
    ELSE
        select transactionId, created_at, amount, stateOfTransaction,
               typeOfTransactionCode, numberAccount, destinationAccount
        from BANKTRANSACTIONS
        where (numberAccount = p_numberAccount or destinationAccount = p_numberAccount)
          and created_at between p_fromDate and p_toDate
        order by created_at desc;
        set p_result = "Success";
    END IF;
end$$
delimiter ;
/*End Task 11: Search transaction by date range* - Lợi*/



/*Start Task 12: Search user by phone or ID* - Vũ*/
delimiter $$
/*
 * searchUser: Tìm kiếm khách hàng theo số điện thoại hoặc số CCCD.
 * Staff dùng để tra cứu thông tin khi cần hỗ trợ khách hàng.
 */
create procedure searchUser(
    IN p_keyword varchar(100),
    OUT p_result varchar(200)
)
begin
    declare v_count int default 0;

    select count(*) into v_count
    from USERACCOUNTS
    where numberPhone like concat('%', p_keyword, '%')
       or ID like concat('%', p_keyword, '%');

    if v_count = 0 then
        set p_result = "Not found";
    else
        select userID, userName, ID, birthDay, numberPhone, email, roleUser
        from USERACCOUNTS
        where numberPhone like concat('%', p_keyword, '%')
           or ID like concat('%', p_keyword, '%');
        set p_result = "Success";
    end if;
end$$
delimiter ;
/*End Task 12: Search user by phone or ID* - Vũ*/


select * from useraccounts;

/*Task 14 create card for account */
delimiter $$
create procedure createCard(
    IN p_cardNumber char(16),
    IN p_numberAccount varchar(10),
    IN p_pinCode varchar(64),
    IN p_ccv char(3),
    OUT p_result int
) # 0: success, 1: account not found, 2: server error
proc: begin
    declare v_accountExists int default 0;
    declare exit handler for sqlexception
    begin
        set p_result = 2; -- server error
    end;
    set p_result = 2; -- default to server error
    -- Kiểm tra tài khoản tồn tại
    select count(*) into v_accountExists
    from ACCOUNTBANK
    where numberAccount = p_numberAccount;
    if v_accountExists = 0 then
        set p_result = 1; -- account not found
        leave proc;
    end if;
    -- Tạo thẻ mới
    insert into CARDS (cardNumber, cardPinCodeHash, created_at, expire_at, secureCode, numberAccount)
    values (p_cardNumber, p_pinCode, curdate(), date_add(curdate(), interval 5 year), p_ccv, p_numberAccount);
    set p_result = 0; -- success
end$$
delimiter ;
/*End Task 14: Create Card for Account* - Loi*/


/*Start Task 13: Unblock account* - Vũ*/
select * from accountbank;
delimiter $$
create procedure unblockAccount(
	in p_numberAccount varchar(10),
    out p_result varchar(200)
)
begin
    declare v_state enum("Active","Blocked");
	declare v_balance decimal(15,2);
    select state, balance into v_state,v_balance from ACCOUNTBANK where numberAccount = p_numberAccount;
    IF v_state is null
    THEN set p_result ="Not found account";
    ELSEIF v_state != "Blocked"
    THEN set p_result = "This account is already active";
	ELSE
		update ACCOUNTBANK set state = "Active" where numberAccount = p_numberAccount;
        set p_result = "Success";
	END IF;
end$$
delimiter ;
/*End Task 13: Unblock account* - Vũ*/


/*Task 15 View Audit Logs */
delimiter $$
create procedure viewAuditLogs(
    in p_userID varchar(10),
    out p_result varchar(200)
)
begin
    declare v_exists int;
    
    -- Kiểm tra user có tồn tại không
    select count(*) into v_exists from USERACCOUNTS where userID = p_userID;
    
    if v_exists = 0 then
        set p_result = "User not found";
    else
        -- Lấy lịch sử thay đổi, xếp cái mới nhất lên đầu
        select logId, actionType, oldValue, newValue, changedAt 
        from AUDIT_LOG_USER 
        where userID = p_userID 
        order by changedAt desc;
        set p_result = "Success";
    end if;
end$$
delimiter ;
/*End Task 15: View Audit Logs - Vũ*/
select * from audit_log_user;
/*Statistics total money in system*/
delimiter $$
create procedure getSystemStatistics(
    OUT p_totalUsers int,
    OUT p_totalAccounts int,
    OUT p_totalBalance double
)
begin
    select count(*) into p_totalUsers from USERACCOUNTS;
    select count(*) into p_totalAccounts from ACCOUNTBANK;
    select sum(balance) into p_totalBalance from ACCOUNTBANK;
end$$
delimiter ;

/* [NEW] Reporting Views for Grade A */
create view VIEW_TRANSACTION_SUMMARY as
select 
    date(created_at) as transaction_date,
    typeOfTransactionCode,
    count(*) as total_transactions,
    sum(amount) as total_amount
from BANKTRANSACTIONS
group by date(created_at), typeOfTransactionCode;

create view VIEW_USER_FINANCIAL_HEALTH as
select 
    u.userID,
    u.userName,
    count(a.numberAccount) as total_accounts,
    sum(a.balance) as total_balance
from USERACCOUNTS u
left join ACCOUNTBANK a on u.userID = a.userID
group by u.userID, u.userName;

/* Procedure to Log Staff Actions */
delimiter $$
create procedure logStaffAction(
    IN p_staffID varchar(10),
    IN p_actionType varchar(100),
    IN p_targetInfo varchar(500)
)
begin
    insert into STAFF_AUDIT_LOGS (staffID, actionType, targetInfo)
    values (p_staffID, p_actionType, p_targetInfo);
end$$
delimiter ;
/*End Statistics total money in system*/


