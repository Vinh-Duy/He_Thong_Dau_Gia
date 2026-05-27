-- ============================================================
-- DATABASE SCRIPT - BID NOVA AUCTION SYSTEM
-- ============================================================
-- Tạo database
CREATE DATABASE IF NOT EXISTS bidnova;
USE bidnova;

-- ============================================================
-- TABLE: user
-- ============================================================
CREATE TABLE IF NOT EXISTS user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    full_name VARCHAR(150),
    phone VARCHAR(20),
    gender VARCHAR(10),
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: auction
-- ============================================================
CREATE TABLE IF NOT EXISTS auction (
    id VARCHAR(50) PRIMARY KEY,
    product_name VARCHAR(255) NOT NULL,
    start_price DOUBLE NOT NULL,
    current_highest_bid DOUBLE DEFAULT 0,
    highest_bidder VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    category VARCHAR(100),
    description LONGTEXT,
    start_time DATETIME,
    end_time DATETIME,
    seller_id INT NOT NULL,
    image_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_seller_id (seller_id),
    INDEX idx_end_time (end_time),
    INDEX idx_highest_bidder (highest_bidder)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- TABLE: autobid
-- ============================================================
CREATE TABLE IF NOT EXISTS autobid (
    id INT PRIMARY KEY AUTO_INCREMENT,
    auction_id VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL,
    max_bid DOUBLE NOT NULL,
    increment DOUBLE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at BIGINT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (auction_id) REFERENCES auction(id) ON DELETE CASCADE,
    FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE,
    INDEX idx_auction_id (auction_id),
    INDEX idx_username (username),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================
-- Dữ liệu mẫu
-- ============================================================
-- Insert sample users
INSERT INTO user (username, password, email, full_name, phone, gender, role) VALUES
('admin', 'admin123', 'admin@bidnova.com', 'Admin', '0123456789', 'M', 'ADMIN'),
('seller1', 'pass123', 'seller1@bidnova.com', 'Nguyễn Văn A', '0912345678', 'M', 'USER'),
('bidder1', 'pass123', 'bidder1@bidnova.com', 'Trần Thị B', '0987654321', 'F', 'USER'),
('bidder2', 'pass123', 'bidder2@bidnova.com', 'Lê Văn C', '0912345670', 'M', 'USER');

-- Insert sample auctions
INSERT INTO auction (id, product_name, start_price, current_highest_bid, highest_bidder, status, category, description, start_time, end_time, seller_id, image_url) VALUES
('AUC001', 'Tranh sơn dầu cổ', 5000000, 7500000, 'bidder1', 'OPEN', 'ART', 'Tranh sơn dầu thế kỷ 20', NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 2, 'https://example.com/art1.jpg'),
('AUC002', 'Bất động sản Hà Nội', 2000000000, 2500000000, 'bidder2', 'OPEN', 'REALESTATE', 'Căn hộ cao cấp tại Hà Nội', NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), 2, 'https://example.com/real1.jpg'),
('AUC003', 'Mercedes S500', 1500000000, 1800000000, 'bidder1', 'CLOSED', 'VEHICLE', 'Xe Mercedes S500 2023', DATE_SUB(NOW(), INTERVAL 30 DAY), DATE_SUB(NOW(), INTERVAL 15 DAY), 2, 'https://example.com/car1.jpg');

-- Insert sample autobids
INSERT INTO autobid (auction_id, username, max_bid, increment, is_active, created_at) VALUES
('AUC001', 'bidder1', 10000000, 500000, TRUE, UNIX_TIMESTAMP() * 1000),
('AUC002', 'bidder2', 3000000000, 100000000, TRUE, UNIX_TIMESTAMP() * 1000),
('AUC003', 'bidder1', 2000000000, 50000000, FALSE, UNIX_TIMESTAMP() * 1000);

-- ============================================================
-- Xóa database (nếu cần khôi phục)
-- ============================================================
-- DROP DATABASE IF EXISTS bidnova;
