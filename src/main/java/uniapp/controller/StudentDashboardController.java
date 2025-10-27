package uniapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import uniapp.fx.Controller;
import uniapp.fx.ViewLoader;
import uniapp.model.EnrolledSubject;
import uniapp.model.Student;
import uniapp.service.AuthService;
import uniapp.service.StudentService;
import uniapp.repo.SubjectRepository;
import uniapp.repo.StudentRepository;

/**
 * Student Dashboard Controller - Main interface for students
 */
public class StudentDashboardController extends Controller<Student> {

    //StudentService - for enrollment and removal operations
    private final StudentService studentService;
    private final SubjectRepository subjectRepository;
    private final AuthService authService;

    @FXML private Label welcomeLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label emailLabel;
    @FXML private Label enrollmentCountLabel;
    @FXML private TableView<EnrolledSubject> enrollmentsTable;
    @FXML private TableColumn<EnrolledSubject, String> subjectCodeColumn;
    @FXML private TableColumn<EnrolledSubject, String> subjectTitleColumn;
    @FXML private TableColumn<EnrolledSubject, Integer> markColumn;
    @FXML private TableColumn<EnrolledSubject, String> gradeColumn;
    @FXML private Button enrollButton;
    @FXML private Button removeButton;
    @FXML private Button logoutButton;

    /**
     * Constructor - inject StudentService dependancy
     */
    
    public StudentDashboardController(StudentService studentService, SubjectRepository subjectRepository, AuthService authService) {
        this.studentService = studentService;
        this.subjectRepository = subjectRepository;
        this.authService = authService;
    }

    /**
     * Initialize the controller - called automatically after FXML loads
     */
    @FXML
    private void initialize() {
        //Setup table columns
        subjectCodeColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getSubject().getCode()));
        subjectTitleColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getSubject().getTitle()));
        markColumn.setCellValueFactory(new PropertyValueFactory<>("mark"));
        gradeColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(data.getValue().getGrade().toString()));
        
        //Bind data to UI after model is set
        javafx.application.Platform.runLater(() -> {
            if (model != null) {
                welcomeLabel.setText("Welcome, " + model.getName() + "!");
                studentIdLabel.setText("Student ID: " + model.getId());
                emailLabel.setText("Email: " + model.getEmail());
                enrollmentsTable.setItems(model.getEnrolments());
                updateEnrollmentCount();
            }
        });
    }

    /**
     * Update enrollment count label
     */
    private void updateEnrollmentCount() {
        int count = model.getEnrolments().size();
        enrollmentCountLabel.setText("Enrolled Subjects: " + count + " / 4");
        
        // Disable enroll button if already enrolled in 4 subjects
        enrollButton.setDisable(count >= 4);
    }

    /**
     * Handle Enroll Subject button click
     */
    @FXML
    private void handleEnrollSubject() {
        try {
            ViewLoader.showStage(model, "/uniapp/view/enroll_subject.fxml", 
                               "Enroll in Subject", new Stage(),
                               () -> new EnrollSubjectController(studentService, subjectRepository));
        } catch (Exception e) {
            showAlert("Error", "Failed to open enrollment window: " + e.getMessage(), 
                     Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Handle Remove Subject button click
     */
    @FXML
    private void handleRemoveSubject() {
        EnrolledSubject selected = enrollmentsTable.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert("Warning", "Please select a subject to remove", Alert.AlertType.WARNING);
            return;
        }
        // Confirm removal
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setHeaderText("Remove Subject");
        confirm.setContentText("Are you sure you want to remove: " + 
                              selected.getSubject().getCode() + " - " + 
                              selected.getSubject().getTitle() + "?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Call service to save to database
                    studentService.removeSubject(model.getId(), selected.getSubject().getId());
                                    
                    updateEnrollmentCount();
                    showAlert("Success", "Subject removed successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    //error handling: show user-friendly error message
                    showAlert("Error", "Failed to remove subject: " + e.getMessage(), 
                             Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Handle Logout button click
     */
    @FXML
    private void handleLogout() {
        stage.close();
    }

    /**
     * Handle Change Password button click
     */
    @FXML
    private void handleChangePassword() {
        ChangePasswordDialog dialog = new ChangePasswordDialog(authService, model);
        dialog.showAndWait();
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

