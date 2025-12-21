--liquibase formatted sql

--changeset bankcards_service:9
CREATE TABLE IF NOT EXISTS finance.cards
(
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    number varchar(255) UNIQUE NOT NULL,
    expiration_date date NOT NULL,
    status varchar(255) NOT NULL,
    balance decimal NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id uuid NOT NULL REFERENCES auth.users(id),
    PRIMARY KEY (id)
);