use milou_db;
create table users (
    id int primary key auto_increment,
    email varchar(50) not null unique ,
    password varchar(50) not null
);
alter table users
add column name varchar(50) not null ;
create table email (
    id int primary key auto_increment ,
    date datetime not null default current_timestamp ,
    sender_id int not null ,
    subject nvarchar(50) not null ,
    body nvarchar(500) not null ,
    code nvarchar(10) not null unique ,
    foreign key (sender_id) references users(id)
);
create table email_recipient (
    id int primary key auto_increment ,
    email_id int not null ,
    recipient_id int not null ,
    status enum('unread', 'read') default 'unread',
    foreign key (email_id) references email(id) ,
    foreign key (recipient_id) references users(id)

);
select * from users;

