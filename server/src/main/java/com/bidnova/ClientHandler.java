package com.bidnova;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.bidnova.handlers.ActionHandler;
import com.bidnova.handlers.HandlerRegistry;
import com.bidnova.models.AuthUserContext;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.services.AuthService;
import com.bidnova.services.impl.AuthServiceImpl;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * ClientHandler - Xử lý kết nối của mỗi client độc lập
 * 
 * <h2>Chức Năng:</h2>
 * <ul>
 *   <li>Đọc request JSON từ client qua socket</li>
 *   <li>Xác thực token người dùng (Oauth)</li>
 *   <li>Dispatch request tới ActionHandler thích hợp</li>
 *   <li>Broadcast real-time updates tới tất cả clients</li>
 *   <li>Ghi lại lịch sử hoạt động</li>
 * </ul>
 * 
 * <h2>Kiến Trúc Request-Response:</h2>
 * <pre>
 * Client                     Server (ClientHandler)
 *   │                              │
 *   ├─ Request (JSON) ────→ Nhận dữ liệu
 *   │                         │
 *   │                         ├─ Parse JSON
 *   │                         ├─ Validate Token
 *   │                         ├─ Get ActionHandler
 *   │                         └─ Handle.handle()
 *   │                              │
 *   ←─ Response (JSON) ────────── Gửi kết quả
 *   │
 *   ├─ Broadcast (nếu cần) ← Nhận cập nhật thời gian thực
 * </pre>
 * 
 * <h2>Action Công Khai (Không cần token):</h2>
 * <ul>
 *   <li>LOGIN - Đăng nhập</li>
 *   <li>REGISTER - Đăng ký tài khoản</li>
 * </ul>
 * 
 * <h2>Action Yêu Cầu Token:</h2>
 * <ul>
 *   <li>PLACE_BID - Đặt giá (trigger broadcast)</li>
 *   <li>SET_AUTO_BID - Thiết lập auto bid</li>
 *   <li>ADD_PRODUCT - Tạo phiên đấu giá (trigger broadcast)</li>
 *   <li>Và 15+ action khác...</li>
 * </ul>
 * 
 * @author BidNova Team
 * @version 1.0
 * @see ActionHandler
 * @see HandlerRegistry
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    private final HandlerRegistry registry = new HandlerRegistry();
    private final AuthService authService = new AuthServiceImpl();

    private static final Set<PrintWriter> clientWriters = ConcurrentHashMap.newKeySet();
    private static final Set<String> PUBLIC_ACTIONS =
            new HashSet<>(Arrays.asList("LOGIN", "REGISTER", "GET_ALL_AUCTIONS", 
                                      "GET_AUCTIONS_BY_CATEGORY", "GET_AUCTION_BY_ID",
                                      "GET_BID_HISTORY"));

    /**
     * Constructor - Khởi tạo handler cho một client mới
     * 
     * @param socket Socket kết nối tới client
     */
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    /**
     * run() - Luồng chính xử lý request từ client
     * 
     * <h3>Quy Trình:</h3>
     * <ol>
     *   <li>Mở BufferedReader để đọc JSON từ client</li>
     *   <li>Mở PrintWriter để gửi response lại</li>
     *   <li>Vòng lặp đọc request cho đến khi client đóng</li>
     *   <li>Mỗi request:
     *     <ul>
     *       <li>Parse JSON thành Request object</li>
     *       <li>Xác thực token (nếu không phải PUBLIC_ACTION)</li>
     *       <li>Tìm ActionHandler phù hợp</li>
     *       <li>Gọi handler.handle(request, authUser)</li>
     *       <li>Gửi response JSON lại client</li>
     *       <li>Broadcast nếu là PLACE_BID hoặc sửa sản phẩm</li>
     *     </ul>
     *   </li>
     *   <li>Cleanup: Đóng socket, xóa PrintWriter khỏi broadcast list</li>
     * </ol>
     * 
     * <h3>Exception Handling:</h3>
     * <ul>
     *   <li>JSON parse error → Gửi error response</li>
     *   <li>Token không hợp lệ → ERROR: "Unauthorized: token không hợp lệ"</li>
     *   <li>Action không tìm thấy → ERROR: "Hành động không hợp lệ"</li>
     *   <li>Client ngắt kết nối → In log và cleanup</li>
     * </ul>
     */
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
                    // Bỏ qua dòng trống hoặc chỉ whitespace
                    if (inputLine.trim().isEmpty()) {
                        continue;
                    }

                    // Phát hiện HTTP request (health check từ Render) và skip
                    if (isHttpRequest(inputLine)) {
                        System.out.println(" HTTP request detected (health check), skipping: " + inputLine.substring(0, Math.min(50, inputLine.length())));
                        // Read HTTP headers until blank line, then ignore
                        String headerLine;
                        while ((headerLine = in.readLine()) != null && !headerLine.trim().isEmpty()) {
                            // Skip HTTP headers
                        }
                        // Send minimal HTTP response to satisfy health check
                        out.println("HTTP/1.1 200 OK");
                        out.println("Content-Length: 0");
                        out.println();
                        out.flush();
                        continue;
                    }

                    Request request = null;
                    try {
                        request = gson.fromJson(inputLine, Request.class);
                    } catch (JsonSyntaxException e) {
                        // Log input để debug
                        System.err.println("❌ JSON Parse Error - Received: " + inputLine);
                        System.err.println("❌ Error: " + e.getMessage());
                        out.println(gson.toJson(new Response("ERROR", "Dữ liệu gửi lên không phải JSON hợp lệ", null)));
                        continue;
                    }

                    if (request == null || request.getAction() == null || request.getAction().isBlank()) {
                        out.println(gson.toJson(new Response("ERROR", "Thiếu action trong request", null)));
                        continue;
                    }

                    // Xác thực người dùng
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
                                    broadcastAll(wrap.get("event").toString());
                                }
                            } catch (Exception ignored) {
                            }
                        }

                        // Tự động broadcast khi có thay đổi sản phẩm (Thêm/Sửa/Xóa)
                        if (isProductModification(request.getAction()) && "SUCCESS".equals(handled.getStatus())) {
                            JsonObject updateMsg = new JsonObject();
                            updateMsg.addProperty("action", "AUCTION_LIST_UPDATE");
                            broadcastAll(gson.toJson(updateMsg));
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
     * isProductModification() - Kiểm tra xem action có làm thay đổi sản phẩm/đấu giá không
     * 
     * @param action Tên action (ví dụ: "ADD_PRODUCT", "PLACE_BID", etc.)
     * @return true nếu action gây thay đổi cần broadcast, false ngược lại
     * 
     * <h3>Actions Trigger Broadcast:</h3>
     * <ul>
     *   <li>ADD_PRODUCT - Tạo phiên đấu giá mới</li>
     *   <li>UPDATE_PRODUCT - Cập nhật thông tin phiên</li>
     *   <li>DELETE_PRODUCT - Xóa phiên đấu giá</li>
     *   <li>PLACE_BID - Đặt giá (cập nhật giá hiện tại)</li>
     * </ul>
     */
    private boolean isProductModification(String action) {
        return "ADD_PRODUCT".equals(action) || "UPDATE_PRODUCT".equals(action) || 
               "DELETE_PRODUCT".equals(action) || "PLACE_BID".equals(action);
    }

    /**
     * broadcastAll() - Gửi message tới tất cả clients kết nối
     * 
     * <h3>Chức Năng:</h3>
     * <p>Lặp qua toàn bộ PrintWriter trong clientWriters set và gửi message.</p>
     * <p>Được gọi khi có thay đổi thời gian thực cần cập nhật mọi client:</p>
     * <ul>
     *   <li>Cập nhật giá sau khi PLACE_BID</li>
     *   <li>Danh sách phiên đấu giá thay đổi (ADD/UPDATE/DELETE)</li>
     *   <li>AutoBid trigger thành công</li>
     *   <li>Phiên đấu giá kết thúc</li>
     * </ul>
     * 
     * @param message JSON message để gửi (ví dụ: broadcast event)
     * 
     * <h3>Ví Dụ Message:</h3>
     * <pre>
     * {
     *   "action": "AUCTION_LIST_UPDATE",
     *   "status": "SUCCESS"
     * }
     * </pre>
     * 
     * <h3>Exception Handling:</h3>
     * <p>Nếu writer throw exception (client đã disconnect), log error nhưng không crash server.</p>
     * 
     * @since Dùng cho real-time updates
     * @see #isProductModification(String)
     */
    public static void broadcastAll(String message) {
        // Issue 4 Fix: Add null checks and remove disconnected writers
        if (message == null || message.isEmpty()) {
            return;
        }
        
        for (PrintWriter writer : clientWriters) {
            if (writer == null) {
                continue;
            }
            try {
                if (!writer.checkError()) {
                    writer.println(message);
                    writer.flush();
                } else {
                    // Writer has error, queue it for removal
                    clientWriters.remove(writer);
                }
            } catch (Exception e) {
                System.err.println("Error broadcasting to client: " + e.getMessage());
                clientWriters.remove(writer);
            }
        }
    }

    /**
     * Phát hiện xem dòng input có phải HTTP request không
     * (GET, POST, HEAD, PUT, DELETE, etc.)
     */
    private boolean isHttpRequest(String line) {
        String[] httpMethods = {"GET", "POST", "HEAD", "PUT", "DELETE", "PATCH", "OPTIONS"};
        for (String method : httpMethods) {
            if (line.startsWith(method + " ")) {
                return true;
            }
        }
        return false;
    }
}