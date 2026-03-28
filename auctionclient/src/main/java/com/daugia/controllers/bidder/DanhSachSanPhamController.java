package com.daugia.controllers.bidder;

import java.io.IOException;

import com.daugia.controllers.components.AuctionCardController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class DanhSachSanPhamController {

    @FXML private Label lblTitle;
    @FXML private FlowPane productContainer;

    private String currentCategory;

    // Hàm nhận dữ liệu từ Menu truyền sang
    public void setCategory(String categoryName) {
        this.currentCategory = categoryName;
        lblTitle.setText("Danh mục: " + categoryName);
        
        loadProducts();
    }

    private void loadProducts() {
        productContainer.getChildren().clear(); // Xóa thẻ cũ đi (nếu có)

        // TODO: Lấy List<Item> từ Database/Server dựa theo biến currentCategory
        // Tạm thời mình dùng vòng lặp giả lập (Mock data) tạo ra 10 sản phẩm
        
        for (int i = 1; i <= 10; i++) {
            try {
                // 1. Tải giao diện của 1 cái Thẻ
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/AuctionCard.fxml"));
                Node cardView = loader.load();

                // 2. Lấy Controller của thẻ đó
                AuctionCardController cardController = loader.getController();

                // 3. Đổ dữ liệu giả vào thẻ (Sau này thay bằng dữ liệu thật từ List<Item>)
                cardController.setData(
                    currentCategory + " - SP " + i, 
                    "5,000,000", 
                    "02:00:00", 
                    "/images/UET-logo.png" // Dùng tạm logo UET của bạn làm ảnh SP
                );

                // 4. Nhét thẻ vào FlowPane
                productContainer.getChildren().add(cardView);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}