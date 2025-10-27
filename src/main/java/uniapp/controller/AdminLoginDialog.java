package uniapp.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import uniapp.service.AdminAuthService;

public class AdminLoginDialog extends Dialog<Boolean> {
    private final AdminAuthService adminAuthService;

    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Label errorLabel;

    public AdminLoginDialog(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;

        setTitle("Admin Login");
        setHeaderText("Administrator Authentication");

        // Create input fields
        usernameField = new TextField();
        usernameField.setPromptText("Admin username");
        usernameField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        passwordField = new PasswordField();
        passwordField.setPromptText("Admin password");
        passwordField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        // Create error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #DC3545; -fx-font-size: 12px;");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        // Create layout
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        content.getChildren().addAll(
            new Label("Username:"),
            usernameField,
            new Label("Password:"),
            passwordField,
            errorLabel
        );

        getDialogPane().setContent(content);

        // Add buttons
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Handle result
        setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return performLogin();
            }
            return false;
        });
    }

    private boolean performLogin() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);

        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return false;
        }

        try {
            boolean success = adminAuthService.login(username, password);
            if (success) {
                return true;
            } else {
                showError("Invalid admin credentials");
                return false;
            }
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            return false;
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
