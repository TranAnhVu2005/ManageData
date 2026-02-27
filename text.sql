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
    email varchar(100) not null unique
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

/*Start Task 1: Create account* - Lợi*/

delimiter $$
create procedure createUserAccount (
	IN p_userID varchar(10),
	IN p_userName varchar(200),
    IN p_ID char(12),
    IN p_birthday date,
    IN p_numberPhone varchar(15),
    IN p_email varchar(100),
    IN p_passwordHash varchar(2000)
)
begin
	insert into useraccounts values (p_userID, p_userName, p_ID, p_birthday, p_numberPhone, p_email, p_passwordHash);
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


/*Start Task 3: Withdraw money* - Lợi*/
delimiter $$
create procedure withDrawMoney (
	IN p_cardNumber char(16),
    IN p_cardPinCodeHash varchar(64),
    IN p_amount decimal(15, 2),
    OUT p_result INT # 0: success, 1: no card number, 2: wrong pin, 3: not enough balance, 4: error by server
)
proc: begin
	declare v_cardPinCodeHash varchar(64) default NULL;
    declare v_numberAccount varchar(10);
    declare v_balance decimal(15, 2);
    
    set p_result = 4;
    
    start transaction;
    
    select cardPinCodeHash, numberAccount
    into v_cardPinCodeHash, v_numberAccount
    from CARDS
    where cardNumber = p_cardNumber
    for update;
    
    if v_cardPinCodeHash is NULL then
		set p_result = 1;
        rollback;
        leave proc;
	end if;
    
    if v_cardPinCodeHash <> p_cardPinCodeHash then
		set p_result = 2;
        rollback;
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
        leave proc;
	end if;
    
    update ACCOUNTBANK set balance = balance - p_amount where numberAccount = v_numberAccount;
    commit;
end$$
delimiter ;
/*End Task 3: Withdraw money - Lợi*/



/*Start Task 4: Transfer money* - Vũ*/

/*End Task 4: Transfer money* - Vũ*/



/*Start Task 5: Check balance* - Lợi*/

/*End Task 5: Check balance* - Lợi*/



/*Start Task 6: Check transaction* - Vũ/

/*End Task 6: Check transaction* - Vũ/



/*Start Task 7: Deposit money into an account * - Lợi/

/*End Task 7: Deposit money into an account * - Lợi/



/*Start Task 8: Delete account* - Vũ/

/*Start Task 8: Delete account* - Vũ/