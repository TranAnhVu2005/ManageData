-- =============================================================================
-- BANK MANAGEMENT SYSTEM - MASTER SCRIPT
-- =============================================================================

DROP DATABASE IF EXISTS MANAGEBANKACCOUNT;
CREATE DATABASE MANAGEBANKACCOUNT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE MANAGEBANKACCOUNT;

-- =============================================================================
-- 1. CREATING TABLES (Ordered by dependencies)
-- =============================================================================

CREATE TABLE USERACCOUNTS (	
    userID varchar(10) primary key,
    userName varchar(200) not null,
    ID char(12) not null unique,
    passWordHash varchar(200) not null,
    birthDay date not null,
    numberPhone varchar(10) not null unique,
    email varchar(100) not null unique,
    roleUser varchar(20) not null	
);

CREATE TABLE TYPEOFTRANSACTION (
    typeOfTransactionCode char(4) primary key,
    nameTypeOfTransaction varchar(100) not null,
    description varchar(100) not null
);

CREATE TABLE ACCOUNTBANK (
    numberAccount varchar(10) primary key,
    userID varchar(10) not null,
    pinCodeHash varchar(64) not null,
    balance decimal(15,2) not null default 0 check (balance >=0),
    state enum("Active", "Blocked") not null DEFAULT "Active",
    created_at datetime DEFAULT NOW(),
    foreign key (userID) references USERACCOUNTS(userID) on update cascade on delete cascade
);

CREATE TABLE CARDS (
    cardNumber char(16) primary key,
    cardPinCodeHash varchar(64) not null,
    created_at date not null,
    expire_at date not null,
    secureCode char(3) not null,
    numberAccount varchar(10) not null,
    foreign key (numberAccount) references ACCOUNTBANK(numberAccount) on update cascade on delete cascade
);

CREATE TABLE BANKTRANSACTIONS (
    transactionId char(30) primary key,
    created_at datetime not null default current_timestamp,
    amount decimal(15,2) not null default 0 check (amount >=0),
    stateOfTransaction enum("Processing", "Success", "Cancel") not null default "Processing",
    typeOfTransactionCode char(4) not null,
    numberAccount varchar(10) not null,
    destinationAccount varchar(10),
    foreign key (typeOfTransactionCode) references TYPEOFTRANSACTION(typeOfTransactionCode) on update cascade on delete cascade,
    foreign key (numberAccount) references ACCOUNTBANK(numberAccount) on update cascade on delete cascade,
    foreign key (destinationAccount) references ACCOUNTBANK(numberAccount) on update cascade on delete cascade
);

CREATE TABLE USERAUDITLOGS (
    logId int auto_increment primary key,
    userID varchar(10) not null,
    actionType varchar(50) not null,
    oldValue varchar(500),
    newValue varchar(500),
    changedAt datetime default CURRENT_TIMESTAMP,
    foreign key (userID) references USERACCOUNTS(userID) on update cascade on delete cascade
);

CREATE TABLE STAFFAUDITLOGS (
    logId int auto_increment primary key,
    staffID varchar(10) not null,
    actionType varchar(100) not null,
    targetInfo varchar(500),
    actionAt datetime default CURRENT_TIMESTAMP,
    foreign key (staffID) references USERACCOUNTS(userID) on update cascade
);

-- =============================================================================
-- 2. VIEWS
-- =============================================================================

CREATE VIEW VIEW_TRANSACTION_SUMMARY AS
SELECT 
    DATE(created_at) AS transaction_date,
    typeOfTransactionCode,
    COUNT(*) AS total_transactions,
    SUM(amount) AS total_amount
FROM BANKTRANSACTIONS
GROUP BY DATE(created_at), typeOfTransactionCode;

CREATE VIEW VIEW_USER_FINANCIAL_HEALTH AS
SELECT 
    u.userID,
    u.userName,
    COUNT(a.numberAccount) AS total_accounts,
    COALESCE(SUM(a.balance), 0) AS total_balance
FROM USERACCOUNTS u
LEFT JOIN ACCOUNTBANK a ON u.userID = a.userID
GROUP BY u.userID, u.userName;

-- =============================================================================
-- 3. SEED DATA (Initial inserts)
-- =============================================================================

INSERT INTO TYPEOFTRANSACTION VALUES 
('W001', 'Rút tiền', 'Khách hàng rút tiền từ tài khoản'),
('T001', 'Chuyển tiền', 'Chuyển tiền giữa các tài khoản'),
('D001', 'Nạp tiền', 'Nạp tiền vào tài khoản');

