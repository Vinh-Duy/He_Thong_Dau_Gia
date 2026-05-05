package com.daugia.controllers.seller;

import com.daugia.models.Auction;
import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;
import com.daugia.utils.SessionManager;
import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class AddProductController {
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtStartingPrice;
    @FXML private DatePicker dateEnd;
    @FXML private TextField txtTimeEnd;

    private Gson gson = new Gson();

    @FXML
    private void handleAddProduct() {
        try {
            // 1. Kiểm tra đầu vào
            if (txtName.getText().isEmpty() || dateEnd.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu tin", "Bác nhập tên sản phẩm với ngày kết thúc đã nhé!");
                return;
            }

            // 2. Khởi tạo đối tượng Auction (Dùng constructor rỗng như trong file của bác)
            Auction auction = new Auction();
            auction.setName(txtName.getText());
            auction.setProductName(txtName.getText());
            auction.setCategory("Điện thoại");
            auction.setDescription(txtDescription.getText());
            double price = Double.parseDouble(txtStartingPrice.getText());
            auction.setStartingPrice(price);
            auction.setStartPrice(price);
            auction.setEndTime(dateEnd.getValue().toString() + " " + txtTimeEnd.getText() + ":00");
            
            // Lấy đúng sellerId từ SessionManager bác vừa sửa ở Bước 1
            auction.setSellerId(SessionManager.getUserId()); 

            auction.setSellerId(SessionManager.getUserId()); 

            // THÊM 2 DÒNG NÀY VÀO ĐỂ BẮT BỆNH:
            System.out.println("=== KIỂM TRA DỮ LIỆU TRƯỚC KHI GỬI ===");
            System.out.println("1. ID Người bán đang là: " + SessionManager.getUserId());
            System.out.println("2. Chuỗi JSON gửi lên Server: " + gson.toJson(auction));

            // 3. Gửi lên Server
            new Thread(() -> {
                try {
                    String jsonReq = gson.toJson(auction);
                    Request req = new Request("ADD_PRODUCT", jsonReq);
                    Response res = NetworkClient.getInstance().sendRequest(req);

                    System.out.println("=== KẾT QUẢ TỪ SERVER TRẢ VỀ ===");
                    if (res == null) {
                        System.out.println("Toang rồi: Biến res bị NULL! (Lỗi ở hàm sendRequest)");
                    } else {
                        System.out.println("Status nhận được: " + res.getStatus());
                        System.out.println("Message nhận được: " + res.getMessage());
                    }

                    Platform.runLater(() -> {
                        if (res != null && "SUCCESS".equals(res.getStatus())) {
                            showAlert(Alert.AlertType.INFORMATION, "Ngon rồi", "Hàng đã lên sàn thành công!");
                            handleClear();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Server không nhận sản phẩm bác ạ.");
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }).start();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Bác check lại giá tiền (phải là số) nhé!");
        }
    }

    // --- HÀM THOÁT RA HOMEVIEW CHO SELLER ---
    @FXML
    private void goBackHome(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/common/HomeView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        txtName.clear();
        txtDescription.clear();
        txtStartingPrice.clear();
        txtTimeEnd.clear();
        dateEnd.setValue(null);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleClear() {
        clearFields();
    }
}