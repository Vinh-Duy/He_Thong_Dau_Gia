package com.bidnova.controllers.bidder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

import com.bidnova.models.BidMessage;
import com.bidnova.models.Product;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class AuctionDetailController {

    // ── FXML inject — tên phải khớp fx:id trong FXML ──
    @FXML private Label  lblProductName;
    @FXML private Label  lblProductPrice;
    @FXML private Label  lblCurrentBid;
    @FXML private Label  lblTimer;
    @FXML private Pane   imgProduct;
    @FXML private TextField           txtBidAmount;
    @FXML private ListView<String>    listHistory;
    @FXML private LineChart<String, Number> bidChart;
    @FXML private CategoryAxis        timeAxis;
    @FXML private NumberAxis          priceAxis;

    // ── Chart series: "đường vẽ" trên biểu đồ ──
    // Mỗi XYChart.Data<String, Number> = 1 điểm (timestamp, giá)
    private XYChart.Series<String, Number> bidSeries;

    // ── Network ──
    private static final String HOST = "localhost";
    private static final int    PORT = 8888;
    private Socket       socket;
    private PrintWriter  out;
    private BufferedReader serverIn;

    private final Gson gson = new Gson();

    // ── State ──
    private int    currentAuctionId;
    private String currentUsername = "TestUser"; // TODO: truyền từ màn hình đăng nhập

    @FXML
    public void initialize() {
        setupChart();
    }

    // Khởi tạo series rỗng và format trục Y
    private void setupChart() {
        bidSeries = new XYChart.Series<>();
        bidChart.getData().add(bidSeries);
        priceAxis.setForceZeroInRange(false);

        // Format nhãn trục Y: chia tỷ cho dễ đọc
        priceAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
            @Override public String toString(Number v) {
                if (v.longValue() >= 1_000_000_000L)
                    return String.format("%.1f tỷ", v.doubleValue() / 1_000_000_000.0);
                return String.format("%.0f tr", v.doubleValue() / 1_000_000.0);
            }
            @Override public Number fromString(String s) { return 0; }
        });
    }

    /**
     * Gọi từ ProductCardController khi user click vào card sản phẩm.
     * Hiển thị thông tin rồi kết nối server.
     */
    public void setData(Product product) {
        lblProductName.setText(product.getName());
        lblProductPrice.setText("Giá khởi điểm: "
            + String.format("%,d VNĐ", product.getStartingPrice()));
        lblCurrentBid.setText(String.format("%,d VNĐ", product.getStartingPrice()));
        imgProduct.setStyle("-fx-background-image: url('" + product.getImagePath()
            + "'); -fx-background-size: cover; -fx-background-position: center;");

        this.currentAuctionId = product.getId(); // int, khớp auctionId trên server
        connectToServer();
    }

    /**
     * Mở socket, gửi JOIN_AUCTION, khởi chạy background thread lắng nghe.
     */
    private void connectToServer() {
        try {
            socket   = new Socket(HOST, PORT);
            out      = new PrintWriter(socket.getOutputStream(), true);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Gửi JOIN_AUCTION → server sẽ joinRoom(auctionId, out) và trả về BID_HISTORY
            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", currentAuctionId);
            payload.addProperty("username",  currentUsername);
            out.println(gson.toJson(new Request("JOIN_AUCTION", payload.toString())));

            // ── REALTIME STEP 1 (CLIENT SIDE) ────────────────────────────────
            // Bắt đầu thread riêng để lắng nghe server.
            // KHÔNG chạy trên JavaFX thread vì readLine() sẽ BLOCK làm đơ toàn bộ UI.
            // Thread này chạy vô tận, chờ server gửi dữ liệu.
            Thread listener = new Thread(this::listenFromServer);
            listener.setDaemon(true); // tự tắt khi app đóng
            listener.start();
            // ─────────────────────────────────────────────────────────────────

        } catch (Exception e) {
            System.out.println("Không kết nối được server: " + e.getMessage());
        }
    }

    /**
     * Vòng lặp chạy trên BACKGROUND THREAD.
     * Mỗi dòng JSON từ server được xử lý tại đây.
     *
     * Có 2 loại message server gửi về:
     *   1. Response (có field "status") → phản hồi cho request của mình
     *   2. BidMessage (có field "type") → broadcast từ server khi ai đó đặt giá
     */
    private void listenFromServer() {
        try {
            String line;
            while ((line = serverIn.readLine()) != null) {
                JsonObject obj = JsonParser.parseString(line).getAsJsonObject();

                if (obj.has("status")) {
                    // Đây là Response (phản hồi cho JOIN_AUCTION hoặc PLACE_BID)
                    handleResponse(gson.fromJson(line, Response.class));
                } else if (obj.has("type")) {
                    // ── REALTIME STEP 2 (CLIENT SIDE) ────────────────────────
                    // Đây là BidMessage broadcast — ai đó (kể cả mình) vừa đặt giá thành công.
                    // Gọi handleBroadcast() để cập nhật biểu đồ.
                    handleBroadcast(gson.fromJson(line, BidMessage.class));
                    // ─────────────────────────────────────────────────────────
                }
            }
        } catch (Exception e) {
            System.out.println("Mất kết nối server.");
        }
    }

    /**
     * Xử lý Response từ server.
     * BID_HISTORY: vẽ lại toàn bộ chart từ lịch sử (dùng khi mới JOIN_AUCTION)
     */
    private void handleResponse(Response response) {
        if (!"BID_HISTORY".equals(response.getStatus())) return;

        Type listType = new TypeToken<List<BidMessage>>() {}.getType();
        List<BidMessage> history = gson.fromJson(response.getData(), listType);

        // Platform.runLater: mọi thao tác với UI PHẢI chạy trên JavaFX thread
        Platform.runLater(() -> {
            bidSeries.getData().clear();
            listHistory.getItems().clear();
            for (BidMessage entry : history) {
                // ── VẼ LẠI CHART TỪ LỊCH SỬ ────────────────────────────
                addChartPoint(entry.getTimestamp(), entry.getAmount());
                // ─────────────────────────────────────────────────────────
                addLogEntry(entry);
            }
            if (!history.isEmpty()) {
                BidMessage last = history.get(history.size() - 1);
                lblCurrentBid.setText(String.format("%,.0f VNĐ  (%s)",
                    last.getAmount(), last.getUsername()));
            }
        });
    }

    /**
     * Xử lý BidMessage broadcast — được gọi khi BẤT KỲ AI đặt giá thành công.
     *
     * ── ĐÂY LÀ NƠI BIỂU ĐỒ CẬP NHẬT REALTIME ──
     * Mỗi khi server broadcast NEW_BID, hàm này:
     *   1. Thêm điểm mới vào bidSeries → đường trên chart kéo dài thêm 1 điểm
     *   2. Cập nhật lblCurrentBid → giá hiện tại thay đổi ngay lập tức
     *   3. Thêm dòng log vào listHistory
     * Tất cả diễn ra trong Platform.runLater() để đảm bảo chạy trên JavaFX thread.
     */
    private void handleBroadcast(BidMessage msg) {
        if (!"NEW_BID".equals(msg.getType())) return;
        if (msg.getAuctionId() != currentAuctionId) return;

        Platform.runLater(() -> {
            // ── REALTIME: thêm điểm mới lên biểu đồ ───────────────────
            addChartPoint(msg.getTimestamp(), msg.getAmount());
            // ────────────────────────────────────────────────────────────

            // ── REALTIME: cập nhật label giá ───────────────────────────
            lblCurrentBid.setText(String.format("%,.0f VNĐ  (%s)",
                msg.getAmount(), msg.getUsername()));
            // ────────────────────────────────────────────────────────────

            addLogEntry(msg);
        });
    }

    /**
     * Thêm 1 điểm (timestamp, giá) vào LineChart.
     * Chỉ gọi từ Platform.runLater().
     */
    private void addChartPoint(String timestamp, double amount) {
        bidSeries.getData().add(new XYChart.Data<>(timestamp, amount));
        // Chỉ giữ 60 điểm gần nhất để chart không bị quá dày
        if (bidSeries.getData().size() > 60) {
            bidSeries.getData().remove(0);
        }
    }

    /**
     * Thêm 1 dòng log vào ListView.
     * Chỉ gọi từ Platform.runLater().
     */
    private void addLogEntry(BidMessage msg) {
        String entry = String.format("[%s]  %s  →  %,.0f VNĐ",
            msg.getTimestamp(), msg.getUsername(), msg.getAmount());
        listHistory.getItems().add(0, entry); // thêm đầu list: mới nhất ở trên
    }

    /**
     * Gọi khi user nhấn "ĐẤU GIÁ NGAY" (onAction="#handlePlaceBid" trong FXML).
     * Gửi PLACE_BID lên server. Server validate rồi broadcast kết quả.
     */
    @FXML
    private void handlePlaceBid() {
        String input = txtBidAmount.getText().trim().replace(",", "");
        if (input.isEmpty()) return;

        try {
            double amount = Double.parseDouble(input);

            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", currentAuctionId);
            payload.addProperty("amount",    amount);
            payload.addProperty("username",  currentUsername);

            out.println(gson.toJson(new Request("PLACE_BID", payload.toString())));
            txtBidAmount.clear();

        } catch (NumberFormatException e) {
            lblCurrentBid.setText("Giá nhập không hợp lệ!");
        }
    }

    // Gọi khi rời màn hình này để đóng socket sạch sẽ
    public void disconnect() {
        try { if (socket != null) socket.close(); }
        catch (Exception ignored) {}
    }
}