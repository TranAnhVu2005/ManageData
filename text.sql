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

/*Task 1: Create account*/



/*Task 2: Update account*/
/*Task 3: Ưithdraw money*/
/*Task 4: Transfer money*/
/*Task 5: Check balance*/
/*Task 6: Create account*/
/*Task 7: Create account*/
/*Task 8: Create account*/
