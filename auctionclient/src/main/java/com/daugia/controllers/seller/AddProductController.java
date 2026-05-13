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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class AddProductController {
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtStartingPrice;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private DatePicker dateEnd;
    @FXML private TextField txtTimeEnd;
    private Auction editingAuction = null;

    private Gson gson = new Gson();

    @FXML
    public void initialize() {
        // Khởi tạo danh mục sản phẩm
        cmbCategory.getItems().addAll(
            "Bất động sản",
            "Tài sản nhà nước", 
            "Phương tiện - xe cộ",
            "Sưu tầm - nghệ thuật",
            "Tài sản khác"
        );
    }

    @FXML
    private void handleAddProduct() {
        try {
            if (txtName.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu tin", "Bác nhập tên sản phẩm nhé!");
                return;
            }
            if (cmbCategory.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu tin", "Bác chọn phân loại sản phẩm nhé!");
                return;
            }
            if (txtStartingPrice.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu tin", "Bác phải nhập giá khởi điểm nhé!");
                return;
            }

            // Dùng Auction đang sửa (nếu có), không thì tạo mới
            Auction auction = (editingAuction != null) ? editingAuction : new Auction();
            auction.setProductName(txtName.getText());
            auction.setCategory(cmbCategory.getValue());
            auction.setDescription(txtDescription.getText());
            
            double startPrice = Double.parseDouble(txtStartingPrice.getText());
            if (startPrice <= 0) {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Giá khởi điểm phải lớn hơn 0 đồng!");
                return;
            }
            
            auction.setStartPrice(startPrice);
            // Khởi tạo current highest bid = start price
            auction.setCurrentHighestBid(startPrice);
            
            // Xử lý ngày giờ kết thúc
            if (dateEnd.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu tin", "Bác chọn ngày kết thúc nhé!");
                return;
            }
            if (txtTimeEnd.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu tin", "Bác nhập giờ kết thúc (HH:mm) nhé!");
                return;
            }
            
            // Format endTime: yyyy-MM-dd HH:mm:ss
            String endDateTime = dateEnd.getValue() + " " + txtTimeEnd.getText() + ":00";
            auction.setEndTime(endDateTime);
            auction.setStatus("OPEN");

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

    @FXML
    private void goBackToManage(MouseEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/views/seller/ManageProductView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- HÀM THOÁT RA HOMEVIEW CHO SELLER ---
    @FXML
    private void goBackHome(MouseEvent event) {
        try {
            // Đăng xuất và về màn hình đăng nhập
            com.daugia.utils.SessionManager.logout();
            
            Parent root = FXMLLoader.load(getClass().getResource("/views/auth/LoginPopup.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setTitle("Đăng nhập");
            stage.sizeToScene();
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        txtName.clear();
        txtDescription.clear();
        cmbCategory.getSelectionModel().clearSelection();
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
        txtName.setText(auction.getProductName());
        txtDescription.setText(auction.getDescription());
        if (auction.getCategory() != null) {
            cmbCategory.setValue(auction.getCategory());
        }
        txtStartingPrice.setText(String.valueOf(auction.getStartPrice()));
        
        // Bác có thể parse thêm Date/Time ở đây nếu Server lưu chuẩn
    }
}