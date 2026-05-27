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
	`username` VARCHAR(50) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`password` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`email` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`full_name` VARCHAR(100) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`phone` VARCHAR(20) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`gender` VARCHAR(10) NULL DEFAULT NULL COLLATE 'utf8mb4_0900_ai_ci',
	`role` VARCHAR(50) NULL DEFAULT 'user' COLLATE 'utf8mb4_0900_ai_ci',
	`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	`updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `username` (`username`) USING BTREE
)
COLLATE='utf8mb4_0900_ai_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1
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
-- CLEAR OLD DATA & INSERT SAMPLE DATA
-- ============================================================
SET FOREIGN_KEY_CHECKS = 0;

-- Delete existing data (in correct order to respect foreign keys)
DELETE FROM bidhistory;
DELETE FROM auto_bids;
DELETE FROM auctions;
DELETE FROM users;

-- Reset auto_increment
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE auctions AUTO_INCREMENT = 1;
ALTER TABLE auto_bids AUTO_INCREMENT = 1;
ALTER TABLE bidhistory AUTO_INCREMENT = 1;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- INSERT SAMPLE USERS
-- ============================================================
INSERT INTO users (username, password, email, full_name, phone, gender, role) VALUES
('admin', 'admin123', 'admin@bidnova.com', 'Admin User', '0123456789', 'M', 'admin'),
('seller1', 'pass123', 'seller1@bidnova.com', 'Nguyễn Văn A', '0912345678', 'M', 'user'),
('bidder1', 'pass123', 'bidder1@bidnova.com', 'Trần Thị B', '0987654321', 'F', 'user'),
('bidder2', 'pass123', 'bidder2@bidnova.com', 'Lê Văn C', '0912345670', 'M', 'user');

-- ============================================================
-- INSERT SAMPLE AUCTIONS
-- ============================================================
INSERT INTO auctions (id, product_name, name, start_price, current_highest_bid, highest_bidder, status, category, description, start_time, end_time, seller_id, image_url) 
VALUES
('AUC001', 'Tranh sơn dầu cổ', 'Tranh sơn dầu cổ', 5000000, 7500000, 'bidder1', 'OPEN', 'ART', 'Tranh sơn dầu thế kỷ 20', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 2, 'https://example.com/art1.jpg'),
('AUC002', 'Bất động sản Hà Nội', 'Bất động sản Hà Nội', 2000000000, 2500000000, 'bidder2', 'OPEN', 'REALESTATE', 'Căn hộ cao cấp tại Hà Nội', NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), 2, 'https://example.com/real1.jpg'),
('AUC003', 'Mercedes S500', 'Mercedes S500', 1500000000, 1800000000, 'bidder1', 'CLOSED', 'VEHICLE', 'Xe Mercedes S500 2023', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), 2, 'https://example.com/car1.jpg');

-- ============================================================
-- INSERT SAMPLE AUTO BIDS
-- ============================================================
INSERT INTO auto_bids (auction_id, username, max_bid, increment, is_active, created_at) 
VALUES
('AUC001', 'bidder1', 10000000, 500000, TRUE, UNIX_TIMESTAMP() * 1000),
('AUC002', 'bidder2', 3000000000, 100000000, TRUE, UNIX_TIMESTAMP() * 1000),
('AUC003', 'bidder1', 2000000000, 50000000, FALSE, UNIX_TIMESTAMP() * 1000);
