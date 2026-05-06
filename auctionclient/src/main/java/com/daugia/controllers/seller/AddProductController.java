package com.daugia.controllers.seller;

import com.daugia.models.Auction;
import com.daugia.network.NetworkClient;
import com.daugia.network.Request;
import com.daugia.network.Response;
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
    private Auction editingAuction = null;

    private Gson gson = new Gson();

    @FXML
    private void handleAddProduct() {
        try {
            if (txtName.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu tin", "Bác nhập tên sản phẩm nhé!");
                return;
            }

            // Dùng Auction đang sửa (nếu có), không thì tạo mới
            Auction auction = (editingAuction != null) ? editingAuction : new Auction();
            auction.setName(txtName.getText());
            auction.setProductName(txtName.getText());
            auction.setDescription(txtDescription.getText());
            auction.setStartingPrice(Double.parseDouble(txtStartingPrice.getText()));
            // Set thêm ngày giờ ở đây...

            String payload = gson.toJson(auction);
            
            // QUYẾT ĐỊNH LỆNH GỬI: UPDATE hay ADD
            String command = (editingAuction != null) ? "UPDATE_PRODUCT" : "ADD_PRODUCT";

            new Thread(() -> {
                try {
                    Request req = new Request(command, payload);
                    Response res = NetworkClient.getInstance().sendRequest(req);
                    
                    Platform.runLater(() -> {
                        if (res != null && "SUCCESS".equals(res.getStatus())) {
                            String msg = (editingAuction != null) ? "Sửa thành công!" : "Hàng đã lên sàn thành công!";
                            showAlert(Alert.AlertType.INFORMATION, "Ngon rồi", msg);
                            
                            // Lưu xong thì quay lại màn quản lý luôn cho tiện
                            try {
                                Parent root = FXMLLoader.load(getClass().getResource("/views/seller/ManageProductView.fxml"));
                                Stage stage = (Stage) txtName.getScene().getWindow();
                                stage.getScene().setRoot(root);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Server từ chối: " + (res != null ? res.getMessage() : ""));
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
            Parent root = FXMLLoader.load(getClass().getResource("/views/seller/ManageProductView.fxml"));
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

    public void setAuctionToEdit(Auction auction) {
        this.editingAuction = auction;
        
        // Đổ dữ liệu cũ vào các ô nhập liệu
        txtName.setText(auction.getName());
        txtDescription.setText(auction.getDescription());
        txtStartingPrice.setText(String.valueOf((long) auction.getStartingPrice())); // Ép kiểu tùy thuộc thuộc tính của bác
        
        // Bác có thể parse thêm Date/Time ở đây nếu Server lưu chuẩn
    }
}