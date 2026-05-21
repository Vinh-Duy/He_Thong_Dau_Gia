-- ============================================================
-- HỆ THỐNG ĐẤU GIÁ - DATABASE SETUP
-- ============================================================

-- Tạo database
CREATE DATABASE IF NOT EXISTS auction_db;
USE auction_db;

-- ============================================================
-- TABLE: auctions
-- ============================================================
CREATE TABLE `auctions` (
	`id` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`name` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_unicode_ci',
	`description` TEXT NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`image_url` VARCHAR(500) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`start_price` DOUBLE NULL DEFAULT NULL,
	`current_highest_bid` DOUBLE NULL DEFAULT NULL,
	`start_time` DATETIME NULL DEFAULT NULL,
	`end_time` DATETIME NULL DEFAULT NULL,
	`status` VARCHAR(50) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`category` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`seller_id` INT NULL DEFAULT NULL,
	PRIMARY KEY (`id`) USING BTREE
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
  max_bid DECIMAL(20, 2) NOT NULL,
  increment DECIMAL(20, 2) NOT NULL,
  is_active BOOLEAN DEFAULT true,
  created_at BIGINT NOT NULL,
  FOREIGN KEY (auction_id) REFERENCES auctions(id)
);

-- ============================================================
-- TABLE: users
-- ============================================================
CREATE TABLE `users` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`username` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`password` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`token` VARCHAR(255) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`role` VARCHAR(50) NULL DEFAULT 'user' COLLATE 'utf8mb4_0900_ai_ci',
	`full_name` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`email` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`phone` VARCHAR(20) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`gender` VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `username` (`username`) USING BTREE
)
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB
AUTO_INCREMENT=24
;


-- ============================================================
-- TABLE: bidhistory
-- ============================================================
CREATE TABLE `bidhistory` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`auction_id` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`user_id` INT NOT NULL,
	`username` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`bid_amount` DOUBLE NOT NULL,
	`bid_time` DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP),
	PRIMARY KEY (`id`) USING BTREE,
	INDEX `auction_id` (`auction_id`) USING BTREE,
	INDEX `user_id` (`user_id`) USING BTREE,
	CONSTRAINT `bidhistory_ibfk_1` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
	CONSTRAINT `bidhistory_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB
;


-- ============================================================
-- CLEAR OLD DATA
-- ============================================================
TRUNCATE TABLE auto_bids;
TRUNCATE TABLE auctions;
TRUNCATE TABLE users;
