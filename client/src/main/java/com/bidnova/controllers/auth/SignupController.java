package com.bidnova.controllers.auth;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
// import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
// import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class SignupController {
    // @FXML
    // private RadioButton personalRadio, orgRadio;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Label ruleLength, ruleUpper, ruleLower, ruleNumber, ruleSpecial;

    @FXML
    private ComboBox<String> sexBox;

    private void updateRule(Label label, boolean ok) {
        if (ok) {
            label.setText("✔ " + label.getText().substring(2));
            label.setStyle("-fx-text-fill: green;");
        } else {
            label.setText("✖ " + label.getText().substring(2));
            label.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    public void initialize() {
        // ToggleGroup group = new ToggleGroup();
        // personalRadio.setToggleGroup(group);
        // orgRadio.setToggleGroup(group);

        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateRule(ruleLength, newVal.length() >= 8);
            updateRule(ruleUpper, newVal.matches(".*[A-Z].*"));
            updateRule(ruleLower, newVal.matches(".*[a-z].*"));
            updateRule(ruleNumber, newVal.matches(".*\\d.*"));
            updateRule(ruleSpecial, newVal.matches(".*[!@#$%^&*()].*"));
        });

        sexBox.getItems().addAll("Nam", "Nữ", "Chọn giới tính");
    }

    @FXML
    private void goTo(Event event, String fxmlPath) {
        try {
            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            System.out.println("Lỗi khi tải trang");
            e.printStackTrace();
        }
    }

    @FXML
    private void goToSignin(MouseEvent event) {
        goTo(event, "/views/auth/signin-view.fxml");
    }

    @FXML
    private void togglePassword() {
        if (passwordField.isVisible()) {

            passwordTextField.setText(passwordField.getText());
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);

            passwordField.setVisible(false);
            passwordField.setManaged(false);
        } else {

            passwordField.setText(passwordTextField.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);

            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
        }
    }
}
