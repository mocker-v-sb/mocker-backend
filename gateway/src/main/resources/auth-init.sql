create table if not exists users
(
    id uuid not null primary key,
    email varchar not null,
    password varchar not null,
    UNIQUE(email)
);

create table if not exists refresh_tokens
(
    id uuid not null primary key,
    token varchar not null
);
echo "" > $(docker inspect --format='{{.LogPath}}' <>)
