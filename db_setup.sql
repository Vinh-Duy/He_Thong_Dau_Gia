-- ============================================================
-- HỆ THỐNG ĐẤU GIÁ - DATABASE SETUP (FINAL)
-- ============================================================

CREATE DATABASE IF NOT EXISTS auction_db;
USE auction_db;

-- ============================================================
-- TABLE: auctions
-- ============================================================
CREATE TABLE `auctions` (
	`id` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_unicode_ci',
	`product_name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_unicode_ci',
	`description` TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`image_url` VARCHAR(500) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`start_price` DOUBLE NULL DEFAULT NULL,
	`current_highest_bid` DOUBLE NULL DEFAULT NULL,
	`highest_bidder` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`start_time` DATETIME NULL DEFAULT NULL,
	`end_time` DATETIME NULL DEFAULT NULL,
	`status` VARCHAR(50) NULL DEFAULT 'OPEN' COLLATE 'utf8mb4_0900_ai_ci',
	`category` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`seller_id` INT NULL DEFAULT NULL,
	`price_ceiling` DOUBLE NULL DEFAULT NULL COMMENT 'Giá tối đa - khi đạt giá này đấu giá kết thúc',
	`min_bid_increment` DOUBLE NOT NULL DEFAULT 1000 COMMENT 'Bước giá tối thiếu (VD: 1tr)',
	`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`) USING BTREE,
	INDEX `idx_seller_id` (`seller_id`),
	INDEX `idx_status` (`status`),
	INDEX `idx_category` (`category`)
)
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB
;


-- ============================================================
-- TABLE: auto_bids
-- ============================================================
CREATE TABLE IF NOT EXISTS auto_bids (
  id INT AUTO_INCREMENT PRIMARY KEY,
  auction_id VARCHAR(50) NOT NULL,
  username VARCHAR(100) NOT NULL,
  max_bid DOUBLE NOT NULL,
  increment DOUBLE NOT NULL,
  is_active BOOLEAN DEFAULT true,
  created_at BIGINT NOT NULL,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_auction_id` (`auction_id`),
  INDEX `idx_username` (`username`),
  CONSTRAINT `fk_autobid_auction` FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE `users` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `role` VARCHAR(50) NULL DEFAULT 'user',
    `full_name` VARCHAR(100) NULL DEFAULT NULL,
    `email` VARCHAR(100) NULL DEFAULT NULL,
    `phone` VARCHAR(20) NULL DEFAULT NULL,
    `gender` VARCHAR(10) NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    UNIQUE INDEX `username` (`username`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- TABLE: auto_bids
-- ============================================================
CREATE TABLE `auto_bids` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `auction_id` VARCHAR(50) NOT NULL,
    `user_id` INT NOT NULL,
    `max_bid` DECIMAL(20, 2) NOT NULL,
    `increment` DECIMAL(20, 2) NOT NULL,
    `is_active` BOOLEAN DEFAULT true,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (`auction_id`) REFERENCES `auctions`(`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- TABLE: bidhistory
-- ============================================================
CREATE TABLE `bidhistory` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `auction_id` VARCHAR(50) NOT NULL,
    `user_id` INT NOT NULL,
    `bid_amount` DECIMAL(20, 2) NOT NULL,
    `bid_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `auction_id` (`auction_id`) USING BTREE,
    INDEX `user_id` (`user_id`) USING BTREE,
    CONSTRAINT `bidhistory_ibfk_1` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
    CONSTRAINT `bidhistory_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- CLEAR OLD DATA (AN TOÀN)
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE bidhistory;
TRUNCATE TABLE auto_bids;
TRUNCATE TABLE auctions;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;