package com.bidnova.models;

// TODO: làm tính năng yêu thích đấu giá
public class User {
    private int id;
    private String username;
    private String password;
    private String email;
    private String fullName;
    private String phone;
    private String gender;
    private String role;
    private String token;

    public User() {}

    // Constructor để nhận data từ Gson
    public User(int id, String username, String password, String email, String fullName, String phone, String gender, String role, String token) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.fullName = fullName;
        this.phone = phone;
        this.gender = gender;
        this.role = role;
        this.token = token;
    }

    // --- FULL BỘ GETTER / SETTER KHÔNG BAO GIỜ SỢ BÁO LỖI ĐỎ NỮA ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    // Hàm getName đặc biệt để hiển thị tên
    public String getName() { 
        return fullName != null && !fullName.isEmpty() ? fullName : username; 
    }
}