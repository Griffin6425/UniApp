package uniapp.model;

import java.util.ArrayList;
import java.util.List;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Student {
    private StringProperty id = new SimpleStringProperty();           // 6-digit zero-padded
    private StringProperty name = new SimpleStringProperty();
    private StringProperty email = new SimpleStringProperty();        // must end with @university.com
    private String password;                                           // validated by rules (keep as String for security)
    private final ObservableList<EnrolledSubject> enrolments = FXCollections.observableArrayList();

    public Student(String id, String name, String email, String password) {
        this.id.set(id);
        this.name.set(name);
        this.email.set(email);
        this.password = password;
    }

    // Getters
    public final String getId() { return id.get(); }
    public final String getName() { return name.get(); }
    public final String getEmail() { return email.get(); }
    public String getPassword() { return password; }
    public ObservableList<EnrolledSubject> getEnrolments() { return enrolments; }

    // Setters
    public final void setId(String value) { this.id.set(value); }
    public final void setName(String value) { this.name.set(value); }
    public final void setEmail(String value) { this.email.set(value); }
    public void changePassword(String newPassword) { this.password = newPassword; }

    // Property getters (for JavaFX binding)
    public StringProperty idProperty() { return id; }
    public StringProperty nameProperty() { return name; }
    public StringProperty emailProperty() { return email; }

    // Enrolment methods
    public void addEnrolment(EnrolledSubject es) { enrolments.add(es); }
    public void removeEnrolmentIf(java.util.function.Predicate<EnrolledSubject> predicate) {
        enrolments.removeIf(predicate);
    }
}

