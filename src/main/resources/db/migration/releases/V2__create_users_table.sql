--liquibase formatted sql

--changeset bankcards_service:2
CREATE TABLE IF NOT EXISTS auth.users
(
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    username varchar(255) UNIQUE NOT NULL,
    password varchar(255) NOT NULL,
    account_non_expired boolean NOT NULL,
    account_non_locked boolean NOT NULL,
    credentials_non_expired boolean NOT NULL,
    enabled boolean NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);