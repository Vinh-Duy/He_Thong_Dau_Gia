package com.daugia.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;

public class NetworkClient {

    private static NetworkClient instance;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final Gson gson = new Gson();

    private NetworkClient() {

        connect("127.0.0.1", 8888); 
    }

    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    private void connect(String ip, int port) {
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

    public synchronized Response sendRequest(Request request) {
        if (socket == null || socket.isClosed()) {
            return new Response("ERROR", "Mất kết nối với Server", null);
        }

        try {
            String jsonRequest = gson.toJson(request);
            out.println(jsonRequest);

            String jsonResponse = in.readLine();
            if (jsonResponse != null) {
                return gson.fromJson(jsonResponse, Response.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Response("ERROR", "Lỗi đường truyền mạng", null);
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