INSERT INTO USERACCOUNTS (userID, userName, ID, passWordHash, birthDay, numberPhone, email, roleUser)
VALUES (
    'STAFF00001', 'Van Teo', '111111111111', 
    '$2a$10$BuHGpV5BogFkqHpYz0H0sO2unKxmx/cLqd2EDM4VI.g7v.Zk2fYYq', #password: 123456
    '2000-12-12', '0123456789', 'vanteo@gmail.com', 'Staff'
);

INSERT INTO ACCOUNTBANK (numberAccount, userID, pinCodeHash, balance)
VALUES (
    '0000000001', 'STAFF00001', 
    '$2a$10$BuHGpV5BogFkqHpYz0H0sO2unKxmx/cLqd2EDM4VI.g7v.Zk2fYYq', 
    0
);

-- =============================================================================
-- 4. FUNCTIONS & TRIGGERS
-- =============================================================================

DELIMITER $$

CREATE FUNCTION random_string(p_length int)
RETURNS varchar(30)
DETERMINISTIC
READS SQL DATA
BEGIN
    DECLARE chars VARCHAR(62) DEFAULT 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
    DECLARE rs varchar(30) default '';
    DECLARE i int default 0;
    WHILE i < p_length DO
        SET rs = concat(rs, SUBSTRING(chars, FLOOR(1 + RAND() * 62), 1));
        SET i = i+1;
    END WHILE;
    RETURN rs;
END$$

CREATE TRIGGER log_user_update
AFTER UPDATE ON USERACCOUNTS
FOR EACH ROW
BEGIN
    IF OLD.numberPhone <> NEW.numberPhone THEN
        INSERT INTO USERAUDITLOGS (userID, actionType, oldValue, newValue)
        VALUES (NEW.userID, 'Change Phone', OLD.numberPhone, NEW.numberPhone);
    END IF;
    IF OLD.email <> NEW.email THEN
        INSERT INTO USERAUDITLOGS (userID, actionType, oldValue, newValue)
        VALUES (NEW.userID, 'Change Email', OLD.email, NEW.email);
    END IF;
END$$

-- =============================================================================
-- 5. STORED PROCEDURES
-- =============================================================================

/* Task 1: Create account */
CREATE PROCEDURE createUserAccount (
    IN p_userID varchar(10), IN p_userName varchar(200), IN p_ID char(12),
    IN p_passwordHash varchar(2000), IN p_birthday date, IN p_numberPhone varchar(15),
    IN p_email varchar(100), IN p_roleUser varchar(20), OUT p_result varchar(200)
)
BEGIN
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN SET p_result = 'Error'; END;
    INSERT INTO USERACCOUNTS VALUES (p_userID, p_userName, p_ID, p_passwordHash, p_birthday, p_numberPhone, p_email, p_roleUser);
    SET p_result = 'Success';
END$$

/* Task 2: Update account */
CREATE PROCEDURE updateInfo(
    IN p_userID varchar(10), IN p_userName varchar(200), IN p_ID char(12),
    IN p_birthDay date, IN p_numberPhone varchar(15), IN p_email varchar(100),
    IN p_passwordHashNew varchar(200), OUT p_result varchar(200)
)
BEGIN
    DECLARE exit handler for sqlexception BEGIN SET p_result = "Error"; END;
    
    UPDATE USERACCOUNTS SET
        userName = COALESCE(p_userName, userName),
        ID = COALESCE(p_ID, ID),
        birthDay = COALESCE(p_birthDay, birthDay),
        numberPhone = COALESCE(p_numberPhone, numberPhone),
        email = COALESCE(p_email, email),
        passWordHash = COALESCE(p_passwordHashNew, passWordHash)
        WHERE userID = p_userID;

    IF ROW_COUNT() > 0 THEN SET p_result = "Success";
    ELSE SET p_result = "Not found user or no changes made"; END IF;
END$$

