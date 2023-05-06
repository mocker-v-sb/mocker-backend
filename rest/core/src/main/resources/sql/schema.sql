CREATE TABLE `service`
(
    `id`              BIGINT(16)   NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(128) NOT NULL,
    `path`            VARCHAR(128) NOT NULL UNIQUE,
    `url`             VARCHAR(128) NULL,
    `description`     VARCHAR(128) NULL,
    `creation_time`   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expiration_time` TIMESTAMP    NULL     DEFAULT NULL,
    `proxy_enabled`   BOOLEAN      NOT NULL DEFAULT FALSE,
    `history_enabled` BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`id`)
);

CREATE TABLE `model`
(
    `id`            BIGINT(16)   NOT NULL AUTO_INCREMENT,
    `service_id`    BIGINT(16)   NOT NULL,
    `name`          VARCHAR(128) NOT NULL,
    `description`   VARCHAR(128) NULL,
    `response_type` INT(8)       NOT NULL,
    `response`      MEDIUMTEXT   NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `model_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`) ON DELETE CASCADE
);

CREATE TABLE `mock`
(
    `id`                BIGINT(16)    NOT NULL AUTO_INCREMENT,
    `service_id`        BIGINT(16)    NOT NULL,
    `name`              VARCHAR(128)  NOT NULL,
    `description`       VARCHAR(128)  NULL,
    `path`              VARCHAR(128)  NOT NULL UNIQUE,
    `method`            INT(8)        NOT NULL,
    `request_model_id`  BIGINT(16)    NULL,
    `response_model_id` BIGINT(16)    NULL,
    `request_headers`   VARCHAR(2048) NULL,
    `response_headers`  VARCHAR(2048) NULL,
    `query_params`      VARCHAR(2048) NULL,
    `path_params`       VARCHAR(2048) NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `mock_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`) ON DELETE CASCADE,
    CONSTRAINT `mock_ibfk_2` FOREIGN KEY (`request_model_id`) REFERENCES `model` (`id`),
    CONSTRAINT `mock_ibfk_3` FOREIGN KEY (`response_model_id`) REFERENCES `model` (`id`)
);

CREATE TABLE `mock_response`
(
    `id`               BIGINT(16)   NOT NULL AUTO_INCREMENT,
    `mock_id`          BIGINT(16)   NOT NULL,
    `name`             VARCHAR(128) NOT NULL,
    `status_code`      INT(8)       NOT NULL,
    `request_headers`  JSON         NULL,
    `response_headers` JSON         NULL,
    `path_params`      JSON         NULL,
    `query_params`     JSON         NULL,
    `response`         MEDIUMTEXT   NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `mock_response_ibfk_1` FOREIGN KEY (`mock_id`) REFERENCES `mock` (`id`) ON DELETE CASCADE
);

CREATE TABLE `mock_response_history`
(
    `id`               BIGINT(16)    NOT NULL AUTO_INCREMENT,
    `service_id`       BIGINT(16)    NOT NULL,
    `query_url`        VARCHAR(2048) NULL,
    `method`           INT(8)        NOT NULL,
    `response_url`     VARCHAR(2048) NULL,
    `response_source`  INT(8)        NOT NULL,
    `status_code`      INT(8)        NOT NULL,
    `request_headers`  JSON          NULL,
    `response_headers` JSON          NULL,
    `response`         MEDIUMTEXT    NOT NULL,
    `response_time`    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `mock_response_history_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`) ON DELETE CASCADE
);