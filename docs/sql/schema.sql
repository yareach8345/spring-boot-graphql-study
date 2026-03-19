drop table if exists book;
drop table if exists writer;

create table if not exists writer(
    id int primary key auto_increment,
    name varchar(100) not null,
    description text
);

create table if not exists book(
    id int primary key auto_increment,
    title varchar(100) not null,
    description text,
    writer_id int,

    foreign key(writer_id) references writer(id) on delete set null
);