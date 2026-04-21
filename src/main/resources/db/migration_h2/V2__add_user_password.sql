alter table app_user
    add column password_hash varchar(100) not null default '';

