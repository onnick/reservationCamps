create table app_user
(
    id         uuid primary key,
    email      varchar(320) not null unique,
    role       varchar(32)  not null,
    created_at timestamp    not null
);

create table camp
(
    id               uuid primary key,
    name             varchar(200) not null,
    base_price_cents int          not null,
    created_at       timestamp    not null
);

create table camp_session
(
    id         uuid primary key,
    camp_id    uuid      not null,
    start_date date      not null,
    end_date   date      not null,
    capacity   int       not null,
    created_at timestamp not null,
    constraint camp_session_camp_fk foreign key (camp_id) references camp (id)
);

create table reservation
(
    id           uuid primary key,
    session_id   uuid      not null,
    user_id      uuid      not null,
    status       varchar(32) not null,
    created_at   timestamp not null,
    confirmed_at timestamp,
    paid_at      timestamp,
    cancelled_at timestamp,
    constraint reservation_session_fk foreign key (session_id) references camp_session (id),
    constraint reservation_user_fk foreign key (user_id) references app_user (id),
    constraint reservation_unique_user_session unique (session_id, user_id)
);

create index reservation_session_status_idx on reservation (session_id, status);

