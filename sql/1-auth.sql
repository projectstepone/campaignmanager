CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(45) NOT NULL,
  `roles` blob,
  `deleted` tinyint(1) DEFAULT '0',
  `created` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `updated` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `user_tokens` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `token` varchar(45) NOT NULL,
  `user_id` varchar(255) NOT NULL,
  `expiry` datetime(3) DEFAULT NULL,
  `deleted` tinyint(1) DEFAULT '0',
  `created` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `updated` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `token` (`token`),
  KEY `expiry` (`expiry`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;