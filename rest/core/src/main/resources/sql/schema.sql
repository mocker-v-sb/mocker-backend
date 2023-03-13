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
    `last_model_id`   BIGINT(16)   NOT NULL DEFAULT 0,
    `last_mock_id`    BIGINT(16)   NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_name` (`name` ASC) VISIBLE,
    UNIQUE INDEX `idx_path` (`path` ASC) VISIBLE,
    UNIQUE INDEX `idx_url` (`url` ASC) VISIBLE
);

CREATE TABLE `model`
(
    `id`            BIGINT(16)   NOT NULL,
    `service_id`    BIGINT(16)   NOT NULL,
    `name`          VARCHAR(128) NOT NULL,
    `description`   VARCHAR(128) NULL,
    `creation_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`, `service_id`),
    CONSTRAINT `model_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`) ON DELETE CASCADE
);

CREATE TABLE `mock`
(
    `id`                BIGINT(16)    NOT NULL,
    `service_id`        BIGINT(16)    NOT NULL,
    `name`              VARCHAR(128)  NOT NULL,
    `description`       VARCHAR(128)  NULL,
    `path`              VARCHAR(128)  NOT NULL,
    `method`            INT(8)        NOT NULL,
    `request_model_id`  BIGINT(16)    NULL,
    `response_model_id` BIGINT(16)    NULL,
    `request_headers`   VARCHAR(2048) NULL,
    `response_headers`  VARCHAR(2048) NULL,
    `query_params`      VARCHAR(2048) NULL,
    `path_params`       VARCHAR(2048) NULL,
    `creation_time`     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`, `service_id`),
    CONSTRAINT `mock_ibfk_1` FOREIGN KEY (`service_id`) REFERENCES `service` (`id`) ON DELETE CASCADE,
    CONSTRAINT `mock_ibfk_2` FOREIGN KEY (`request_model_id`) REFERENCES `model` (`id`),
    CONSTRAINT `mock_ibfk_3` FOREIGN KEY (`response_model_id`) REFERENCES `model` (`id`)
);