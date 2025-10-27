package uniapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import uniapp.fx.Controller;
import uniapp.model.EnrolledSubject;
import uniapp.model.Student;
import uniapp.model.Subject;
import uniapp.service.StudentService;
import uniapp.repo.SubjectRepository;
import uniapp.repo.StudentRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Enroll Subject Controller - Handle subject enrollment
 */
public class EnrollSubjectController extends Controller<Student> {
    

    // StudentService - for enrollment operations and getting subject list
    private final StudentService studentService;
    private final SubjectRepository subjectRepository;
    
    @FXML private ListView<Subject> subjectListView;  // Changed to subject type
    @FXML private Button enrollButton;
    @FXML private Button cancelButton;
    @FXML private Label infoLabel;  // Optional: display a reminder message

    /**
     * Constructor - inject StudentService dependency
     */
    public EnrollSubjectController(StudentService studentService, SubjectRepository subjectRepository) {
        this.studentService = studentService;
        this.subjectRepository = subjectRepository;
    }

    /**
     * Initialize the controller - called automatically after FXML loads
     */
    @FXML
    private void initialize() {
        // Set custom cell factory for ListView
        subjectListView.setCellFactory(param -> new ListCell<Subject>() {
            @Override
            protected void updateItem(Subject subject, boolean empty) {
                super.updateItem(subject, empty);
                
                if (empty || subject == null) {
                    setText(null);
                } else {
                    // Custom display format: CS101 - Intro to CS
                    setText(subject.getCode() + " - " + subject.getTitle());
                }
            }
        });
        
        // Wait for model to be set before loading data
        javafx.application.Platform.runLater(() -> {
            if (model != null) {
                loadAvailableSubjects();
            }
        });
    }
    
    /**
     * Load available subjects (exclude already enrolled subjects)
     */
    private void loadAvailableSubjects() {
        try { 
            // 1. Get list of subject IDs already enrolled by student
            List<Integer> enrolledSubjectIds = model.getEnrolments().stream()
                .map(es -> es.getSubject().getId())
                .collect(Collectors.toList());
            
            // 2. Load all subjects from database
            List<Subject> allSubjects = subjectRepository.findAll();
            
            // 3. Filter out already enrolled subjects
            List<Subject> availableSubjects = allSubjects.stream()
                .filter(subject -> !enrolledSubjectIds.contains(subject.getId()))
                .collect(Collectors.toList());
            
            // 4. Display in ListView
            subjectListView.getItems().addAll(availableSubjects);
            
            // 6. Check if already enrolled in 4 subjects
            if (model.getEnrolments().size() >= 4) {
                enrollButton.setDisable(true);
                if (infoLabel != null) {
                    infoLabel.setText("You have already enrolled in 4 subjects (maximum)");
                    infoLabel.setStyle("-fx-text-fill: red;");
                }
            } else if (availableSubjects.isEmpty()) {
                enrollButton.setDisable(true);
                if (infoLabel != null) {
                    infoLabel.setText("No available subjects to enroll");
                }
            }
            
        } catch (Exception e) {
            showAlert("Error", "Failed to load subjects: " + e.getMessage(), 
                     Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Handle Enroll button click
     */
    @FXML
    private void handleEnroll() {
        // 1. Get selected subject
        Subject selected = subjectListView.getSelectionModel().getSelectedItem();
        
        if (selected == null) {
            showAlert("Warning", "Please select a subject to enroll", 
                     Alert.AlertType.WARNING);
            return;
        }
        
        // 2. Check if already enrolled in 4 subjects (double check)
        if (model.getEnrolments().size() >= 4) {
            showAlert("Error", "You have already enrolled in 4 subjects (maximum)", 
                     Alert.AlertType.ERROR);
            return;
        }
        
        // 3. Call StudentService to enroll
        try {
            studentService.enrolSubject(model, selected.getId());
            
            // 4. Show success message (with randomly generated mark)
            EnrolledSubject newEnrolment = model.getEnrolments().stream()
                .filter(es -> es.getSubject().getId() == selected.getId())
                .findFirst()
                .orElse(null);
            
            String message = "Successfully enrolled in: " + selected.getCode() + " - " + selected.getTitle();
            if (newEnrolment != null) {
                message += "\nMark: " + newEnrolment.getMark();
                message += "\nGrade: " + newEnrolment.getGrade();
            }
            
            showAlert("Success", message, Alert.AlertType.INFORMATION);
            
            // 
            // Close window (return to student dashboard)
            stage.close();
            
            // Note: Student dashboard will auto-refresh because of ObservableList
            
        } catch (IllegalStateException e) {
            // Business logic errors (already enrolled, max 4 subjects, etc.)
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
            
        } catch (Exception e) {
            // Other errors
            showAlert("Error", "Failed to enroll: " + e.getMessage(), 
                     Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Handle Cancel button click
     */
    @FXML
    private void handleCancel() {
        stage.close();
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
