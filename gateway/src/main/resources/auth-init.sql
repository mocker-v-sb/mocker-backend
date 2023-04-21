create table if not exists users
(
    id uuid not null primary key,
    email varchar not null,
    password varchar not null,
    UNIQUE(email)
);
