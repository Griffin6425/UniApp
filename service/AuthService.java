package uniapp.service;

import uniapp.model.Student;
import uniapp.repo.StudentRepository;
import uniapp.util.IdGenerator;
import uniapp.util.Validator;

public class AuthService {
    private final StudentRepository studentRepository;
    private final IdGenerator idGenerator;
    private StudentAuthService studentAuthService;

    public AuthService(StudentRepository studentRepository, IdGenerator idGenerator) {
        this.studentRepository = studentRepository;
        this.idGenerator = idGenerator;
    }

    public void setStudentAuthService(StudentAuthService studentAuthService) {
        this.studentAuthService = studentAuthService;
    }

    public Student register(String name, String email, String password) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("Name is required");
        if (!Validator.isValidEmail(email)) throw new IllegalArgumentException("Invalid email (must end with @university.com)");
        if (!Validator.isValidPassword(password)) throw new IllegalArgumentException("Invalid password format");
        if (studentRepository.findByEmail(email) != null) throw new IllegalArgumentException("Email already registered");
        String id = idGenerator.nextStudentId();
        Student s = new Student(id, name, email, password);
        studentRepository.save(s);
        return s;
    }

    public Student login(String identifier, String password) {
        Student s = identifier.contains("@") ? studentRepository.findByEmail(identifier) : studentRepository.findById(identifier);
        if (s == null) throw new IllegalArgumentException("User not found");
        if (studentAuthService != null) {
            if (studentAuthService.isLocked(identifier)) throw new IllegalArgumentException("Account locked. Try later.");
            boolean ok = s.getPassword().equals(password);
            studentAuthService.noteLoginResult(identifier, ok);
            if (!ok) throw new IllegalArgumentException("Invalid password");
            return s;
        } else {
            if (!s.getPassword().equals(password)) throw new IllegalArgumentException("Invalid password");
            return s;
        }
    }

    public String issueResetCode(String email) {
        if (studentAuthService == null) throw new IllegalStateException("Reset not configured");
        Student s = studentRepository.findByEmail(email);
        if (s == null) throw new IllegalArgumentException("Email not found");
        return studentAuthService.issueResetCode(email);
    }

    public void resetPassword(String email, String code, String newPassword) {
        if (studentAuthService == null) throw new IllegalStateException("Reset not configured");
        if (!uniapp.util.Validator.isValidPassword(newPassword)) throw new IllegalArgumentException("Invalid password format");
        Student s = studentRepository.findByEmail(email);
        if (s == null) throw new IllegalArgumentException("Email not found");
        if (!studentAuthService.verifyAndConsumeResetCode(email, code)) throw new IllegalArgumentException("Invalid or expired code");
        s.changePassword(newPassword);
        studentRepository.upsert(s);
    }
}


