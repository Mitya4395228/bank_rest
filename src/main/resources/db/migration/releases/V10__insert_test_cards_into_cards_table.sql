--liquibase formatted sql

--changeset bankcards_service:10
INSERT INTO finance.cards (number, expiration_date, status, balance, user_id) VALUES
('avwmQ6j2gcZnGAllDjbdk2y7wOGB6BC6Er3hXJCIqk0=', '2027-01-01', 'ACTIVE', 1000, '72f2aab1-12d8-4c1a-aafd-cda7439e8ec4'),
('jVMYz7of+iRcdSlYjU85kmy7wOGB6BC6Er3hXJCIqk0=', '2027-01-01', 'ACTIVE', 1000, '5ae3a386-059c-4739-b13f-a9a1761968aa');