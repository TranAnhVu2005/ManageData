create database MANAGEBANKACCOUNT;
use MANAGEBANKACCOUNT;

/*Start create and modify database*/
create table USERACCOUNTS (
	userID varchar(10) primary key,
    userName varchar(200) not null,
    ID char(12) not null unique,
    passWordHash varchar(200) not null,
    birthDay date not null,
    numberPhone varchar(15) not null unique,
    email varchar(100) not null unique,
    roleUser varchar(20) not null
);


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

create table BANKTRANSACTIONS(
	transactionId char(30) primary key,
    created_at datetime not null default current_timestamp,
    amount decimal(15,2) not null default 0 check (amount >=0),
    stateOfTransaction enum("Processing", "Success", "Cancel") not null default "Processing",
    typeOfTransactionCode char(4) not null,
    numberAccount varchar(10) not null,
    foreign key (typeOfTransactionCode) references TYPEOFTRANSACTION(typeOfTransactionCode) on update cascade on delete cascade,
    foreign key (numberAccount) references ACCOUNTBANK(numberAccount) on update cascade on delete cascade
);

alter table BANKTRANSACTIONS add column destinationAccount varchar(10) not null;
alter table BANKTRANSACTIONS add foreign key  (destinationAccount) references accountbank(numberAccount) on update cascade on delete cascade;
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


/*Start Task 2: Update account* - Vũ*/
delimiter $$
create procedure updateInfo(
	IN p_userID varchar(10),
	IN p_userName varchar(200),
    IN p_ID char(12),
    IN p_birthDay date,
    IN p_numberPhone varchar(15),
    IN p_email varchar(100),
    IN p_passwordHashOld varchar(200),
    IN p_passwordHashNew varchar(200),
    
    IN p_numberAccount varchar(10),
    OUT p_result varchar(200)
)
begin
	declare v_passwordHashOld varchar(200);
    declare v_state varchar(10); 
    declare v_countUser int;
    
    declare exit handler for sqlexception
    begin
		set p_result = "Error";
    end;
    
    SELECT passWordHash INTO v_passwordHashOld FROM USERACCOUNTS WHERE userID = p_userID;
	SELECT state INTO v_state FROM ACCOUNTBANK WHERE  numberAccount = p_numberAccount;
    SELECT COUNT(*) INTO v_countUser FROM USERACCOUNTS WHERE userID = p_userID;
    
    IF v_countUser = 0 THEN
		set p_result = "Not found user";
	ELSEIF p_passwordHashOld != v_passwordHashOld THEN
		set p_result = "Incorrect password";
	ELSEIF v_state != "Active" THEN
		set p_result = "This account blocked!";
	ELSE
		UPDATE USERACCOUNTS SET
			userName = COALESCE(p_userName, userName),
            ID = COALESCE(p_ID, ID),
            birthDay = COALESCE(p_birthDay, birthDay),
            numberPhone = COALESCE(p_numberPhone, numberPhone),
            email = COALESCE(p_email, email),
            passWordHash = COALESCE(p_passwordHashNew, passWordHash)
			WHERE userID = p_userID;
		set p_result = "Success";
	END IF;
end$$
delimiter ;

/*End Task 2: Update account* - Vũ */

create table BANKTRANSACTIONS(
	transactionId char(30) primary key,
    created_at datetime not null default current_timestamp,
    amount decimal(15,2) not null default 0 check (amount >=0),
    stateOfTransaction enum("Processing", "Success", "Cancel") not null default "Processing",
    typeOfTransactionCode char(4) not null,
    numberAccount varchar(10) not null,
    foreign key (typeOfTransactionCode) references TYPEOFTRANSACTION(typeOfTransactionCode) on update cascade on delete cascade,
    foreign key (numberAccount) references ACCOUNTBANK(numberAccount) on update cascade on delete cascade
);
/*Start Task 3: Withdraw money* - Lợi*/
delimiter $$
create procedure withDrawMoney (
	IN p_cardNumber char(16),
    IN p_cardPinCodeHash varchar(64),
    IN p_amount decimal(15, 2),
    IN p_transactionId char(30),
    OUT p_result INT # 0: success, 1: no card number, 2: wrong pin, 3: not enough balance, 4: error by server, 5: not authorization
)
proc: begin
	declare v_cardPinCodeHash varchar(64) default NULL;
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
    
    select cardPinCodeHash, numberAccount
    into v_cardPinCodeHash, v_numberAccount
    from CARDS
    where cardNumber = p_cardNumber;

    if v_cardPinCodeHash is null then
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
    
    select cardPinCodeHash
    into v_cardPinCodeHash
    from CARDS
    where cardNumber = p_cardNumber
    for update;
    
    if v_cardPinCodeHash <> p_cardPinCodeHash then
		set p_result = 2;
        rollback;
        update BANKTRANSACTIONS
			set stateOfTransaction = 'Cancel'
			where transactionId = p_transactionId;
        leave proc;
	end if;
    
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