/* Task 3: Withdraw money */
CREATE PROCEDURE withDrawMoney (
    IN p_cardNumber char(16), IN p_amount decimal(15, 2),
    IN p_transactionId char(30), OUT p_result INT 
)
proc: BEGIN
    DECLARE v_numberAccount varchar(10) default NULL;
    DECLARE v_balance decimal(15, 2) default 0;
    DECLARE v_role varchar(20) default NULL;
    DECLARE exit handler for sqlexception
    BEGIN
        ROLLBACK;
        UPDATE BANKTRANSACTIONS SET stateOfTransaction = 'Cancel' WHERE transactionId = p_transactionId;
        SET p_result = 4;
    END;
    
    SET p_result = 4;
    IF p_amount <= 0 THEN SET p_result = 4; LEAVE proc; END IF;
    
    SELECT numberAccount INTO v_numberAccount FROM BANKCARDS WHERE cardNumber = p_cardNumber;
    IF v_numberAccount is null THEN SET p_result = 1; LEAVE proc; END IF;
    
    SELECT u.roleUser INTO v_role FROM ACCOUNTBANK a JOIN USERACCOUNTS u ON a.userID = u.userID WHERE a.numberAccount = v_numberAccount;
    IF v_role <> 'Client' THEN SET p_result = 5; LEAVE proc; END IF;
    
    INSERT INTO BANKTRANSACTIONS VALUES (p_transactionId, NOW(), p_amount, 'Processing', 'W001', v_numberAccount, v_numberAccount);
    
    START TRANSACTION;
    SELECT balance INTO v_balance FROM ACCOUNTBANK WHERE numberAccount = v_numberAccount FOR UPDATE;
    
    IF v_balance < p_amount THEN
        SET p_result = 3; ROLLBACK;
        UPDATE BANKTRANSACTIONS SET stateOfTransaction = 'Cancel' WHERE transactionId = p_transactionId;
        LEAVE proc;
    END IF;
    
    UPDATE ACCOUNTBANK SET balance = balance - p_amount WHERE numberAccount = v_numberAccount;
    UPDATE BANKTRANSACTIONS SET stateOfTransaction = 'Success' WHERE transactionId = p_transactionId;
    SET p_result = 0;
    COMMIT;
END$$

/* Task 4: Transfer money */
CREATE PROCEDURE transferMoney(
    IN p_numberAccount varchar(10), IN p_destinationAccount varchar(10),
    IN p_amount decimal(15,2), OUT p_result varchar(200)
)
BEGIN
    DECLARE v_balance decimal(15,2);
    DECLARE v_state enum("Active","Blocked");
    DECLARE v_state_des enum("Active","Blocked");
    DECLARE exit handler for sqlexception 
    BEGIN ROLLBACK; SET p_result = "Error transaction"; END;
    
    SELECT balance, state INTO v_balance, v_state FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;
    SELECT state INTO v_state_des FROM ACCOUNTBANK WHERE numberAccount = p_destinationAccount;
    
    IF v_state is null THEN SET p_result ="Not found account";
    ELSEIF v_state_des is null THEN SET p_result ="Not found account destination";
    ELSEIF v_state_des != "Active" THEN SET p_result = "The account destination is not active";
    ELSEIF v_state!="Active" THEN SET p_result = "This account is blocked";
    ELSEIF p_amount <= 0 THEN SET p_result = "Amount must be > 0";
    ELSEIF p_amount > v_balance THEN SET p_result = "Not enough balance";
    ELSE
        START TRANSACTION;
            UPDATE ACCOUNTBANK SET balance = balance - p_amount WHERE numberAccount = p_numberAccount;
            UPDATE ACCOUNTBANK SET balance = balance + p_amount WHERE numberAccount = p_destinationAccount;
            INSERT INTO BANKTRANSACTIONS(transactionID, amount, stateOfTransaction, typeofTransactionCode, numberAccount, destinationAccount) 
            VALUES (CONCAT("TRSF",random_string(10)), p_amount, "Success", "T001", p_numberAccount, p_destinationAccount);
        COMMIT;
        SET p_result = "Success";
    END IF;
END$$

/* Task 5: Check balance */
CREATE PROCEDURE checkBalance (
    IN p_numberAccount varchar(10), OUT p_balance decimal(15, 2), OUT p_result int 
)
proc: BEGIN
    DECLARE v_state ENUM('Active','Blocked');
    SET p_result = 4; SET p_balance = null;
    SELECT state, balance INTO v_state, p_balance FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;
    
    IF v_state is null THEN SET p_result = 1; SET p_balance = NULL; LEAVE proc; END IF;
    IF v_state = "Blocked" THEN SET p_result = 3; SET p_balance = NULL; LEAVE proc; END IF;
    SET p_result = 0;
END$$

