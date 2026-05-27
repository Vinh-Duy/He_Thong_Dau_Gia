package com.daugia;

/**
 * ĐÂY LÀ "TIẾP VIÊN HÀNG KHÔNG" CỦA SERVER.
 *
 * Mỗi khi có 1 client (app JavaFX) kết nối vào Server qua Socket (cổng 8888),
 * Server tạo 1 đối tượng ClientHandler MỚI để xử lý riêng client đó.
 * => Mỗi người dùng đang online = 1 ClientHandler đang chạy song song (multi-thread).
 *
 * Nhiệm vụ chính:
 * 1. Đọc tin nhắn JSON từ client gửi lên.
 * 2. Dùng HandlerRegistry để tìm đúng "chuyên gia" xử lý (ví dụ: LOGIN -> LoginHandler).
 * 3. Gửi kết quả trả về cho client.
 * 4. PHÁT SÓNG (broadcast) thông báo cho TẤT CẢ clients khi có sự kiện quan trọng
 *    (ví dụ: giá đấu thay đổi -> mọi người phải thấy ngay).
 */

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

                        // 🔴 REAL-TIME BROADCAST 🔴
                        // Khi có người đặt giá thành công (PLACE_BID),
                        // Server phải THÔNG BÁO cho TẤT CẢ clients khác biết giá đã thay đổi.
                        // Ví dụ: User A đặt giá -> User B đang nhìn màn hình cũng thấy giá nhảy lên ngay lập tức.
                        if ("PLACE_BID".equals(request.getAction())
                                && handled != null
                                && "SUCCESS".equals(handled.getStatus())
                                && handled.getPayload() != null) {
                            try {
                                String payloadStr = String.valueOf(handled.getPayload());
                                JsonObject wrap = JsonParser.parseString(payloadStr).getAsJsonObject();
                                if (wrap.has("event")) {
                                    // Gọi hàm static broadcastAll để gửi tin nhắn cho mọi client đang kết nối
                                    broadcastAll(wrap.get("event").toString());
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

    /**
     * PHÁT SÓNG THÔNG BÁO CHO TẤT CẢ CLIENT.
     *
     * Tại sao phải là STATIC?
     * - Vì các Handler (như PlaceBidHandler, AutoBidService...) KHÔNG có reference
     *   đến instance ClientHandler cụ thể nào. Mỗi client kết nối tạo 1 ClientHandler mới.
     * - Static cho phép GỌI TỪ BẤT CỨ ĐÂU trong project: ClientHandler.broadcastAll(msg)
     *
     * Tại sao lại có 2 hàm giống nhau trước đây?
     * - Cũ: 1 hàm private (dùng trong run()), 1 hàm static public (dùng ngoài handler).
     * - Giờ: CHỈ GIỮ 1 HÀM STATIC cho cả 2 chỗ, gọn gàng hơn.
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