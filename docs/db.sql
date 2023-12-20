CREATE TABLE `apple_id`(
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `email` VARCHAR(255) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `email_password` VARCHAR(255) NOT NULL,
    `birthday` VARCHAR(255) NOT NULL,
    `usa_card` VARCHAR(255),
    `usa_card_link` VARCHAR(255),
    `status` INT(1) NOT NULL DEFAULT 0,  -- 0:待绑定 1:已取出 2: 已注册
    PRIMARY KEY (`id`)
);
CREATE INDEX `status` ON `apple_id`(`status`);

CREATE TABLE `usa_card`(
    `id` INT(11) NOT NULL AUTO_INCREMENT,
    `card` VARCHAR(255) NOT NULL UNIQUE,
    `link` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`)
);
