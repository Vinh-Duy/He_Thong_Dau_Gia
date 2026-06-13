package com.bidnova.utils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Upload ảnh lên Cloudinary (unsigned upload với upload preset).
 */
public class CloudinaryUploader {
    // Cấu hình tìm ở thư mục cha (gốc dự án) và không báo lỗi nếu thiếu file
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("..") 
            .ignoreIfMissing()
            .load();
            
    private static final String CLOUD_NAME = dotenv.get("CLOUD_NAME", "default_cloud_name"); 
    private static final String CLOUDINARY_UPLOAD_PRESET = dotenv.get("CLOUDINARY_UPLOAD_PRESET", "default_preset");

    /**
     * Upload file ảnh lên Cloudinary.
     *
     * @param file File ảnh cần upload
     * @return URL của ảnh trên Cloudinary, null nếu lỗi
     */
    public static String upload(File file) {
        System.out.println("DEBUG: Đang upload bằng Cloud Name: " + CLOUD_NAME + ", Preset: " + CLOUDINARY_UPLOAD_PRESET);
        if ("default_preset".equals(CLOUDINARY_UPLOAD_PRESET)) {
            System.err.println("LỖI: Chưa load được file .env hoặc chưa cấu hình CLOUDINARY_UPLOAD_PRESET!");
        }
        try {
            // Kiểm tra cấu hình trước khi upload
            if (CLOUD_NAME == null || CLOUD_NAME.isEmpty()) {
                String workingDir = System.getProperty("user.dir");
                System.err.println("LỖI: Không tìm thấy cấu hình CLOUD_NAME.");
                System.err.println("Đang chạy tại: " + workingDir);
                System.err.println("Hãy đảm bảo file .env tồn tại trong thư mục trên.");
                return null;
            }
            if (CLOUDINARY_UPLOAD_PRESET == null || CLOUDINARY_UPLOAD_PRESET.isEmpty()) {
                System.err.println("LỖI: CLOUDINARY_UPLOAD_PRESET chưa được cấu hình. Hãy kiểm tra file .env");
                return null;
            }

            System.out.println("Đang upload lên Cloudinary (Cloud: " + CLOUD_NAME + ")...");

            // Đọc file thành bytes
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            String fileName = file.getName();
            String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
            String boundary = "---" + System.currentTimeMillis();

            // Gửi request
            URL url = new URL("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/image/upload");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            try (OutputStream os = conn.getOutputStream()) {
                // Write File Part
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n").getBytes());
                os.write(("Content-Type: image/" + ext + "\r\n\r\n").getBytes());
                os.write(fileBytes);
                os.write("\r\n".getBytes());

                // Write Upload Preset Part
                os.write(("--" + boundary + "\r\n").getBytes());
                os.write("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n".getBytes());
                os.write(CLOUDINARY_UPLOAD_PRESET.getBytes());
                os.write("\r\n".getBytes());

                // End of multipart
                os.write(("--" + boundary + "--\r\n").getBytes());
                os.flush();
            }

            // Đọc response
            int code = conn.getResponseCode();
            if (code == 200) {
                String response = new String(conn.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(response).getAsJsonObject();
                return json.get("secure_url").getAsString(); // lấy URL
            } else {
                String error = new String(conn.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
                System.err.println("Cloudinary upload error: " + code + " - " + error);
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}