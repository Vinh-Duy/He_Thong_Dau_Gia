package com.bidnova.controllers.components;

import com.bidnova.utils.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class UserPopupController {
    @FXML private VBox root;
    @FXML private Label popupUsername;
    @FXML private Label popupRole;
    @FXML private Label popupEmail;
    @FXML private Label popupFullName;
    @FXML private Label popupPhone;
    @FXML private Label popupGender;

    @FXML
    public void initialize() {
        // Không cần setVisible/Managed ở đây nữa vì đã có Popup stage quản lý
    }

    public void updateData() {
        popupUsername.setText(SessionManager.getUsername());
        popupRole.setText(SessionManager.getRole() != null ? SessionManager.getRole() : "Thành viên");
        popupEmail.setText(SessionManager.getEmail() != null ? SessionManager.getEmail() : "Chưa cập nhật");
        popupFullName.setText(SessionManager.getFullName() != null ? SessionManager.getFullName() : "Chưa cập nhật");
        popupPhone.setText(SessionManager.getPhone() != null ? SessionManager.getPhone() : "Chưa cập nhật");
        popupGender.setText(SessionManager.getGender() != null ? SessionManager.getGender() : "Chưa cập nhật");
    }
}