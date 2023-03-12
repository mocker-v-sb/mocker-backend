CREATE TABLE `service`
(
    `id`              BIGINT(16)   NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(128) NOT NULL,
    `path`            VARCHAR(128) NOT NULL,
    `url`             VARCHAR(128) NULL,
    `description`     VARCHAR(128) NULL,
    `creation_time`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expiration_time` TIMESTAMP    NULL     DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_name` (`name` ASC) VISIBLE,
    UNIQUE INDEX `idx_path` (`path` ASC) VISIBLE,
    UNIQUE INDEX `idx_url` (`url` ASC) VISIBLE
);

CREATE TABLE `model`
(
    `id`            BIGINT(16)   NOT NULL AUTO_INCREMENT,
    `service_id`    BIGINT(16)   NOT NULL,
    `name`          VARCHAR(128) NOT NULL,
    `description`   VARCHAR(128) NULL,
    `creation_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `model_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`) ON DELETE CASCADE
);