package uniapp.model;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private String id;                // 6-digit zero-padded
    private String name;
    private String email;             // must end with @university.com
    private String password;          // validated by rules
    private final List<EnrolledSubject> enrolments = new ArrayList<>();

    public Student(String id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public List<EnrolledSubject> getEnrolments() { return enrolments; }

    public void changePassword(String newPassword) { this.password = newPassword; }

    public void addEnrolment(EnrolledSubject es) { enrolments.add(es); }
    public void removeEnrolmentIf(java.util.function.Predicate<EnrolledSubject> predicate) {
        enrolments.removeIf(predicate);
    }
}


