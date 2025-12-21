--liquibase formatted sql

--changeset bankcards_service:4
CREATE TABLE IF NOT EXISTS auth.roles
(
    id uuid NOT NULL DEFAULT gen_random_uuid(),
    role varchar(255) UNIQUE NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);