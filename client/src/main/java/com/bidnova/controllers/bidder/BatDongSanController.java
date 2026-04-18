package com.bidnova.controllers.bidder;

import java.io.IOException;
import java.util.List;

import com.bidnova.controllers.components.ProductCardController;
import com.bidnova.models.Product;
import com.bidnova.utils.ProductLoader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;

public class BatDongSanController {
    @FXML
    private FlowPane productContainer;

    @FXML
    public void initialize() {
        displayProducts();
    }

    private void displayProducts() {
        List<Product> products = ProductLoader.loadProducts();

        for (Product product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/components/product-card.fxml"));
                Parent card = loader.load();

                // gọi hàm setData để fill dữ liệu vào card
                ProductCardController controller = loader.getController();
                controller.setData(product);

                // thêm card vào cùng hiển thị trên màn hình
                productContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
