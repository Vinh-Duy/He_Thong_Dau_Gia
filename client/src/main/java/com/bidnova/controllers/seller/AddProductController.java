package com.bidnova.controllers.seller;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.bidnova.models.Auction;
import com.bidnova.network.NetworkClient;
import com.bidnova.network.Request;
import com.bidnova.network.Response;
import com.bidnova.utils.CloudinaryUploader;
import com.bidnova.utils.LocalDateTimeAdapter;
import com.bidnova.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AddProductController {
    @FXML private TextField txtName;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtStartingPrice;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private DatePicker dateStart;
    @FXML private TextField txtTimeStart;
    @FXML private DatePicker dateEnd;
    @FXML private TextField txtTimeEnd;
    @FXML private ImageView imgPreview;
    @FXML private Label lblImageStatus;
    @FXML private FontAwesomeIconView iconStatus;
    private Auction editingAuction = null;

    private Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    private String selectedImageUrl = null; // URL từ Cloudinary

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

        // Ban đầu không hiện icon
        iconStatus.setVisible(false);
        iconStatus.setManaged(false);
    }

    @FXML
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Hình ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        Stage stage = (Stage) txtName.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return;

        // Hiển thị preview local trước
        Image previewImage = new Image(file.toURI().toString());
        imgPreview.setImage(previewImage);
        lblImageStatus.setText("Đang upload lên Cloudinary...");
        iconStatus.setVisible(false); // Ẩn icon khi đang trong quá trình upload
        iconStatus.setManaged(false);

        // Upload lên Cloudinary trong background thread
        new Thread(() -> {
            String url = CloudinaryUploader.upload(file);
            if (url != null) {
                selectedImageUrl = url;
                Platform.runLater(() -> {
                    lblImageStatus.setText("Upload thành công");
                    lblImageStatus.setStyle("-fx-text-fill: #00703c; -fx-font-weight: bold;");
                    iconStatus.setGlyphName("CHECK");
                    iconStatus.setFill(Color.valueOf("#00703c"));
                    iconStatus.setVisible(true);
                    iconStatus.setManaged(true);
                });
            } else {
                Platform.runLater(() -> {
                    lblImageStatus.setText("Upload thất bại");
                    lblImageStatus.setStyle("-fx-text-fill: #b41712; -fx-font-weight: bold;");
                    iconStatus.setGlyphName("CLOSE");
                    iconStatus.setFill(Color.valueOf("#b41712"));
                    iconStatus.setVisible(true);
                    iconStatus.setManaged(true);
                });
            }
        }).start();
    }

    @FXML
    private void handleAddProduct() {
        try {
            if (txtName.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Bác nhập tên sản phẩm nhé!");
                return;
            }
            if (cmbCategory.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Bác chọn phân loại sản phẩm nhé!");
                return;
            }

            // Dùng Auction đang sửa (nếu có), không thì tạo mới
            Auction auction = (editingAuction != null) ? editingAuction : new Auction();
            auction.setProductName(txtName.getText());
            auction.setCategory(cmbCategory.getValue());
            auction.setDescription(txtDescription.getText());
            auction.setStartPrice(Double.parseDouble(txtStartingPrice.getText()));

            // Set startTime
            if (dateStart.getValue() != null) {
                LocalDate startDate = dateStart.getValue();
                String timeStr = txtTimeStart.getText().trim();
                LocalTime startTime = LocalTime.now();
                if (!timeStr.isEmpty()) {
                    try {
                        startTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                    } catch (Exception ignored) {}
                }
                auction.setStartTime(LocalDateTime.of(startDate, startTime));
            } else {
                auction.setStartTime(LocalDateTime.now());
            }

            // Set endTime
            if (dateEnd.getValue() != null) {
                LocalDate endDate = dateEnd.getValue();
                String timeStr = txtTimeEnd.getText().trim();
                LocalTime endTime = LocalTime.parse("23:59:59");
                if (!timeStr.isEmpty()) {
                    try {
                        endTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
                    } catch (Exception ignored) {}
                }
                auction.setEndTime(LocalDateTime.of(endDate, endTime));
            } else {
                auction.setEndTime(LocalDateTime.now().plusDays(7));
            }

            // Set image URL từ Cloudinary (nếu có)
            auction.setImageUrl(selectedImageUrl);

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
                            goBackToManage();
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi", "Server từ chối: " + (res != null ? res.getMessage() : ""));
                        }
                    });
                } catch (Exception e) { e.printStackTrace(); }
            }).start();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Bác check lại giá tiền (phải là số) nhé!");
            e.printStackTrace();
        }
    }

    @FXML
    private void goTo(String fxmlPath) {
        try {
            Stage stage = (Stage) txtName.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.out.println("Lỗi khi tải trang");
            e.printStackTrace();
        }
    }

    @FXML
    private void signOut() {
        SessionManager.logout();
        goTo("/views/auth/signin-view.fxml");
    }

    @FXML
    private void goBackToManage() {
        goTo("/views/seller/manage-product-view.fxml");
    }

    private void clearFields() {
        if (editingAuction != null) {
            // Nếu đang sửa, reset về dữ liệu ban đầu từ DB
            setAuctionToEdit(editingAuction);
        } else {
            // Nếu thêm mới, xóa trắng toàn bộ
            txtName.clear();
            txtDescription.clear();
            cmbCategory.getSelectionModel().clearSelection();
            txtStartingPrice.clear();
            txtTimeEnd.clear();
            txtTimeStart.clear();
            dateEnd.setValue(null);
            dateStart.setValue(null);
            selectedImageUrl = null;
            imgPreview.setImage(null);
            lblImageStatus.setText("Chưa chọn ảnh");
            lblImageStatus.setStyle("-fx-text-fill: #999");
            iconStatus.setVisible(false);
            iconStatus.setManaged(false);
        }
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

    /* Reset dữ liệu lấy từ database */
    public void setAuctionToEdit(Auction auction) {
        this.editingAuction = auction;
        
        // Đổ dữ liệu cũ vào các ô nhập liệu
        txtName.setText(auction.getProductName());
        txtDescription.setText(auction.getDescription());
        if (auction.getCategory() != null) {
            cmbCategory.setValue(auction.getCategory());
        }
        txtStartingPrice.setText(String.valueOf(auction.getStartPrice()));

        // Đổ ngày giờ bắt đầu
        if (auction.getStartTime() != null) {
            dateStart.setValue(auction.getStartTime().toLocalDate());
            txtTimeStart.setText(auction.getStartTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            dateStart.setValue(null);
            txtTimeStart.clear();
        }

        // Đổ ngày giờ kết thúc
        if (auction.getEndTime() != null) {
            dateEnd.setValue(auction.getEndTime().toLocalDate());
            txtTimeEnd.setText(auction.getEndTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            dateEnd.setValue(null);
            txtTimeEnd.clear();
        }

        // Đổ ảnh và trạng thái icon
        if (auction.getImageUrl() != null) {
            selectedImageUrl = auction.getImageUrl();
            Image previewImage = new Image(selectedImageUrl, true);
            imgPreview.setImage(previewImage);
            lblImageStatus.setText("Ảnh hiện tại");
            lblImageStatus.setStyle("-fx-text-fill: #00703c; -fx-font-weight: bold;");
            iconStatus.setGlyphName("CHECK");
            iconStatus.setFill(Color.valueOf("#00703c"));
            iconStatus.setVisible(true);
            iconStatus.setManaged(true);
        } else {
            selectedImageUrl = null;
            imgPreview.setImage(null);
            lblImageStatus.setText("Chưa chọn ảnh");
            lblImageStatus.setStyle("-fx-text-fill: #999");
            iconStatus.setVisible(false);
            iconStatus.setManaged(false);
        }
    }
}