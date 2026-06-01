package com.bidnova.utils;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

public class NotificationUtil {
    private static final Duration ANIMATION_DURATION = Duration.millis(400);
    private static final Duration DISPLAY_DURATION = Duration.seconds(4);

    public static void showTopBarNotification(Window owner, String title, String message) {
        if (owner == null || !owner.isShowing()) {
            return;
        }

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");

        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(420);

        VBox container = new VBox(6, titleLabel, messageLabel);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(14));
        container.setMinWidth(420);
        container.setMaxWidth(420);
        container.setBackground(new Background(new BackgroundFill(Color.web("#2c3e50"), new CornerRadii(10), Insets.EMPTY)));
        container.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.25), 10, 0, 0, 3);");
        container.setOpacity(0);
        container.setTranslateY(-30);

        Popup popup = new Popup();
        popup.getContent().add(container);
        popup.setAutoFix(true);
        popup.setAutoHide(true);

        double x = owner.getX() + (owner.getWidth() - 440) / 2;
        double y = owner.getY() + 10;
        popup.show(owner, x, y);

        FadeTransition fadeIn = new FadeTransition(ANIMATION_DURATION, container);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(ANIMATION_DURATION, container);
        slideIn.setFromY(-30);
        slideIn.setToY(0);

        PauseTransition stay = new PauseTransition(DISPLAY_DURATION);

        FadeTransition fadeOut = new FadeTransition(ANIMATION_DURATION, container);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(ANIMATION_DURATION, container);
        slideOut.setFromY(0);
        slideOut.setToY(-30);

        SequentialTransition seq = new SequentialTransition(fadeIn, slideIn, stay, fadeOut, slideOut);
        seq.setOnFinished(evt -> popup.hide());
        seq.play();
    }

    public static void showTopBanner(String title, String message) {
        Window owner = Window.getWindows().stream()
            .filter(Window::isShowing)
            .findFirst()
            .orElse(null);
        showTopBarNotification(owner, title, message);
    }
}
