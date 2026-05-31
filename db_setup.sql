-- ============================================================
-- HỆ THỐNG ĐẤU GIÁ - DATABASE SETUP (FINAL)
-- ============================================================

CREATE DATABASE IF NOT EXISTS auction_db CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE auction_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS bidhistory;
DROP TABLE IF EXISTS auto_bids;
DROP TABLE IF EXISTS auctions;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

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
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- TABLE: auctions
-- ============================================================
CREATE TABLE `auctions` (
    `id` VARCHAR(50) NOT NULL,
    `name` VARCHAR(255) DEFAULT NULL,
    `product_name` VARCHAR(255) DEFAULT NULL,
    `description` TEXT DEFAULT NULL,
    `image_url` VARCHAR(500) DEFAULT NULL,
    `start_price` DOUBLE DEFAULT NULL,
    `current_highest_bid` DOUBLE DEFAULT NULL,
    `highest_bidder` VARCHAR(100) DEFAULT NULL,
    `start_time` DATETIME DEFAULT NULL,
    `end_time` DATETIME DEFAULT NULL,
    `status` VARCHAR(50) DEFAULT 'OPEN',
    `category` VARCHAR(255) DEFAULT NULL,
    `seller_id` INT DEFAULT NULL,
    `price_ceiling` DOUBLE DEFAULT NULL COMMENT 'Giá tối đa - khi đạt giá này đấu giá kết thúc',
    `min_bid_increment` DOUBLE NOT NULL DEFAULT 1000 COMMENT 'Bước giá tối thiểu (VD: 1tr)',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_seller_id` (`seller_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_category` (`category`),
    CONSTRAINT `fk_auction_seller` FOREIGN KEY (`seller_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ============================================================
-- TABLE: auto_bids
-- ============================================================
CREATE TABLE `auto_bids` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `auction_id` VARCHAR(50) NOT NULL,
    `user_id` INT NOT NULL,
    `max_bid` DOUBLE NOT NULL,
    `increment` DOUBLE NOT NULL,
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_auto_auction_id` (`auction_id`),
    INDEX `idx_auto_user_id` (`user_id`),
    CONSTRAINT `fk_autobid_auction` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_autobid_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
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
    PRIMARY KEY (`id`),
    INDEX `idx_bidhistory_auction_id` (`auction_id`),
    INDEX `idx_bidhistory_user_id` (`user_id`),
    CONSTRAINT `fk_bidhistory_auction` FOREIGN KEY (`auction_id`) REFERENCES `auctions` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_bidhistory_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
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
