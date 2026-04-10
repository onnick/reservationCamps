create table app_user
(
    id         uuid primary key,
    email      varchar(320) not null unique,
    role       varchar(32)  not null,
    created_at timestamptz  not null
);

create table camp
(
    id               uuid primary key,
    name             varchar(200) not null,
    base_price_cents int          not null,
    created_at       timestamptz  not null
);

create table camp_session
(
    id         uuid primary key,
    camp_id    uuid        not null references camp (id),
    start_date date        not null,
    end_date   date        not null,
    capacity   int         not null,
    created_at timestamptz not null
);

create table reservation
(
    id           uuid primary key,
    session_id   uuid        not null references camp_session (id),
    user_id      uuid        not null references app_user (id),
    status       varchar(32) not null,
    created_at   timestamptz not null,
    confirmed_at timestamptz,
    paid_at      timestamptz,
    cancelled_at timestamptz,
    constraint reservation_unique_user_session unique (session_id, user_id)
);

create index reservation_session_status_idx on reservation (session_id, status);

