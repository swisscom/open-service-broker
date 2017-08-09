CREATE TABLE IF NOT EXISTS `named_lock` (
  `id`             BIGINT(20)   NOT NULL AUTO_INCREMENT,
  `name`           VARCHAR(255) NOT NULL,
  `date_created`   DATETIME              DEFAULT CURRENT_TIMESTAMP,
  `ttl_in_seconds` INT(11)      NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_asdfefrwwx9c3y3qa2q12asfea` (`name`)
)