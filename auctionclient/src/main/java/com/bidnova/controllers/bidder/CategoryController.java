package com.bidnova.controllers.bidder;

import java.io.IOException;
import java.util.List;

import com.bidnova.controllers.components.ProductCardController;
import com.bidnova.models.Product;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;

public class CategoryController {
    @FXML
    private Label lblCategoryTitle;

    @FXML
    private FlowPane productContainer;

    public void resetCategoryData(String titleName, List<Product> products) {
        // set tiêu đề
        lblCategoryTitle.setText(titleName);

        // xóa hết các card cũ
        productContainer.getChildren().clear();

        // thêm product-card vào productContainer
        for (Product product : products) {
            // kiểm tra nếu nó cùng loại với title
            if (product.getCategory().equals(titleName)) {
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
}
