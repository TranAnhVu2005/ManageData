create database MANAGEBANKACCOUNT;
use MANAGEBANKACCOUNT;
create table USERBANK(
	userID varchar(10) primary key,
    name varchar(200) not null,
    ID char(12) not null unique,
    birthDay date not null,
    numberPhone varchar(10) not null unique,
    email varchar(100) not null unique
);


create table ACCOUNTBANK(
	numberAccount varchar(10) primary key,
    userID varchar(10) not null,
    passWord varchar(200) not null,
    pinCode char(6) not null,
    balance decimal(15,2) not null default 0 check (balance >=0),
    state enum("Active", "Blocked", "Deleted") not null DEFAULT "Active",
    create_at datetime DEFAULT NOW(),
    foreign key (userID) references USERBANK(userID)
);