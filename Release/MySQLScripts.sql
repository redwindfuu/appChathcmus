Create database qltk
use qltk
create table if not exists taikhoan
(
    USERNAME varchar(45) not null
        constraint `PRIMARY`
        primary key,
    PASSWORD varchar(45) null,
    TEN      varchar(45) null,
    SDT      varchar(15) null,
    DIACHI   varchar(45) null,
    EMAIL    varchar(45) null
);