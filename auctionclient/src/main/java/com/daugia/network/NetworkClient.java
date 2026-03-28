package com.daugia.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

public class NetworkClient {
    private static Socket socket;
    private static PrintWriter out;
    private static BufferedReader in;
    private static final Gson gson = new Gson();

    // Khởi tạo kết nối khi bật App
    public static void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Đã kết nối tới Server thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Không thể kết nối tới Server.");
        }
    }

    // Hàm gửi Request và đợi nhận Response
    public static Response sendRequest(Request request) {
        try {
            // Biến Request thành JSON rồi gửi đi
            String jsonRequest = gson.toJson(request);
            out.println(jsonRequest);

            // Đọc phản hồi (JSON) từ Server và biến ngược lại thành Response
            String jsonResponse = in.readLine();
            if (jsonResponse != null) {
                return gson.fromJson(jsonResponse, Response.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Response("ERROR", "Lỗi kết nối mạng", null);
    }
}