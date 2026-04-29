package com.bidnova.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import com.bidnova.network.Request;
import com.bidnova.network.Response;

public class SocketClient {
    
    /**
     * Gửi Request object và nhận Response object
     */
    public static Response sendRequest(Request request) {
        try (Socket socket = new Socket("localhost", 8888);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            // Gửi yêu cầu đến server (convert Request thành string)
            String requestStr = request.toString();
            out.writeUTF(requestStr);
            System.out.println("Gửi request: " + requestStr);

            // Nhận phản hồi từ server
            String responseStr = in.readUTF();
            System.out.println("Nhận response: " + responseStr);
            
            // Parse response string thành Response object
            return Response.parse(responseStr);
        } catch (Exception e) {
            System.out.println("Lỗi khi kết nối đến server!");
            e.printStackTrace();
            return new Response("FAILED", "Lỗi kết nối đến server");
        }
    }
    
    /**
     * Phiên bản cũ (String) - keep for backward compatibility
     */
    public static String sendRequest(String request) {
        try (Socket socket = new Socket("localhost", 8888);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            out.writeUTF(request);
            return in.readUTF();
        } catch (Exception e) {
            System.out.println("Lỗi khi kết nối đến server!");
            e.printStackTrace();
            return null;
        }
    }
}
