package com.bidnova.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.bidnova.dao.UserDAO;
import com.bidnova.models.User;

// nhận yêu cầu từ client và gọi các phương thức trong DAO để thực hiện các thao tác với database, sau đó gửi kết quả trả về cho client
// Đây là lớp xử lý kết nối với client, sẽ được chạy trong một luồng riêng biệt cho mỗi client kết nối đến server
public class ClientHandler extends Thread {
    private Socket socket;
    private UserDAO userDAO = new UserDAO();

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            // Đọc yêu cầu từ Client (Ví dụ: "LOGIN|username|password", "REGISTER|username|password|email|fullName|phone|gender|role")
            String requestStr = in.readUTF();
            Request request = Request.parse(requestStr);
            String[] parts = request.getParams();
            String action = request.getAction();

            if (action.equals("LOGIN")) {
                String user = parts[0];
                String pass = parts[1];
                
                // GỌI DAO ĐỂ KIỂM TRA (Sử dụng BCrypt bên trong DAO)
                User authenticatedUser = userDAO.loginUser(user, pass);

                if (authenticatedUser != null) {
                    // Format: SUCCESS|message|data
                    Response response = new Response("SUCCESS", "Đăng nhập thành công!", "ROLE:" + authenticatedUser.getRole());
                    out.writeUTF(response.toString());
                } else {
                    Response response = new Response("FAILED", "Sai tài khoản hoặc mật khẩu!");
                    out.writeUTF(response.toString());
                }
            } 
            else if (action.equals("REGISTER")) {
                String username = parts[0];
                String password = parts[1];
                String email = parts[2];
                String fullName = parts[3];
                String phone = parts[4];
                String gender = parts[5];
                String role = parts[6];

                User newUser = new User();
                newUser.setUsername(username);
                newUser.setPassword(password);
                newUser.setEmail(email);
                newUser.setFullName(fullName);
                newUser.setPhone(phone);
                newUser.setGender(gender);
                newUser.setRole(role);

                boolean isRegistered = userDAO.registerUser(newUser);
                if (isRegistered) {
                    Response response = new Response("SUCCESS", "Đăng ký thành công!");
                    out.writeUTF(response.toString());
                } else {
                    Response response = new Response("FAILED", "Đăng ký thất bại!");
                    out.writeUTF(response.toString());
                }
            }
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
