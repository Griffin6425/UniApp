package uniapp.service;

import uniapp.model.Grade;
import uniapp.model.Student;
import uniapp.model.Subject;
import uniapp.repo.StudentRepository;
import uniapp.repo.SubjectRepository;

import java.util.*;
import java.util.stream.Collectors;

public class AdminService {
    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;

    public AdminService(StudentRepository studentRepository, SubjectRepository subjectRepository) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
    }

    public List<Student> viewAllStudents() { return studentRepository.findAll(); }

    public List<Student> viewStudentsByGrade(Grade grade) {
        return studentRepository.findAll().stream()
                .filter(s -> s.getEnrolments().stream().anyMatch(e -> e.getGrade() == grade))
                .collect(Collectors.toList());
    }

    public Map<String, List<Student>> categorizePassFail() {
        Map<String, List<Student>> result = new LinkedHashMap<>();
        result.put("PASS", new ArrayList<>());
        result.put("FAIL", new ArrayList<>());
        for (Student s : studentRepository.findAll()) {
            boolean pass = s.getEnrolments().stream().anyMatch(e -> e.getGrade() == Grade.P || e.getGrade() == Grade.C || e.getGrade() == Grade.D || e.getGrade() == Grade.HD);
            (pass ? result.get("PASS") : result.get("FAIL")).add(s);
        }
        return result;
    }

    public Map<Grade, List<Student>> groupByGrade() {
        Map<Grade, List<Student>> map = new EnumMap<>(Grade.class);
        for (Grade g : Grade.values()) map.put(g, new ArrayList<>());
        for (Student s : studentRepository.findAll()) {
            Set<Grade> grades = s.getEnrolments().stream().map(e -> e.getGrade()).collect(java.util.stream.Collectors.toSet());
            for (Grade g : grades) map.get(g).add(s);
        }
        return map;
    }

    public void removeStudent(String studentId) { studentRepository.deleteById(studentId); }

    public void clearAllStudents() { studentRepository.clearAll(); }

    public void addSubject(int id, String code, String title) {
        if (id < 1 || id > 999) throw new IllegalArgumentException("Subject id must be 1..999");
        if (code == null || code.trim().isEmpty()) throw new IllegalArgumentException("Code is required");
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Title is required");
        for (Subject s : subjectRepository.findAll()) {
            if (s.getId() == id) throw new IllegalArgumentException("Subject id already exists");
            if (s.getCode().equalsIgnoreCase(code)) throw new IllegalArgumentException("Subject code already exists");
        }
        subjectRepository.add(new Subject(id, code.trim(), title.trim()));
    }

    public void removeSubject(int subjectId) { subjectRepository.removeById(subjectId); }

    public List<Subject> listSubjects() { return subjectRepository.findAll(); }
}


