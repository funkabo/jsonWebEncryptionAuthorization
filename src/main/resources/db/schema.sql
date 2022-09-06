CREATE TABLE `account` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `firstname` VARCHAR(40) NOT NULL,
  `lastname` VARCHAR(40) NOT NULL,
  `username` VARCHAR(15) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `email` VARCHAR(40) NOT NULL,
  `created` DATETIME DEFAULT NULL,
  `enabled` BIT(1) DEFAULT NULL,
  `accountNonExpired` BIT(1) DEFAULT NULL,
  `accountNonLocked` BIT(1) DEFAULT NULL,
  `credentialsNonExpired` BIT(1) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



CREATE TABLE `authority` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `permission` VARCHAR(60) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_authority_permission` (`permission`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;


CREATE TABLE `account_authority` (
  `account_id` BIGINT(20) NOT NULL,
  `authority_id` BIGINT(20) NOT NULL,
  PRIMARY KEY (`account_id`,`authority_id`),

  CONSTRAINT `fk_account_authority_authority_id` FOREIGN KEY (`authority_id`) REFERENCES `authority` (`id`),
  CONSTRAINT `fk_account_authority_account_id` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;



