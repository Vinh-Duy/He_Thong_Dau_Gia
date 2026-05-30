package com.bidnova.database;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    private static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .ignoreIfMalformed()
            .load();

    private static String URL = dotenv.get("DB_URL");
    private static String USER = dotenv.get("DB_USER");
    private static String PASSWORD = dotenv.get("DB_PASSWORD"); 

    static {
        // Fallback kiểm tra thư mục cha nếu chạy từ module server
        if (URL == null || URL.isEmpty()) {
            Dotenv fallback = Dotenv.configure().directory("..").ignoreIfMissing().load();
            URL = fallback.get("DB_URL");
            USER = fallback.get("DB_USER");
            PASSWORD = fallback.get("DB_PASSWORD");
        }
    }

    // Hàm này sẽ trả về một kết nối MỚI mỗi khi được gọi
    public static Connection getConnection() {
        try {
            if (URL == null || URL.isEmpty()) {
                System.err.println("LỖI: Không tìm thấy DB_URL trong .env! Hãy kiểm tra lại file cấu hình.");
                return null;
            }

            Class.forName("com.mysql.cj.jdbc.Driver");
            // Tăng timeout để tránh lỗi kết nối mạng chập chờn trên cloud
            DriverManager.setLoginTimeout(30); 
            
            Properties props = new Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASSWORD);
            props.setProperty("allowPublicKeyRetrieval", "true");
            props.setProperty("useSSL", "false"); // Tắt SSL để tránh lỗi kết nối qua proxy của Railway
            props.setProperty("autoReconnect", "true");
            
            Connection conn = DriverManager.getConnection(URL, props);
            if (conn != null) {
                System.out.println("Kết nối Cloud Database thành công!");
                return conn;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi SQL (Cloud): " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Lỗi Driver: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}
