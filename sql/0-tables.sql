 CREATE TABLE `campaigns` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `campaign_id` varchar(45) NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_by` varchar(255) NOT NULL,
  `notification_type` varchar(255) NOT NULL,
  `content` blob NOT NULL,
  `send_as` varchar(255) NOT NULL,
  `state` varchar(255) DEFAULT NULL,
  `item_count` bigint(20) DEFAULT '0',
  `completed` bigint(20) DEFAULT '0',
  `sent` bigint(20) DEFAULT '0',
  `failures` bigint(20) DEFAULT '0',
  `permanent_failures` bigint(20) DEFAULT '0',
  `temporary_failures` bigint(20) DEFAULT '0',
  `delivered` bigint(20) DEFAULT '0',
  `delivery_failures` bigint(20) DEFAULT '0',
  `created` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `updated` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_campaign_id` (`campaign_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;



 CREATE TABLE `notification_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `notification_id` varchar(45) NOT NULL,
  `notification_type` varchar(255) NOT NULL,
  `campaign_id` varchar(45) NOT NULL,
  `state` varchar(255) DEFAULT NULL,
  `phone` varchar(15) NOT NULL,
  `http_response` blob,
  `http_status` int(10) DEFAULT '-1',
  `provider` varchar(45) DEFAULT 'KALEYRA_SMS',
  `translated_provider_response` varchar(255) DEFAULT NULL,
  `created` datetime(3) DEFAULT CURRENT_TIMESTAMP(3),
  `updated` datetime(3) DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_notification_id` (`notification_id`),
  KEY `idx_campaign_state` (`campaign_id`,`state`(191))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