/* Task 6: Check transaction */
CREATE PROCEDURE checkTransaction(
    IN p_numberAccount varchar(10), OUT p_result varchar(200)
)
BEGIN
    DECLARE v_state enum("Active","Blocked");
    SELECT state INTO v_state FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;
    IF v_state is null THEN SET p_result ="Not found account";
    ELSEIF v_state!="Active" THEN SET p_result = "This account is blocked";
    ELSE 
        SELECT * FROM BANKTRANSACTIONS WHERE numberAccount = p_numberAccount or destinationAccount = p_numberAccount ORDER BY created_at DESC;
        SET p_result = "Success";
    END IF;
END$$

/* Task 7: Deposit money */
CREATE PROCEDURE depositMoney(
    IN p_staffID VARCHAR(10), IN p_numberAccountUser VARCHAR(10), 
    IN p_transactionId CHAR(30), IN p_amount DECIMAL(15,2), OUT p_result INT
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

    SELECT u.ROLEUSER INTO v_role FROM ACCOUNTBANK a JOIN USERACCOUNTS u ON a.USERID = u.USERID WHERE a.NUMBERACCOUNT = p_numberAccountUser;
    IF v_role IS NULL THEN SET p_result = 1; LEAVE proc; END IF;
    IF v_role <> 'Client' THEN SET p_result = 5; LEAVE proc; END IF;

    SELECT ROLEUSER INTO v_role FROM USERACCOUNTS WHERE USERID = p_staffID;
    IF v_role <> 'Staff' THEN SET p_result = 5; LEAVE proc; END IF;

    INSERT INTO BANKTRANSACTIONS (TRANSACTIONID, AMOUNT, STATEOFTRANSACTION, TYPEOFTRANSACTIONCODE, NUMBERACCOUNT, DESTINATIONACCOUNT)
    VALUES (p_transactionId, p_amount, 'Processing', 'D001', p_numberAccountUser, NULL);

    START TRANSACTION;
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

/* Task 8: Block account */
CREATE PROCEDURE blockAccount(
    IN p_numberAccount varchar(10), OUT p_result varchar(200)
)
BEGIN
    DECLARE v_state enum("Active","Blocked");
    SELECT state INTO v_state FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;
    
    IF v_state is null THEN SET p_result ="Not found account";
    ELSEIF v_state != "Active" THEN SET p_result = "This account is already blocked";
    ELSE
        UPDATE ACCOUNTBANK SET state = "Blocked" WHERE numberAccount = p_numberAccount;
        SET p_result = "Success";
    END IF;
END$$

/* Task 9: Create accountBank */
CREATE PROCEDURE createBankAccount (
    IN p_numberAccount VARCHAR(12), IN p_pinCodeHash VARCHAR(64), IN p_userID VARCHAR(10), OUT p_result INT
)
proc: BEGIN
    DECLARE v_exist INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION BEGIN ROLLBACK; SET p_result = 2; END;

    SET p_result = 2;
    START TRANSACTION;

    SELECT COUNT(*) INTO v_exist FROM USERACCOUNTS WHERE userID = p_userID;
    IF v_exist = 0 THEN SET p_result = 1; ROLLBACK; LEAVE proc; END IF;

    SELECT COUNT(*) INTO v_exist FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;
    IF v_exist > 0 THEN SET p_result = 3; ROLLBACK; LEAVE proc; END IF;

    INSERT INTO ACCOUNTBANK (numberAccount, userID, pinCodeHash, balance, state, created_at)
    VALUES (p_numberAccount, p_userID, p_pinCodeHash, 0, 'Active', NOW());

    SET p_result = 0;
    COMMIT;
END$$

/* Task 10: Change Account PIN */
CREATE PROCEDURE changeAccountPin(
    IN p_numberAccount varchar(10), IN p_newPinHash varchar(64), OUT p_result varchar(200)
)
BEGIN
    DECLARE v_state enum("Active","Blocked");
    DECLARE exit handler for sqlexception BEGIN SET p_result = "Error"; END;
    
    SELECT state INTO v_state FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;
    
    IF v_state is null THEN SET p_result = "Not found account";
    ELSEIF v_state != "Active" THEN SET p_result = "This account is blocked";
    ELSE
        UPDATE ACCOUNTBANK SET pinCodeHash = p_newPinHash WHERE numberAccount = p_numberAccount;
        SET p_result = "Success";
    END IF;
END$$

/* Task 11: Search transaction by date range */
CREATE PROCEDURE searchTransactionByDate(
    IN p_numberAccount varchar(10), IN p_fromDate datetime, IN p_toDate datetime, OUT p_result varchar(200)
)
BEGIN
    DECLARE v_state enum("Active","Blocked");
    SELECT state INTO v_state FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;

    IF v_state is null THEN SET p_result = "Not found account";
    ELSEIF v_state != "Active" THEN SET p_result = "This account is blocked";
    ELSE
        SELECT transactionId, created_at, amount, stateOfTransaction, typeOfTransactionCode, numberAccount, destinationAccount
        FROM BANKTRANSACTIONS
        WHERE (numberAccount = p_numberAccount or destinationAccount = p_numberAccount) AND created_at between p_fromDate and p_toDate
        ORDER BY created_at desc;
        SET p_result = "Success";
    END IF;
END$$

/* Task 12: Search user by phone or ID */
CREATE PROCEDURE searchUser(
    IN p_keyword varchar(100), OUT p_result varchar(200)
)
BEGIN
    DECLARE v_count int default 0;
    SELECT count(*) INTO v_count FROM USERACCOUNTS WHERE numberPhone like concat('%', p_keyword, '%') or ID like concat('%', p_keyword, '%');

    IF v_count = 0 THEN SET p_result = "Not found";
    ELSE
        SELECT userID, userName, ID, birthDay, numberPhone, email, roleUser
        FROM USERACCOUNTS
        WHERE numberPhone like concat('%', p_keyword, '%') or ID like concat('%', p_keyword, '%');
        SET p_result = "Success";
    END IF;
END$$

/* Task 13: Unblock account */
CREATE PROCEDURE unblockAccount(
    IN p_numberAccount varchar(10), OUT p_result varchar(200)
)
BEGIN
    DECLARE v_state enum("Active","Blocked");
    SELECT state INTO v_state FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;
    
    IF v_state is null THEN SET p_result ="Not found account";
    ELSEIF v_state != "Blocked" THEN SET p_result = "This account is already active";
    ELSE
        UPDATE ACCOUNTBANK SET state = "Active" WHERE numberAccount = p_numberAccount;
        SET p_result = "Success";
    END IF;
END$$

/* Task 14: Create Card for Account */
CREATE PROCEDURE createCard(
    IN p_cardNumber char(16), IN p_numberAccount varchar(10), IN p_pinCode varchar(64), IN p_ccv char(3), OUT p_result int
)
proc: BEGIN
    DECLARE v_accountExists int default 0;
    DECLARE exit handler for sqlexception BEGIN SET p_result = 2; END;
    
    SET p_result = 2;
    SELECT count(*) INTO v_accountExists FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;
    
    IF v_accountExists = 0 THEN SET p_result = 1; LEAVE proc; END IF;
    
    INSERT INTO BANKCARDS (cardNumber, cardPinCodeHash, created_at, expire_at, secureCode, numberAccount)
    VALUES (p_cardNumber, p_pinCode, curdate(), date_add(curdate(), interval 5 year), p_ccv, p_numberAccount);
    SET p_result = 0;
END$$

/* Task 15: View Audit Logs */
CREATE PROCEDURE viewAuditLogs(
    IN p_userID varchar(10), OUT p_result varchar(200)
)
BEGIN
    DECLARE v_exists int;
    SELECT count(*) INTO v_exists FROM USERACCOUNTS WHERE userID = p_userID;
    
    IF v_exists = 0 THEN SET p_result = "User not found";
    ELSE
        SELECT logId, actionType, oldValue, newValue, changedAt 
        FROM USERAUDITLOGS WHERE userID = p_userID ORDER BY changedAt desc;
        SET p_result = "Success";
    END IF;
END$$

/* Task 16: Statistics total money in system */
CREATE PROCEDURE getSystemStatistics(
    OUT p_totalUsers int, OUT p_totalAccounts int, OUT p_totalBalance double
)
BEGIN
    SELECT count(*) INTO p_totalUsers FROM USERACCOUNTS;
    SELECT count(*) INTO p_totalAccounts FROM ACCOUNTBANK;
    SELECT COALESCE(sum(balance), 0) INTO p_totalBalance FROM ACCOUNTBANK;
END$$

/* Log Staff Actions */
CREATE PROCEDURE logStaffAction(
    IN p_staffID varchar(10), IN p_actionType varchar(100), IN p_targetInfo varchar(500)
)
BEGIN
    INSERT INTO STAFFAUDITLOGS (staffID, actionType, targetInfo) VALUES (p_staffID, p_actionType, p_targetInfo);
END$$

DELIMITER ;