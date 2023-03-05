CREATE TABLE `service`
(
    `id`              BIGINT(16) NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(128) NOT NULL,
    `url`             VARCHAR(128) NULL,
    `create_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expiration_time` TIMESTAMP NULL DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_name` (`name` ASC) VISIBLE,
    INDEX             `idx_url` (`url` ASC) VISIBLE
);
