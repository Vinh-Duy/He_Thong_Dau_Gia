package com.daugia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.daugia.handlers.ActionHandler;
import com.daugia.handlers.HandlerRegistry;
import com.daugia.models.AuthUserContext;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.services.AuthService;
import com.daugia.services.impl.AuthServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Gson gson = new Gson();

    private final HandlerRegistry registry = new HandlerRegistry();
    private final AuthService authService = new AuthServiceImpl();

    private static final Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();
    private static final Set<String> PUBLIC_ACTIONS =
            new HashSet<>(Arrays.asList("LOGIN", "REGISTER"));

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        PrintWriter out = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            clientWriters.add(out);

            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                try {
                    Request request = gson.fromJson(inputLine, Request.class);
                    if (request == null || request.getAction() == null || request.getAction().isBlank()) {
                        out.println(gson.toJson(new Response("ERROR", "Thiếu action trong request", null)));
                        continue;
                    }

                    AuthUserContext authUser = null;
                    if (!PUBLIC_ACTIONS.contains(request.getAction())) {
                        authUser = authService.validateToken(request.getToken());
                        if (authUser == null) {
                            out.println(gson.toJson(new Response("ERROR", "Unauthorized: token không hợp lệ", null)));
                            continue;
                        }
                    }

                    ActionHandler handler = registry.get(request.getAction());
                    if (handler != null) {
                        Response handled = handler.handle(request, authUser);
                        out.println(gson.toJson(handled));

                        // Broadcast cho PLACE_BID nếu handler trả event
                        if ("PLACE_BID".equals(request.getAction())
                                && handled != null
                                && "SUCCESS".equals(handled.getStatus())
                                && handled.getPayload() != null) {
                            try {
                                String payloadStr = String.valueOf(handled.getPayload());
                                JsonObject wrap = JsonParser.parseString(payloadStr).getAsJsonObject();
                                if (wrap.has("event")) {
                                    broadcast(wrap.get("event").toString());
                                }
                            } catch (Exception ignored) {
                            }
                        }

                        continue;
                    }

                    Response response = new Response("ERROR", "Hành động không hợp lệ", null);
                    out.println(gson.toJson(response));

                } catch (Exception reqEx) {
                    reqEx.printStackTrace();
                    Response errorResponse = new Response("ERROR", "Dữ liệu yêu cầu không hợp lệ", null);
                    out.println(gson.toJson(errorResponse));
                }
            }

        } catch (Exception e) {
            System.out.println("Client ngắt kết nối: " + e.getMessage());
        } finally {
            if (out != null) {
                clientWriters.remove(out);
            }
            try {
                socket.close();
            } catch (Exception ignore) {
            }
        }
    }

    private void broadcast(String message) {
        for (PrintWriter writer : clientWriters) {
            try {
                writer.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Public static method cho handlers gọi broadcast.
     * Dùng để gửi real-time updates (auto-bid, bid updates) cho tất cả clients.
     */
    public static void broadcastAll(String message) {
        for (PrintWriter writer : clientWriters) {
            try {
                writer.println(message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}