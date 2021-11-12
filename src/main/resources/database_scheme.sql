create table if not exists neighbours
(
    id int auto_increment,
    ip varchar(100) not null,
    port int not null,
    discovered_at datetime not null,
    last_connected_at datetime not null,
    trustable bool default true not null,
    constraint neighbours_pk
        primary key (id)
);