/*End Task 4: Transfer money* - Vũ*/



/*Start Task 5: Check balance* - Lợi*/
delimiter $$
create procedure checkBalance (
	IN p_numberAccount varchar(10),
    IN p_pinCodeHash varchar(64),
    OUT p_balance decimal(15, 2),
    OUT p_result int # 0:success, 1:no account, 2:wrong pin, 3:blocked, 4: server error
)
proc: begin
	declare v_pinCodeHash varchar(64) default NULL;
    DECLARE v_state ENUM('Active','Blocked');
    DECLARE CONTINUE HANDLER FOR NOT FOUND
	BEGIN
		SET v_pinCodeHash = NULL;
	END;
    
    set p_result = 4;
    set p_balance = null;
    
    select pinCodeHash, state, balance
    into v_pinCodeHash, v_state, p_balance
    from ACCOUNTBANK
    where numberAccount = p_numberAccount;
    
    if v_pinCodeHash is null then
		set p_result = 1;
        set p_balance = NULL;
        leave proc;
	end if;
    
    if v_pinCodeHash <> p_pinCodeHash then
		set p_result = 2;
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



/*Start Task 6: Check transaction* - Vũ/

/*End Task 6: Check transaction* - Vũ/



/*Start Task 7: Deposit money into an account * - Lợi*/
delimiter $$
create procedure depositMoney (
	IN p_numberAccountStaff varchar(10),
	IN p_numberAccountUser varchar(10),
    IN p_transactionId char(30),
    IN p_amount decimal(15, 2),
    OUT p_result int # 0:success, 1: no account, 2: server error, 5:not authorization
)
proc: begin
	declare v_role varchar(30) default null;
    declare exit handler for sqlexception
    begin
		rollback;
        update BANKTRANSACTIONS
			set stateOfTransaction = 'Cancel'
			where transactionId = p_transactionId;
		set p_result = 2;
    end;
    
    set p_result = 2;
    
    #Kiem tra so tien nap co am
    if p_amount <= 0 then
		set p_result = 2;
		leave proc;
	end if;
    
    #Kiem tra phan quyen cua tai khoan client
    select u.roleUser
		into v_role
		from ACCOUNTBANK a
		join USERACCOUNTS u on a.userID = u.userID
		where a.numberAccount = p_numberAccountUser;
        
	if v_role is null then
        set p_result = 1;
        leave proc;
	end if;
        
    if v_role <> 'Client' then
        set p_result = 5;
        leave proc;
	end if;
    
    #Kiem tra phan quyen cua tai khoan
    select u.roleUser
		into v_role
		from ACCOUNTBANK a
		join USERACCOUNTS u on a.userID = u.userID
		where a.numberAccount = p_numberAccountStaff;
        
	if v_role is null then
        set p_result = 1;
        leave proc;
	end if;
    
    if v_role <> 'Staff' then
        set p_result = 5;
        leave proc;
	end if;
    
    
    INSERT INTO BANKTRANSACTIONS
    VALUES (p_transactionId, NOW(), p_amount, 'Processing', 'D001', p_numberAccountStaff, p_numberAccountUser);
    
    start transaction;
    
    UPDATE ACCOUNTBANK
    SET balance = balance + p_amount
    WHERE numberAccount = p_numberAccountUser;

    IF ROW_COUNT() = 0 THEN
        SET p_result = 1;
        ROLLBACK;
        update BANKTRANSACTIONS
			set stateOfTransaction = 'Cancel'
			where transactionId = p_transactionId;
        LEAVE proc;
	else
		update BANKTRANSACTIONS
			set stateOfTransaction = 'Success'
			where transactionId = p_transactionId;
    END IF;
    
    commit;
end$$
delimiter ;

/*End Task 7: Deposit money into an account * - Lợi*/



/*Start Task 8: Delete account* - Vũ*/

/*Start Task 8: Delete account* - Vũ*/