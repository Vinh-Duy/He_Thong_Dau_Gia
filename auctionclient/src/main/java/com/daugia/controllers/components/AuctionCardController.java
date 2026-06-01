package com.daugia.controllers.components;

import com.daugia.controllers.bidder.ItemDetailController;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;    
import java.io.IOException;

import org.w3c.dom.Node;

public class AuctionCardController {
    @FXML private ImageView imgProduct;
    @FXML private Label titleLabel;
    @FXML private Label priceLabel;
    @FXML private Label timeLabel;
    @FXML private Button btnDetail;

    private String currentItemId;

    public void setData(String id, String title, String price, String time) {
        this.currentItemId = id;
        if (titleLabel != null) titleLabel.setText(title);
        if (priceLabel != null) priceLabel.setText(price);
        if (timeLabel != null) timeLabel.setText(time);
        
        String imagePath = "/images/Product.jpg"; 
        
        try {
            if (imgProduct != null) {
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                imgProduct.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh: " + imagePath);
        }
    }

    @FXML
    private void handleDetail() {
        System.out.println("\n--- BẮT ĐẦU CHUYỂN CẢNH ---");
        System.out.println("1. Click vào thẻ có ID: " + this.currentItemId);
        
        try {
            System.out.println("2. Đang nạp file FXML...");
            java.net.URL url = getClass().getResource("/views/bidder/ItemDetailView.fxml");
            
            // Chốt chặn 1: Kiểm tra xem có tìm thấy file không
            if (url == null) {
                System.out.println("❌ LỖI NGAY BƯỚC 2: Sai đường dẫn! Không tìm thấy file ItemDetailView.fxml");
                return; 
            }

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(url);
            javafx.scene.Parent homeRoot = loader.load();
            System.out.println("3. Nạp FXML thành công!");

            ItemDetailController detailController = loader.getController();
            
            System.out.println("4. Đang gọi hàm setAuctionId bên trang chi tiết...");
            // Chốt chặn 2: Rất hay bị NullPointerException ở dòng này
            detailController.setAuctionId(this.currentItemId); 
            System.out.println("5. Truyền ID thành công!");

            javafx.stage.Stage stage = (javafx.stage.Stage) btnDetail.getScene().getWindow();
            stage.getScene().setRoot(homeRoot);
            System.out.println("✅ 6. CHUYỂN CẢNH THÀNH CÔNG!");

        } catch (Exception e) { // BẮT BUỘC DÙNG Exception thay vì IOException
            System.out.println("❌ CODE BỊ CHẾT NGANG Ở ĐÂY:");
            e.printStackTrace();
        }
    }
    
}