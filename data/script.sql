create table users
(
    username char(255) not null
        primary key,
    password char(255) not null,
    name     char(255) not null,
    surname  char(255) not null,
    role     int       not null
);

create table fields
(
    field_id       varchar(255)         not null
        primary key,
    name           varchar(255)         not null,
    sport          int                  not null,
    address        varchar(255)         not null,
    city           varchar(255)         not null,
    price_per_hour decimal(10, 2)       not null,
    availability   text                 null,
    indoor         tinyint(1) default 0 not null,
    manager_id     varchar(50)          null comment 'Username of field manager who owns this field',
    structure_name varchar(100)         null comment 'Name of the sports structure/facility',
    auto_approve   tinyint(1) default 0 null comment 'Auto-approve booking requests without manager intervention',
    constraint fk_field_manager
        foreign key (manager_id) references users (username)
            on delete set null,
    check ((`sport` >= 0) and (`sport` <= 7)),
    check (`price_per_hour` >= 0)
);

create table bookings
(
    booking_id         int auto_increment
        primary key,
    field_id           varchar(50)                                         not null,
    requester_username varchar(50)                                         not null,
    booking_date       date                                                not null,
    start_time         time                                                not null,
    end_time           time                                                not null,
    type               enum ('MATCH', 'PRIVATE') default 'MATCH'           not null,
    status             tinyint                   default 0                 not null,
    total_price        decimal(10, 2)                                      null,
    requested_at       timestamp                 default CURRENT_TIMESTAMP null,
    confirmed_at       timestamp                                           null,
    rejection_reason   varchar(500)                                        null,
    constraint fk_booking_field
        foreign key (field_id) references fields (field_id)
            on delete cascade,
    constraint fk_booking_requester
        foreign key (requester_username) references users (username)
            on delete cascade,
    constraint chk_booking_status
        check (`status` between 0 and 4),
    constraint chk_booking_times
        check (`start_time` < `end_time`)
);

create index idx_booking_date
    on bookings (booking_date);

create index idx_booking_field_status
    on bookings (field_id, status);

create index idx_booking_requester
    on bookings (requester_username);

create index idx_booking_status
    on bookings (status);

create index idx_fields_manager
    on fields (manager_id);

create table matches
(
    match_id              int auto_increment
        primary key,
    sport                 int           not null,
    match_date            date          not null,
    match_time            time          not null,
    city                  varchar(255)  not null,
    required_participants int           not null,
    organizer_username    char(255)     not null,
    field_id              varchar(255)  null,
    status                int default 0 not null,
    participants          json          null,
    booking_id            int           null comment 'Reference to booking if field was booked',
    constraint fk_match_booking
        foreign key (booking_id) references bookings (booking_id)
            on delete set null,
    constraint organizer_username
        foreign key (organizer_username) references users (username),
    check (`required_participants` >= 2),
    check ((`sport` >= 0) and (`sport` <= 7)),
    check ((`status` >= 0) and (`status` <= 2))
);

create table time_slots
(
    slot_id     int auto_increment
        primary key,
    field_id    varchar(50)                                                 not null,
    day_of_week tinyint                                                     not null,
    start_time  time                                                        not null,
    end_time    time                                                        not null,
    status      enum ('AVAILABLE', 'BOOKED', 'BLOCKED') default 'AVAILABLE' null,
    booking_id  int                                                         null,
    constraint fk_slot_booking
        foreign key (booking_id) references bookings (booking_id)
            on delete set null,
    constraint fk_slot_field
        foreign key (field_id) references fields (field_id)
            on delete cascade,
    constraint chk_slot_day
        check (`day_of_week` between 1 and 7),
    constraint chk_slot_times
        check (`start_time` < `end_time`)
);

create index idx_slot_field_day
    on time_slots (field_id, day_of_week);

create index idx_slot_status
    on time_slots (status);


