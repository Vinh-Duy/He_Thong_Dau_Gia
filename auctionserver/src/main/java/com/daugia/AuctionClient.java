package com.daugia;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class AuctionClient extends JFrame {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();

    private JTextField txtUsername;
    private JTextField txtAuctionId;
    private JTextField txtBidAmount;
    private JLabel lblPrice;
    private JTextArea txtConsole;

    public AuctionClient() {
        setTitle("App Đấu Giá Pro - Client");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel panelTop = new JPanel(new GridLayout(4, 2, 5, 5));
        panelTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panelTop.add(new JLabel("Tên người chơi:"));
        txtUsername = new JTextField("NguoiChoi1");
        panelTop.add(txtUsername);

        panelTop.add(new JLabel("Mã món hàng (Auction ID):"));
        txtAuctionId = new JTextField("1");
        panelTop.add(txtAuctionId);

        panelTop.add(new JLabel("Nhập giá muốn đặt:"));
        txtBidAmount = new JTextField();
        panelTop.add(txtBidAmount);

        JButton btnBid = new JButton("VUNG TIỀN ĐẶT GIÁ!");
        // ĐÃ FIX UI: Bỏ background đi để nút sắc nét, chỉ in đậm và tô đỏ chữ
        btnBid.setFont(new Font("Arial", Font.BOLD, 14));
        btnBid.setForeground(Color.RED); 
        panelTop.add(btnBid);

        JButton btnGetAuctions = new JButton("TẢI DANH SÁCH HÀNG");
        panelTop.add(btnGetAuctions);

        add(panelTop, BorderLayout.NORTH);

        lblPrice = new JLabel("Giá hiện tại: --- VNĐ", SwingConstants.CENTER);
        lblPrice.setFont(new Font("Arial", Font.BOLD, 22));
        lblPrice.setForeground(Color.BLUE);
        lblPrice.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblPrice, BorderLayout.CENTER);

        txtConsole = new JTextArea();
        txtConsole.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtConsole);
        scrollPane.setBorder(new TitledBorder("Thông báo & Danh sách đấu giá"));
        scrollPane.setPreferredSize(new Dimension(450, 150));
        add(scrollPane, BorderLayout.SOUTH);

        btnBid.addActionListener(e -> placeBid());
        btnGetAuctions.addActionListener(e -> requestAllAuctions());

        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 8888);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            logToConsole("Đã kết nối thành công tới Server!");
            new Thread(() -> listenFromServer()).start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Không thể kết nối Server!");
            System.exit(0);
        }
    }

    private void requestAllAuctions() {
        JsonObject request = new JsonObject();
        request.addProperty("action", "GET_ALL_AUCTIONS");
        request.addProperty("payload", "");
        out.println(gson.toJson(request));
    }

    private void placeBid() {
        try {
            String auctionId = txtAuctionId.getText().trim();
            double amount = Double.parseDouble(txtBidAmount.getText().trim());
            String username = txtUsername.getText().trim();

            if (auctionId.isEmpty() || username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập đủ Mã và Tên!");
                return;
            }

            JsonObject payload = new JsonObject();
            payload.addProperty("auctionId", auctionId);
            payload.addProperty("amount", amount);
            payload.addProperty("username", username);

            JsonObject request = new JsonObject();
            request.addProperty("action", "PLACE_BID");
            request.addProperty("payload", payload.toString());

            out.println(gson.toJson(request));
            txtBidAmount.setText(""); 
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập số tiền hợp lệ!");
        }
    }

    // VÒNG LẶP LẮNG NGHE SERVER (BẢN VÁ LỖI CUỐI CÙNG)
    private void listenFromServer() {
        try {
            String serverMessage;
            while ((serverMessage = in.readLine()) != null) {
                System.out.println("=> [CLIENT NHẬN ĐƯỢC]: " + serverMessage);
                
                try {
                    JsonObject response = JsonParser.parseString(serverMessage).getAsJsonObject();
                    
                    String action = response.has("action") && !response.get("action").isJsonNull() ? response.get("action").getAsString() : "";
                    String status = response.has("status") && !response.get("status").isJsonNull() ? response.get("status").getAsString() : "";
                    String message = response.has("message") && !response.get("message").isJsonNull() ? response.get("message").getAsString() : "";
                    
                    String noiDungHang = "";
                    if (response.has("payload") && !response.get("payload").isJsonNull()) {
                        noiDungHang = response.get("payload").getAsString();
                    } else if (response.has("data") && !response.get("data").isJsonNull()) {
                        noiDungHang = response.get("data").getAsString();
                    }

                    // 1. NHẬN DANH SÁCH
                    if ("SUCCESS".equals(status) && message.contains("Lấy danh sách")) {
                        if (!noiDungHang.isEmpty()) {
                            JsonArray danhSachHang = JsonParser.parseString(noiDungHang).getAsJsonArray();
                            SwingUtilities.invokeLater(() -> {
                                txtConsole.setText("=== DANH SÁCH HÀNG ===\n\n");
                                for (int i = 0; i < danhSachHang.size(); i++) {
                                    JsonObject item = danhSachHang.get(i).getAsJsonObject();
                                    String id = item.get("id").getAsString();
                                    String name = item.get("name").getAsString();
                                    String priceDisplay = String.format("%.0f", item.get("currentHighestBid").getAsDouble());
                                    txtConsole.append("📦 ID: " + id + " | 📱 " + name + " | 💰 " + priceDisplay + " VNĐ\n");
                                }
                            });
                        }
                    }
                    
                    // 2. MÌNH ĐẶT GIÁ THÀNH CÔNG (ĐÃ FIX ÉP SỐ TRỰC TIẾP)
                    else if ("SUCCESS".equals(status) && message.contains("Đặt giá thành công")) {
                        try {
                            // Server chỉ gửi số (VD: "3.5E7") thì mình đọc thẳng thành Double luôn, không cần getAsJsonObject() nữa!
                            double newPrice = Double.parseDouble(noiDungHang.replace("\"", "")); 
                            String priceDisplay = String.format("%.0f", newPrice);
                            updatePriceUI(priceDisplay);
                            logToConsole("✅ BẠN ĐÃ CHỐT GIÁ THÀNH CÔNG: " + priceDisplay + " VNĐ");
                        } catch (Exception e) {
                            logToConsole("✅ Bạn đã đặt giá thành công!");
                        }
                    }
                    
                    // 3. NGƯỜI KHÁC ĐẶT GIÁ -> NHẢY SỐ (ĐÃ FIX CẢ 'action' LẪN 'status')
                    else if ("BID_UPDATE".equals(action) || "BID_UPDATE".equals(status)) {
                        System.out.println("=> [XỬ LÝ]: Đang nhảy số...");
                        
                        com.google.gson.JsonElement element = JsonParser.parseString(noiDungHang);
                        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                            element = JsonParser.parseString(element.getAsString()); 
                        }
                        JsonObject broadcastData = element.getAsJsonObject();
                        
                        String updatedAuctionId = broadcastData.get("auctionId").getAsString();
                        double newBid = broadcastData.get("newHighestBid").getAsDouble();
                        String priceDisplay = String.format("%.0f", newBid);
                        
                        if (updatedAuctionId.equals(txtAuctionId.getText().trim())) {
                            updatePriceUI(priceDisplay);
                            logToConsole("🔥 BÁO ĐỘNG: Có người vừa đẩy giá lên " + priceDisplay + " VNĐ!");
                        }
                    }
                    
                    // 4. BÁO LỖI
                    else if ("ERROR".equals(status)) {
                        logToConsole("❌ LỖI: " + message);
                        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception innerEx) {
                    System.out.println("=> [LỖI APP]: Gặp lỗi khi đọc JSON: " + innerEx.getMessage());
                }
            }
        } catch (Exception e) {
            logToConsole("Mất kết nối tới Server...");
        }
    }
    private void updatePriceUI(String price) {
        SwingUtilities.invokeLater(() -> {
            lblPrice.setText("Giá hiện tại: " + price + " VNĐ");
        });
    }

    private void logToConsole(String text) {
        SwingUtilities.invokeLater(() -> {
            txtConsole.append(text + "\n");
            txtConsole.setCaretPosition(txtConsole.getDocument().getLength()); 
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AuctionClient().setVisible(true));
    }
}