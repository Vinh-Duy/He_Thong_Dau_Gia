package com.bidnova.models;

/**
 * 👤 User - Mô hình dữ liệu người dùng hệ thống
 * 
 * <h2>Chức Năng:</h2>
 * <p>Đại diện cho một người dùng trong hệ thống đấu giá.</p>
 * <ul>
 *   <li>Lưu thông tin xác thực (username, password)</li>
 *   <li>Lưu thông tin cá nhân (email, fullName, phone, gender)</li>
 *   <li>Lưu vai trò người dùng (user, seller, admin)</li>
 * </ul>
 * 
 * <h2>Vai Trò (Role):</h2>
 * <ul>
 *   <li><b>user/bidder:</b> Người dùng thông thường, chỉ có thể đặt giá</li>
 *   <li><b>seller:</b> Người bán, có thể tạo và quản lý phiên đấu giá</li>
 *   <li><b>admin:</b> Quản trị viên, quản lý toàn bộ hệ thống</li>
 * </ul>
 * 
 * <h2>Lưu Trữ:</h2>
 * <p>Dữ liệu được lưu trong bảng <code>users</code> của database MySQL.</p>
 * <pre>
 * CREATE TABLE users (
 *   id INT PRIMARY KEY AUTO_INCREMENT,
 *   username VARCHAR(50) UNIQUE NOT NULL,
 *   password VARCHAR(255) NOT NULL,      -- Hashed bằng BCrypt
 *   email VARCHAR(100),
 *   full_name VARCHAR(100),
 *   phone VARCHAR(20),
 *   gender VARCHAR(10),
 *   role VARCHAR(50) DEFAULT 'user'
 * );
 * </pre>
 * 
 * <h2>Ví Dụ Sử Dụng:</h2>
 * <pre>
 * // Tạo người dùng mới
 * User user = new User(1, "bidder1", "hashedPassword123", "user");
 * 
 * // Khởi tạo đầy đủ với thông tin cá nhân
 * User fullUser = new User(
 *     1,                                // id
 *     "seller1",                        // username
 *     "$2a$10$...",                    // password (BCrypt hash)
 *     "seller1@example.com",           // email
 *     "Nguyễn Văn A",                  // fullName
 *     "0901234567",                    // phone
 *     "male",                          // gender
 *     "seller"                         // role
 * );
 * </pre>
 * 
 * @author BidNova Team
 * @version 1.0
 * @see com.bidnova.handlers.LoginHandler
 * @see com.bidnova.handlers.RegisterHandler
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phone;
    private String gender;
    private String role;

    /**
     * Constructor cơ bản - Tạo user với thông tin tối thiểu
     * 
     * @param id      ID duy nhất của người dùng (auto-generated từ DB)
     * @param username Tên đăng nhập (unique)
     * @param password Mật khẩu (đã được hash bằng BCrypt)
     * @param role     Vai trò người dùng ("user", "seller", "admin")
     */
    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * Constructor đầy đủ - Tạo user với tất cả thông tin cá nhân
     * 
     * @param id        ID duy nhất của người dùng
     * @param username  Tên đăng nhập
     * @param password  Mật khẩu (BCrypt hash)
     * @param email     Địa chỉ email
     * @param fullName  Họ tên đầy đủ
     * @param phone     Số điện thoại
     * @param gender    Giới tính ("male", "female", "other")
     * @param role      Vai trò ("user", "seller", "admin")
     */
    public User(int id, String username, String password, String email,
                String fullName, String phone, String gender, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
    }

    // ==================== GETTERS & SETTERS ====================
    
    /**
     * @return ID duy nhất của người dùng
     */
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    /**
     * @return Tên đăng nhập
     */
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    /**
     * @return Mật khẩu (đã hash)
     * @warning KHÔNG bao giờ gửi mật khẩu cho client
     */
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    /**
     * @return Địa chỉ email
     */
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    /**
     * @return Họ tên đầy đủ
     */
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    /**
     * @return Số điện thoại
     */
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    /**
     * @return Giới tính
     */
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    /**
     * @return Vai trò người dùng ("user", "seller", "admin")
     */
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}