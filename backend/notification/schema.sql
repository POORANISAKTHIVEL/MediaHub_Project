-- Run this once against the mediahub_Notification_and_alert database.
-- Needed because application.properties has spring.jpa.hibernate.ddl-auto=none,
-- so Hibernate will NOT create tables automatically.

CREATE DATABASE IF NOT EXISTS mediahub_Notification_and_alert;
USE mediahub_Notification_and_alert;

CREATE TABLE IF NOT EXISTS notification (
    notification_id BIGINT        NOT NULL AUTO_INCREMENT,
    user_id         BIGINT        NOT NULL,
    message         VARCHAR(2000) NOT NULL,
    category        VARCHAR(20)   NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    created_date    DATETIME,
    PRIMARY KEY (notification_id)
);
