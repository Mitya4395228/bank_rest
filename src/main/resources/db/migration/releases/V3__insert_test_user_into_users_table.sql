--liquibase formatted sql

--changeset bankcards_service:3
INSERT INTO auth.users (id, username, password, account_non_expired, account_non_locked, credentials_non_expired, enabled) 
VALUES('72f2aab1-12d8-4c1a-aafd-cda7439e8ec4', 'admin-test', '$2y$10$emZP3/24hxoGdvoFLHt/weBfnlOPpNrfq0X.1OxKW6IGn4r0e0wyy', true, true, true, true),
('5ae3a386-059c-4739-b13f-a9a1761968aa', 'user-test', '$2y$10$emZP3/24hxoGdvoFLHt/weBfnlOPpNrfq0X.1OxKW6IGn4r0e0wyy', true, true, true, true);