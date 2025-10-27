package uniapp.controller;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import uniapp.model.Student;
import uniapp.service.AuthService;

public class ChangePasswordDialog extends Dialog<Boolean> {
    private final AuthService authService;
    private final Student student;

    private final PasswordField currentPasswordField;
    private final PasswordField newPasswordField;
    private final PasswordField confirmPasswordField;

    private final Label currentPasswordError;
    private final Label newPasswordError;
    private final Label confirmPasswordError;

    public ChangePasswordDialog(AuthService authService, Student student) {
        this.authService = authService;
        this.student = student;

        setTitle("Change Password");
        setHeaderText("Change your account password");

        // Create input fields
        currentPasswordField = new PasswordField();
        currentPasswordField.setPromptText("Current password");
        currentPasswordField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New password");
        newPasswordField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm new password");
        confirmPasswordField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");

        // Create error labels
        currentPasswordError = new Label();
        currentPasswordError.setStyle("-fx-text-fill: #DC3545; -fx-font-size: 12px;");
        currentPasswordError.setVisible(false);
        currentPasswordError.setManaged(false);

        newPasswordError = new Label();
        newPasswordError.setStyle("-fx-text-fill: #DC3545; -fx-font-size: 12px;");
        newPasswordError.setVisible(false);
        newPasswordError.setManaged(false);

        confirmPasswordError = new Label();
        confirmPasswordError.setStyle("-fx-text-fill: #DC3545; -fx-font-size: 12px;");
        confirmPasswordError.setVisible(false);
        confirmPasswordError.setManaged(false);

        // Create layout
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        content.getChildren().addAll(
            new Label("Current Password:"),
            currentPasswordField,
            currentPasswordError,
            new Label("New Password:"),
            newPasswordField,
            newPasswordError,
            new Label("Confirm New Password:"),
            confirmPasswordField,
            confirmPasswordError,
            new Label("Password must start with uppercase letter, contain 5+ letters and 3+ digits")
        );

        getDialogPane().setContent(content);

        // Add buttons
        ButtonType changeButtonType = new ButtonType("Change Password", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);

        // Disable change button initially
        Button changeButton = (Button) getDialogPane().lookupButton(changeButtonType);
        changeButton.setDisable(true);

        // Setup validation
        setupValidation(changeButton);

        // Handle result
        setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                return performPasswordChange();
            }
            return false;
        });
    }

    private void setupValidation(Button changeButton) {
        // Enable button only when all fields have content
        currentPasswordField.textProperty().addListener((obs, old, newVal) ->
            updateButtonState(changeButton));
        newPasswordField.textProperty().addListener((obs, old, newVal) ->
            updateButtonState(changeButton));
        confirmPasswordField.textProperty().addListener((obs, old, newVal) ->
            updateButtonState(changeButton));
    }

    private void updateButtonState(Button button) {
        boolean allFilled = !currentPasswordField.getText().isEmpty() &&
                           !newPasswordField.getText().isEmpty() &&
                           !confirmPasswordField.getText().isEmpty();
        button.setDisable(!allFilled);
    }

    private boolean performPasswordChange() {
        // Clear previous errors
        hideError(currentPasswordError);
        hideError(newPasswordError);
        hideError(confirmPasswordError);

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            showError(confirmPasswordError, "Passwords do not match");
            return false;
        }

        // Attempt password change
        try {
            authService.changePassword(student.getEmail(), currentPassword, newPassword);

            // Show success message
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Password Changed");
            alert.setContentText("Your password has been changed successfully.");
            alert.showAndWait();

            return true;

        } catch (IllegalArgumentException e) {
            // Handle validation errors
            if (e.getMessage().contains("Current password")) {
                showError(currentPasswordError, e.getMessage());
            } else if (e.getMessage().contains("password format")) {
                showError(newPasswordError, "Must start with uppercase, 5+ letters, 3+ digits");
            } else {
                showError(newPasswordError, e.getMessage());
            }
            return false;
        }
    }

    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }   
}
