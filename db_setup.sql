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
-- TABLE: auto_bids
-- ============================================================
<<<<<<< HEAD
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
=======
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
CREATE TABLE IF NOT EXISTS users (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  role VARCHAR(50) DEFAULT 'BIDDER',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- CLEAR OLD DATA
-- ============================================================
TRUNCATE TABLE auto_bids;
TRUNCATE TABLE auctions;
TRUNCATE TABLE users;

-- ============================================================
-- INSERT TEST DATA - AUCTIONS
-- ============================================================
INSERT INTO auctions (id, name, start_price, current_highest_bid, status, category, description, end_time, seller_id) VALUES
('A001', 'Lamborghini Aventador', 5000000000.00, 5000000000.00, 'OPEN', 'Phương tiện', 'Siêu xe hạng A', DATE_ADD(NOW(), INTERVAL 5 MINUTE), 1),
('A002', 'Biệt thự biển Đà Nẵng', 25000000000.00, 25000000000.00, 'OPEN', 'Bất động sản', 'Biệt thự view biển', DATE_ADD(NOW(), INTERVAL 10 MINUTE), 2),
('A003', 'Tranh sơn dầu cổ', 1000000000.00, 1000000000.00, 'OPEN', 'Sưu tầm - nghệ thuật', 'Tranh thế kỷ 19', DATE_ADD(NOW(), INTERVAL 3 MINUTE), 3);

-- ============================================================
-- INSERT TEST DATA - USERS
-- ============================================================
INSERT INTO users (username, password, role) VALUES
('userA', 'pass123', 'BIDDER'),
('userB', 'pass123', 'BIDDER'),
('userC', 'pass123', 'BIDDER'),
('admin', 'admin123', 'ADMIN');

-- ============================================================
-- VERIFY DATA
-- ============================================================
SELECT '✓ Auctions loaded:' as status;
SELECT id, name, current_highest_bid FROM auctions;

SELECT '✓ Users loaded:' as status;
SELECT id, username, role FROM users;

SELECT '✓ Auto-bids table ready:' as status;
SELECT COUNT(*) as auto_bid_count FROM auto_bids;
>>>>>>> origin/main
