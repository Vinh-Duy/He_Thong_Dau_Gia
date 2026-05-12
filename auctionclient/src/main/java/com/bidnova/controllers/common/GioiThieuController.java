package com.bidnova.controllers.common;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;

public class GioiThieuController {
    @FXML
    private VBox content;

    @FXML
    public void initialize() {
        animateCards();
    }

    // khi mời vào trang giới thiệu thì chạy animation cho các card
    private void animateCards() {
        for (int i = 0; i < content.getChildren().size(); i++) {
            Node child = (Node) content.getChildren().get(i);

            if (child instanceof VBox) {
                VBox card = (VBox) child;

                FadeTransition fade = new FadeTransition(Duration.millis(500), card);
                fade.setFromValue(0);
                fade.setToValue(1);

                TranslateTransition slide = new TranslateTransition(Duration.millis(500), card);
                slide.setFromY(30);
                slide.setToY(0);

                ParallelTransition parallel = new ParallelTransition(fade, slide);
                parallel.setDelay(Duration.millis(i * 150)); // Delay mỗi card
                parallel.play();
            }
        }
    }
}
