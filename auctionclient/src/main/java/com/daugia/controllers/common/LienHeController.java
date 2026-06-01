package com.daugia.controllers.common;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class LienHeController implements Initializable {

    @FXML
    private Label titleLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (titleLabel != null) {
            titleLabel.setText("LIÊN HỆ");
        }
    }
}
