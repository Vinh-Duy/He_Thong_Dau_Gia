package com.bidnova.controllers.components;

import java.time.LocalDateTime;

import com.bidnova.controllers.bidder.AuctionDetailController;
import com.bidnova.models.Product;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

// nhận vào một đối tượng kiểu Product và lấy các thông số của nó để set cho this: ảnh, tên, giá tiền
public class ProductCardController {
    @FXML
    private Product product;

    @FXML
    private VBox card;

    @FXML
    private Pane imgProduct;

    @FXML
    private Label lblName;

    @FXML
    private Label lblPrice;

    @FXML
    private Label btn;

    @FXML
    public void initialize() {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(100), card);

        card.setOnMouseEntered(e -> {
            scaleTransition.setToX(1.02); 
            scaleTransition.setToY(1.02); 
            scaleTransition.playFromStart();
        });

        card.setOnMouseExited(e -> {
            scaleTransition.setToX(1.0); 
            scaleTransition.setToY(1.0);
            scaleTransition.playFromStart();
        });
    }

    public void setData(Product product) {
        this.product = product;
        lblName.setText(product.getName());
        lblPrice.setText("Giá khởi điểm: " + String.format("%,d VNĐ", product.getStartingPrice()));
        imgProduct.setStyle("-fx-background-image: url('" + product.getImagePath() + "');");
    }

    private void alertMessage(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void goToAuctionDetail(Event event) {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(product.getStartTime())) {
            alertMessage("Thông báo", "Phiên đấu giá chưa diễn ra!");
            return;
        } else if (now.isAfter(product.getEndTime())) {
            alertMessage("Thông báo", "Phiên đấu giá đã kết thúc!");
            return;
        }

        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/bidder/auction-detail-view.fxml"));
            Parent root = loader.load();

            AuctionDetailController controller = loader.getController();
            controller.setData(product);

            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.out.println("Lỗi khi tải trang");
            e.printStackTrace();
        }
    }
}
