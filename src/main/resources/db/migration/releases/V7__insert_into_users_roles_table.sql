--liquibase formatted sql

--changeset bankcards_service:7
INSERT INTO auth.users_roles (user_id, role_id) 
VALUES('72f2aab1-12d8-4c1a-aafd-cda7439e8ec4', '5ae3a386-059c-4739-b13f-a9a1761968aa'),
('5ae3a386-059c-4739-b13f-a9a1761968aa', '04388159-d878-413f-af13-5c35ef80f5e2');