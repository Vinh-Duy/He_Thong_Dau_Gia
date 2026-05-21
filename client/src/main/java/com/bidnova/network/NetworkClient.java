package com.bidnova.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.bidnova.config.NetworkConfig;
import com.bidnova.utils.SessionManager;
import com.google.gson.Gson;

public class NetworkClient {

    private static NetworkClient instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();

    private final BlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();
    // Danh sách các listener để hỗ trợ nhiều màn hình nhận tin nhắn cùng lúc
    private final List<Consumer<String>> messageListeners = new CopyOnWriteArrayList<>();

    private static final Set<String> PUBLIC_ACTIONS =
            new HashSet<>(Arrays.asList("LOGIN", "REGISTER"));

    private NetworkClient() {
        connect(NetworkConfig.getHost(), NetworkConfig.getPort());
    }

    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    // Phương thức mới: Thêm listener
    public void addOnMessageReceivedListener(Consumer<String> listener) {
        if (listener != null) messageListeners.add(listener);
    }

    // Phương thức mới: Xóa listener khi không dùng nữa (để tránh rò rỉ bộ nhớ)
    public void removeOnMessageReceivedListener(Consumer<String> listener) {
        messageListeners.remove(listener);
    }

    private void connect(String ip, int port) {
        try {
            socket = new Socket(ip, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Đã kết nối tới Server thành công!");

            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        // Kiểm tra nếu là tin nhắn Broadcast (có chứa trường action)
                        if (line.contains("\"action\":\"")) {
                            // Gửi tin nhắn cho tất cả các Controller đang đăng ký lắng nghe
                            for (Consumer<String> listener : messageListeners) {
                                listener.accept(line);
                            }
                        } else {
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

    public synchronized Response sendRequest(Request request) {
        if (socket == null || socket.isClosed()) {
            return new Response("ERROR", "Mất kết nối với Server", null);
        }

        try {
            if (request != null && request.getAction() != null
                    && !PUBLIC_ACTIONS.contains(request.getAction())) {
                request.setToken(SessionManager.getToken());
            }

            String jsonRequest = gson.toJson(request);
            out.println(jsonRequest);

            String jsonResponse = responseQueue.poll(NetworkConfig.getTimeoutSeconds(), TimeUnit.SECONDS);
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