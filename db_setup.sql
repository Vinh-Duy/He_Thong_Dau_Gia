-- ============================================================
-- HỆ THỐNG ĐẤU GIÁ - DATABASE SETUP
-- ============================================================

-- Tạo database
CREATE DATABASE IF NOT EXISTS auction_db;
USE auction_db;

-- ============================================================
-- TABLE: auctions
-- ============================================================
CREATE TABLE IF NOT EXISTS auctions (
  id VARCHAR(50) PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  start_price DECIMAL(20, 2) NOT NULL,
  current_highest_bid DECIMAL(20, 2) DEFAULT 0,
  status VARCHAR(50) DEFAULT 'OPEN',
  category VARCHAR(100),
  description TEXT,
  image_url VARCHAR(500),
  start_time DATETIME,
  end_time DATETIME,
  seller_id INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

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
INSERT INTO auctions (id, name, start_price, current_highest_bid, status, category, description, start_time, end_time, seller_id) VALUES
('A001', 'Lamborghini Aventador', 5000000000.00, 5000000000.00, 'OPEN', 'Phương tiện', 'Siêu xe hạng A', '2026-05-18 10:00:00', '2026-06-18 10:00:00', 1),
('A002', 'Biệt thự biển Đà Nẵng', 25000000000.00, 25000000000.00, 'OPEN', 'Bất động sản', 'Biệt thự view biển', '2026-05-18 10:00:00', '2026-06-18 10:00:00', 2),
('A003', 'Tranh sơn dầu cổ', 1000000000.00, 1000000000.00, 'OPEN', 'Sưu tầm - nghệ thuật', 'Tranh thế kỷ 19', '2026-05-18 10:00:00', '2026-06-18 10:00:00', 3);

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
