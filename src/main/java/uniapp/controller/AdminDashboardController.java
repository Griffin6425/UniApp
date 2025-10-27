package uniapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import uniapp.fx.Controller;
import uniapp.model.Grade;
import uniapp.model.Student;
import uniapp.model.Subject;
import uniapp.repo.StudentRepository;
import uniapp.repo.SubjectRepository;
import uniapp.service.AdminService;
import uniapp.util.IdGenerator;

import java.util.List;
import java.util.Map;

/**
 * Admin Dashboard Controller - Main interface for administrators
 */
public class AdminDashboardController extends Controller<Object> {

    private final AdminService adminService;
    private final StudentRepository studentRepo;
    private final SubjectRepository subjectRepo;
    private final IdGenerator idGenerator;

    @FXML private Label welcomeLabel;

    // Students Tab
    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, String> studentIdColumn;
    @FXML private TableColumn<Student, String> studentNameColumn;
    @FXML private TableColumn<Student, String> studentEmailColumn;
    @FXML private TableColumn<Student, Integer> enrollmentCountColumn;

    // Subjects Tab
    @FXML private TableView<Subject> subjectsTable;
    @FXML private TableColumn<Subject, Integer> subjectIdColumn;
    @FXML private TableColumn<Subject, String> subjectCodeColumn;
    @FXML private TableColumn<Subject, String> subjectTitleColumn;

    // Statistics Tab
    @FXML private TextArea statisticsArea;

    private ObservableList<Student> studentsList;
    private ObservableList<Subject> subjectsList;

    /**
     * Constructor - inject dependencies
     */
    public AdminDashboardController(AdminService adminService, StudentRepository studentRepo,
                                   SubjectRepository subjectRepo, IdGenerator idGenerator) {
        this.adminService = adminService;
        this.studentRepo = studentRepo;
        this.subjectRepo = subjectRepo;
        this.idGenerator = idGenerator;
    }

    /**
     * Initialize the controller - called automatically after FXML loads
     */
    @FXML
    private void initialize() {
        // Setup Students table columns
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        studentEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        enrollmentCountColumn.setCellValueFactory(data ->
            new javafx.beans.property.SimpleIntegerProperty(
                data.getValue().getEnrolments().size()).asObject());

        // Setup Subjects table columns
        subjectIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        subjectCodeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
        subjectTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        // Load data
        javafx.application.Platform.runLater(() -> {
            loadStudents();
            loadSubjects();
            refreshStatistics();
        });
    }

    /**
     * Load all students into the table
     */
    private void loadStudents() {
        List<Student> students = adminService.viewAllStudents();
        studentsList = FXCollections.observableArrayList(students);
        studentsTable.setItems(studentsList);
    }

    /**
     * Load all subjects into the table
     */
    private void loadSubjects() {
        List<Subject> subjects = adminService.listSubjects();
        subjectsList = FXCollections.observableArrayList(subjects);
        subjectsTable.setItems(subjectsList);
    }

    /**
     * Handle Remove Student button click
     */
    @FXML
    private void handleRemoveStudent() {
        Student selected = studentsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Warning", "Please select a student to remove", Alert.AlertType.WARNING);
            return;
        }

        // Confirm removal
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setHeaderText("Remove Student");
        confirm.setContentText("Are you sure you want to remove student: " +
                              selected.getName() + " (ID: " + selected.getId() + ")?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    adminService.removeStudent(selected.getId());
                    loadStudents(); // Refresh the table
                    showAlert("Success", "Student removed successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Failed to remove student: " + e.getMessage(),
                             Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Handle Add Subject button click
     */
    @FXML
    private void handleAddSubject() {
        // Create a custom dialog for adding subject
        Dialog<Subject> dialog = new Dialog<>();
        dialog.setTitle("Add New Subject");
        dialog.setHeaderText("Enter subject details");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the form fields
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField idField = new TextField();
        idField.setPromptText("Subject ID (1-999)");
        TextField codeField = new TextField();
        codeField.setPromptText("Subject Code");
        TextField titleField = new TextField();
        titleField.setPromptText("Subject Title");

        grid.add(new Label("ID:"), 0, 0);
        grid.add(idField, 1, 0);
        grid.add(new Label("Code:"), 0, 1);
        grid.add(codeField, 1, 1);
        grid.add(new Label("Title:"), 0, 2);
        grid.add(titleField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the ID field by default
        javafx.application.Platform.runLater(() -> idField.requestFocus());

        // Convert the result when the Add button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    int id = Integer.parseInt(idField.getText());
                    String code = codeField.getText();
                    String title = titleField.getText();
                    return new Subject(id, code, title);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(subject -> {
            if (subject != null) {
                try {
                    adminService.addSubject(subject.getId(), subject.getCode(), subject.getTitle());
                    loadSubjects(); // Refresh the table
                    showAlert("Success", "Subject added successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Failed to add subject: " + e.getMessage(),
                             Alert.AlertType.ERROR);
                }
            } else {
                showAlert("Error", "Invalid subject ID. Please enter a valid number.",
                         Alert.AlertType.ERROR);
            }
        });
    }

    /**
     * Handle Remove Subject button click
     */
    @FXML
    private void handleRemoveSubject() {
        Subject selected = subjectsTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("Warning", "Please select a subject to remove", Alert.AlertType.WARNING);
            return;
        }

        // Confirm removal
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Removal");
        confirm.setHeaderText("Remove Subject");
        confirm.setContentText("Are you sure you want to remove subject: " +
                              selected.getCode() + " - " + selected.getTitle() + "?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    adminService.removeSubject(selected.getId());
                    loadSubjects(); // Refresh the table
                    showAlert("Success", "Subject removed successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Failed to remove subject: " + e.getMessage(),
                             Alert.AlertType.ERROR);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Handle Refresh Statistics button click
     */
    @FXML
    private void handleRefreshStatistics() {
        refreshStatistics();
        showAlert("Success", "Statistics refreshed", Alert.AlertType.INFORMATION);
    }

    /**
     * Refresh the statistics display
     */
    private void refreshStatistics() {
        StringBuilder stats = new StringBuilder();
        stats.append("=== STUDENT STATISTICS ===\n\n");

        // Total students
        List<Student> allStudents = adminService.viewAllStudents();
        stats.append("Total Students: ").append(allStudents.size()).append("\n\n");

        // Pass/Fail categorization
        stats.append("--- Pass/Fail Distribution ---\n");
        Map<String, List<Student>> passFail = adminService.categorizePassFail();
        stats.append("PASS: ").append(passFail.get("PASS").size()).append(" students\n");
        stats.append("FAIL: ").append(passFail.get("FAIL").size()).append(" students\n\n");

        // Grade distribution
        stats.append("--- Grade Distribution ---\n");
        Map<Grade, List<Student>> gradeDistribution = adminService.groupByGrade();
        for (Grade grade : Grade.values()) {
            List<Student> students = gradeDistribution.get(grade);
            stats.append(String.format("%-4s: %d students\n", grade, students.size()));
        }

        stats.append("\n=== SUBJECT STATISTICS ===\n\n");
        List<Subject> allSubjects = adminService.listSubjects();
        stats.append("Total Subjects: ").append(allSubjects.size()).append("\n");

        statisticsArea.setText(stats.toString());
    }

    /**
     * Handle Logout button click
     */
    @FXML
    private void handleLogout() {
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
