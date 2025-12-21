--liquibase formatted sql

--changeset bankcards_service:6
CREATE TABLE IF NOT EXISTS auth.users_roles
(
    user_id uuid NOT NULL REFERENCES auth.users(id),
    role_id uuid NOT NULL REFERENCES auth.roles(id),
    PRIMARY KEY (user_id, role_id)
);