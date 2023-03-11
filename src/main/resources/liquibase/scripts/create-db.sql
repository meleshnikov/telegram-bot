-- liquibase formatted sql

-- changeset andrew:1
CREATE TABLE notification_task
(
    id           serial PRIMARY KEY,
    chat_id      bigint,
    notification text,
    date_time    timestamp
);