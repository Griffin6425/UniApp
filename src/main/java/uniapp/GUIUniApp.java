package uniapp;

import javafx.application.Application;
import javafx.stage.Stage;
import uniapp.fx.ViewLoader;
import uniapp.model.Student;
import uniapp.repo.StudentRepository;
import uniapp.repo.SubjectRepository;
import uniapp.service.AdminAuthService;
import uniapp.service.AuthService;
import uniapp.service.StudentAuthService;
import uniapp.service.StudentService;
import uniapp.util.IdGenerator;
import uniapp.service.AdminService;

import java.io.File;

/**
 * GUI UniApp - JavaFX version of the University Application
 */
public class GUIUniApp extends Application {
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize repositories and services (same as CLIUniApp)
        File base = new File("uniapp/data");
        StudentRepository sRepo = new StudentRepository(new File(base, "students.data"));
        SubjectRepository subjRepo = new SubjectRepository(new File(base, "subjects.data"));
        IdGenerator ids = new IdGenerator(0, 0, 0);
        AuthService auth = new AuthService(sRepo, ids);
        StudentAuthService studentAuth = new StudentAuthService(new File(base, "student_auth.data"));
        auth.setStudentAuthService(studentAuth);
        AdminAuthService adminAuth = new AdminAuthService(new File(base, "admin.data"));

        //Create StudentService for student enrollment operations
        StudentService studentService = new StudentService(sRepo, subjRepo, ids);
        //Create AdminService for admin operations
        AdminService adminService = new AdminService(sRepo, subjRepo);

        // Seed default subjects if empty
        if (subjRepo.findAll().isEmpty()) {
            subjRepo.add(new uniapp.model.Subject(101, "CS101", "Intro to CS"));
            subjRepo.add(new uniapp.model.Subject(102, "CS102", "Data Structures"));
            subjRepo.add(new uniapp.model.Subject(201, "CS201", "OOP"));
            subjRepo.add(new uniapp.model.Subject(202, "CS202", "Databases"));
        }

        // Create a test student for demo purposes
        // In production, this would be done through the login screen
        Student testStudent = new Student("000001", "John Doe", "john@university.com", "Password123");
        testStudent.addEnrolment(new uniapp.model.EnrolledSubject(1, 85, subjRepo.findById(101)));
        testStudent.addEnrolment(new uniapp.model.EnrolledSubject(2, 92, subjRepo.findById(102)));
        sRepo.upsert(testStudent);

        // Show login window, pass auth and studentService
        ViewLoader.showStage(auth, "/uniapp/view/login.fxml", "UniApp - Login", primaryStage,
        () -> new uniapp.controller.LoginController(studentService, subjRepo, auth, adminAuth, 
                                           adminService, sRepo, ids));
    }
}