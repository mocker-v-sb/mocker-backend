create table if not exists users
(
    id uuid not null primary key,
    username varchar not null,
    password varchar not null
);
