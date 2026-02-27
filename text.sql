create database MANAGEBANKACCOUNT;
use MANAGEBANKACCOUNT;

/*Start create and modify database*/
create table USERBANK(
	userID varchar(10) primary key,
    userName varchar(200) not null,
    ID char(12) not null unique,
    birthDay date not null,
    numberPhone varchar(15) not null unique,
    email varchar(100) not null unique
);


create table ACCOUNTBANK(
	numberAccount varchar(10) primary key,
    userID varchar(10) not null,
    passWordHash varchar(200) not null,
    pinCodeHash char(64) not null,
    balance decimal(15,2) not null default 0 check (balance >=0),
    state enum("Active", "Blocked") not null DEFAULT "Active",
    created_at datetime DEFAULT NOW(),
    foreign key (userID) references USERBANK(userID) on update cascade on delete cascade
);

create table CARD (
	cardNumber char(16) primary key,
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

create table bankTransaction(
	transactionId char(30) primary key,
    created_at datetime not null default current_timestamp,
    amount decimal(15,2) not null default 0 check (amount >=0),
    stateOfTransaction enum("Processing", "Success", "Cancel") not null default "Processing",
    typeOfTransactionCode char(4) not null,
    numberAccount varchar(10) not null,
    foreign key (typeOfTransactionCode) references TYPEOFTRANSACTION(typeOfTransactionCode) on update cascade on delete cascade,
    foreign key (numberAccount) references ACCOUNTBANK(numberAccount) on update cascade on delete cascade
);

alter table bankTransaction add column destinationAccount varchar(10) not null;
alter table bankTransaction add foreign key  (destinationAccount) references accountbank(numberAccount) on update cascade on delete cascade;
/*End create and modify database*/

/*Start Task 1: Create account* - Lợi/

/*End Task 1: Create account* - Lợi/


/*Start Task 2: Update account* - Vũ*/
delimiter $$;
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
	declare passwordHashdOld varchar(200);
    declare state varchar(10); 
    declare countUser int;
    
    SELECT passwordHash INTO passwordHashOld FROM ACCOUNTBANK WHERE numberAccount = p_numberAccount;
	SELECT state INTO state FROM ACCOUNTBANK WHERE  numberAccount = p_numberAccount;
    SELECT COUNT(*) INTO countUser FROM USERBANK WHERE userID = p_userID;
    
    IF countUser = 0;
		set p_result = "Not found user"
	ELSE IF p_passwordHashOld != passwordHash
		set p_result = "Incorrect password"
	ELSE IF state != "Active"
		set p_result = "This account blocked!"
	ELSE 
		UPDATE USERBANK SET
			userName = COALESCE(p_userName, userName),
            ID = COALESCE(p_ID, ID),
            birthDay = COALESCE(p_birthDay, birthDay),
            numberPhone = COALESCE(p_numberPhone, numberPhone),
            email = COALESCE(p_email, email),
            passwordHash = COALESCE(p_passwordHashNew, passwordHash)
end$$
delimiter ;

/*End Task 2: Update account* - Vũ */



/*Start Task 3: Ưithdraw money* - Lợi*/

/*End Task 3: Ưithdraw money - Lợi*/



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