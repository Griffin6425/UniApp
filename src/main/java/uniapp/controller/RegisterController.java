package uniapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import uniapp.fx.Controller;
import uniapp.model.Student;
import uniapp.service.AuthService;
import uniapp.util.Validator;


/**
 * Handles student registration with real-time validation
 */
public class RegisterController extends Controller<AuthService> {
    // FXML-injected UI components - Input fields
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    // FXML-injected UI components - Error labels
    @FXML private Label nameError;
    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;

    // FXML-injected UI components - Buttons
    @FXML private Button registerButton;

    // Validation state flags
    private boolean isNameValid = false;
    private boolean isEmailValid = false;
    private boolean isPasswordValid = false;
    private boolean isConfirmPasswordValid = false;
    
    /**
     * Called automatically after FXML is loaded
     */
    @FXML
    public void initialize() {
        // Setup real-time validation listeners
        setupValidationListeners();
        
        // Initially disable register button
        registerButton.setDisable(true);
    }
    
    /**
     * Setup real-time validation listeners for all fields
     */
    private void setupValidationListeners() {
        // Name field - validate on input
        nameField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateName();
        });
        
        // Email field - validate on input
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateEmail();
        });
        
        // Password field - validate on input
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validatePassword();
            // If confirm password is entered, re-validate match
            if (!confirmPasswordField.getText().isEmpty()) {
                validateConfirmPassword();
            }
        });
        
        // Confirm password field - validate on input
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateConfirmPassword();
        });
    }
    
    /**
     * Validate name field
     */
    private void validateName() {
        String name = nameField.getText().trim();
        
        if (name.isEmpty()) {
            showError(nameError, "Name is required");
            isNameValid = false;
        } else if (name.length() < 2) {
            showError(nameError, "Name must be at least 2 characters");
            isNameValid = false;
        } else {
            hideError(nameError);
            isNameValid = true;
        }
        
        updateRegisterButtonState();
    }
    
    /**
     * Validate email field
     */
    private void validateEmail() {
        String email = emailField.getText().trim();
        
        if (email.isEmpty()) {
            showError(emailError, "Email is required");
            isEmailValid = false;
        } else if (!Validator.isValidEmail(email)) {
            showError(emailError, "Email must end with @university.com");
            isEmailValid = false;
        } else {
            hideError(emailError);
            isEmailValid = true;
        }
        
        updateRegisterButtonState();
    }
    
    /**
     * Validate password field
     */
    private void validatePassword() {
        String password = passwordField.getText();
        
        if (password.isEmpty()) {
            showError(passwordError, "Password is required");
            isPasswordValid = false;
        } else if (!Validator.isValidPassword(password)) {
            showError(passwordError, "Must start with uppercase, 5+ letters, 3+ digits");
            isPasswordValid = false;
        } else {
            hideError(passwordError);
            isPasswordValid = true;
        }
        
        updateRegisterButtonState();
    }
    
    /**
     * Validate confirm password field
     */
    private void validateConfirmPassword() {
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();
        
        if (confirm.isEmpty()) {
            showError(confirmPasswordError, "Please confirm your password");
            isConfirmPasswordValid = false;
        } else if (!confirm.equals(password)) {
            showError(confirmPasswordError, "Passwords do not match");
            isConfirmPasswordValid = false;
        } else {
            hideError(confirmPasswordError);
            isConfirmPasswordValid = true;
        }
        
        updateRegisterButtonState();
    }
    
    /**
     * Update register button state based on validation
     */
    private void updateRegisterButtonState() {
        // Only enable button when all fields are valid
        boolean allValid = isNameValid && isEmailValid && 
                          isPasswordValid && isConfirmPasswordValid;
        registerButton.setDisable(!allValid);
    }
    
    /**
     * Show error message
     */
    private void showError(Label errorLabel, String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
    
    /**
     * Hide error message
     */
    private void hideError(Label errorLabel) {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
    
    /**
     * Handle register button click
     */
    @FXML
    private void handleRegister() {
        // Get form data
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        try {
            // Call AuthService to register
            Student student = model.register(name, email, password);
            
            // Show success message with student ID
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText("Welcome to UniApp!");
            alert.setContentText(
                "Your account has been created successfully!\n\n" +
                "Student ID: " + student.getId() + "\n" +
                "Name: " + student.getName() + "\n" +
                "Email: " + student.getEmail() + "\n\n" +
                "Please remember your Student ID for future login.\n" +
                "You can now login with your email and password."
            );
            alert.showAndWait();
            
            // Close register window and return to login
            stage.close();
            
        } catch (IllegalArgumentException e) {
            // Business errors (e.g., email already registered)
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Failed");
            alert.setHeaderText("Cannot create account");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            
            // If email already registered, highlight email field
            if (e.getMessage().contains("Email already registered")) {
                emailField.setStyle(
                    "-fx-border-color: red; -fx-border-width: 2; " +
                    "-fx-border-radius: 4; -fx-background-radius: 4;"
                );
                showError(emailError, "This email is already registered");
            }
            
        } catch (Exception e) {
            // Other errors
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Unexpected Error");
            alert.setContentText("An unexpected error occurred: " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }
    
    /**
     * Handle back to login button click
     */
    @FXML
    private void handleBackToLogin() {
        // Close register window
        stage.close();
    }
}
