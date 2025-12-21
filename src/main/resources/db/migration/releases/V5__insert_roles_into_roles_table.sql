--liquibase formatted sql

--changeset bankcards_service:5
INSERT INTO auth.roles (id, role) 
VALUES('5ae3a386-059c-4739-b13f-a9a1761968aa', 'ADMIN'),
('04388159-d878-413f-af13-5c35ef80f5e2', 'USER');