package com.daugia.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.google.gson.Gson;

public class NetworkClient {

    private static NetworkClient instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();
    
    // Cái chỗ để chứa câu trả lời cho hàm sendRequest cũ
    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    
    // Cái để nghe tin nhắn Real-time
    private Consumer<String> onMessageReceived;

    private NetworkClient() {
        connect("127.0.0.1", 8888); 
    }

    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    private void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Đã kết nối tới Server thành công!");

            // LUỒNG LÀM NHIỆM VỤ ĐỌC VÀ PHÂN LOẠI TIN NHẮN
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        // Nếu là tin nhắn Broadcast từ Server (có chứa chữ BID_UPDATE)
                        if (line.contains("\"action\":\"BID_UPDATE\"")) {
                            if (onMessageReceived != null) {
                                onMessageReceived.accept(line);
                            }
                        } else {
                            // Nếu là câu trả lời cho các lệnh cũ (như LOGIN, GET_ALL_AUCTIONS)
                            // Thì bỏ vào cho hàm sendRequest lấy ra
                            responseQueue.offer(line);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Mất kết nối lắng nghe từ Server.");
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Không thể kết nối tới Server.");
        }
    }

    // Hàm cũ được giữ nguyên cách dùng, chỉ thay đổi bên trong một chút
    public synchronized Response sendRequest(Request request) {
        if (socket == null || socket.isClosed()) {
            return new Response("ERROR", "Mất kết nối với Server", null);
        }

        try {
            String jsonRequest = gson.toJson(request);
            out.println(jsonRequest); // Gửi yêu cầu đi

            // Chờ lấy câu trả lời (Chờ tối đa 5 giây để app không bị đơ nếu mạng lag)
            String jsonResponse = responseQueue.poll(5, TimeUnit.SECONDS); 
            
            if (jsonResponse != null) {
                return gson.fromJson(jsonResponse, Response.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Response("ERROR", "Lỗi đường truyền hoặc Timeout", null);
    }

    public void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Đã ngắt kết nối với Server.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}