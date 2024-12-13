create sequence city_id_seq
    start with 1
    increment by 1
    no minvalue
    no maxvalue
    cache 1;

create table city
(
    id         bigint primary key default nextval('city_id_seq'),
    name       varchar(100),
    country    varchar(100),
    latitude   decimal,
    longitude  decimal,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

