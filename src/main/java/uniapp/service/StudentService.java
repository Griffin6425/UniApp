package uniapp.service;

import uniapp.model.EnrolledSubject;
import uniapp.model.Student;
import uniapp.model.Subject;
import uniapp.repo.StudentRepository;
import uniapp.repo.SubjectRepository;
import uniapp.util.IdGenerator;
import uniapp.util.Validator;

import java.util.List;
import java.util.Random;

public class StudentService {
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final IdGenerator idGenerator;
    private final Random random = new Random();

    public StudentService(StudentRepository studentRepository, SubjectRepository subjectRepository, IdGenerator idGenerator) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.idGenerator = idGenerator;
    }

    public List<EnrolledSubject> viewEnrolment(String studentId) {
        Student s = requireStudent(studentId);
        return s.getEnrolments();
    }

    /**
     * Enrol subject by student ID (for CLI)
     */
    public void enrolSubject(String studentId, int subjectId) {
        Student s = requireStudent(studentId);
        if (s.getEnrolments().size() >= 4) throw new IllegalStateException("Cannot enrol more than 4 subjects");
        Subject subject = subjectRepository.findById(subjectId);
        if (subject == null) throw new IllegalArgumentException("Subject not found");
        boolean already = s.getEnrolments().stream().anyMatch(es -> es.getSubject().getId() == subjectId);
        if (already) throw new IllegalStateException("Already enrolled in this subject");
        int mark = 25 + random.nextInt(76); // 25..100
        EnrolledSubject es = new EnrolledSubject(idGenerator.nextEnrolmentId(), mark, subject);
        s.addEnrolment(es);
        studentRepository.upsert(s);
    }

    /**
     * Enrol subject using existing Student object (for GUI with ObservableList)
     */
    public void enrolSubject(Student student, int subjectId) {
        if (student.getEnrolments().size() >= 4) throw new IllegalStateException("Cannot enrol more than 4 subjects");
        Subject subject = subjectRepository.findById(subjectId);
        if (subject == null) throw new IllegalArgumentException("Subject not found");
        boolean already = student.getEnrolments().stream().anyMatch(es -> es.getSubject().getId() == subjectId);
        if (already) throw new IllegalStateException("Already enrolled in this subject");
        int mark = 25 + random.nextInt(76); // 25..100
        EnrolledSubject es = new EnrolledSubject(idGenerator.nextEnrolmentId(), mark, subject);
        student.addEnrolment(es);  // ← Manipulate the incoming object directly and the ObservableList will notify the UI!
        studentRepository.upsert(student);
    }

    public void removeSubject(String studentId, int subjectId) {
        Student s = requireStudent(studentId);
        s.removeEnrolmentIf(es -> es.getSubject().getId() == subjectId);
        studentRepository.upsert(s);
    }

    public void changePassword(String studentId, String currentPassword, String newPassword) {
        if (!Validator.isValidPassword(newPassword)) throw new IllegalArgumentException("Invalid password format");
        Student s = requireStudent(studentId);
        if (!s.getPassword().equals(currentPassword)) throw new IllegalArgumentException("Current password incorrect");
        s.changePassword(newPassword);
        studentRepository.upsert(s);
    }

    private Student requireStudent(String id) {
        Student s = studentRepository.findById(id);
        if (s == null) throw new IllegalArgumentException("Student not found");
        return s;
    }
}


