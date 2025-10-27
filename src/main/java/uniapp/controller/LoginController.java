package uniapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import uniapp.fx.Controller;
import uniapp.fx.ViewLoader;
import uniapp.model.Student;
import uniapp.service.AdminAuthService;
import uniapp.service.AdminService;
import uniapp.service.AuthService;
import uniapp.service.StudentAuthService;
import uniapp.service.StudentService;
import uniapp.util.IdGenerator;
import uniapp.repo.StudentRepository;
import uniapp.repo.SubjectRepository;

/**
 * Login Controller - Handles user login and registration
 */
public class LoginController extends Controller<AuthService> {

    // StudentService - to pass to student dashboard
    private final StudentService studentService;
    private final SubjectRepository subjectRepository;
    private final AuthService authService;
    private final AdminAuthService adminAuthService;
    private final AdminService adminService;
    private final IdGenerator idGenerator;
    private final StudentRepository studentRepo;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    /**
     * Constructor - inject StudentService dependency
     */

public LoginController(StudentService studentService, SubjectRepository subjectRepository, 
                      AuthService authService, AdminAuthService adminAuthService,
                      AdminService adminService, StudentRepository studentRepo, 
                      IdGenerator idGenerator) {
        this.studentService = studentService;
        this.subjectRepository = subjectRepository;
        this.authService = authService;
        this.adminAuthService = adminAuthService;
        this.adminService = adminService;
        this.studentRepo = studentRepo;
        this.idGenerator = idGenerator;
    }

    /**
     * Handle login button click
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if(username.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter username and password", Alert.AlertType.ERROR);
            return;
        }

        try {
            //Try student login using AuthService
            Student student = model.login(username, password);

            if (student != null) {
                //Login successful, load student dashboard
                ViewLoader.showStage(student, "/uniapp/view/student_dashboard.fxml",
                                    "Student Dashboard - " + student.getName(), new Stage(),
                                    () -> new StudentDashboardController(studentService, subjectRepository, authService));
                stage.close(); //Close login window
            } else {
                showAlert("Error", "Invalid username or password", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Login failed: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }



    /**
     * Handle register button click
     */
    @FXML
    private void handleRegister() {
        try {
            // Open registration window
            ViewLoader.showStage(model, "/uniapp/view/register.fxml",
                            "Create Account", new javafx.stage.Stage(),
                            () -> new RegisterController());
            // Note: Don't close login window, user can login directly after registration
            
        } catch (Exception e) {
            showAlert("Error", "Failed to open registration: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Handle admin login
     */
    @FXML
    private void handleAdminLogin() {
        AdminLoginDialog dialog = new AdminLoginDialog(adminAuthService);
        dialog.showAndWait().ifPresent(success -> {
            if (success) {
                try {
                    ViewLoader.showStage(adminAuthService, "/uniapp/view/admin_dashboard.fxml",
                                    "Admin Dashboard", new Stage(),
                                    () -> new AdminDashboardController(adminService, studentRepo, subjectRepository, idGenerator));
                    stage.close();
                } catch (Exception e) {
                    showAlert("Error", "Failed to open admin dashboard: " + e.getMessage(), 
                            Alert.AlertType.ERROR);
                    e.printStackTrace();
    }
}
        });
    }
    
    /**
     * Show alert dialog
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
