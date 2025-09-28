package uniapp.repo;

import uniapp.model.EnrolledSubject;
import uniapp.model.Student;
import uniapp.model.Subject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StudentRepository {
    private final File storageFile;

    public StudentRepository(File storageFile) {
        this.storageFile = storageFile;
    }

    public synchronized void upsert(Student student) {
        Map<String, Student> all = indexById(findAll());
        all.put(student.getId(), deepCopy(student));
        writeAll(new ArrayList<>(all.values()));
    }

    public synchronized void save(Student student) { upsert(student); }

    public synchronized Student findByEmail(String email) {
        for (Student s : findAll()) {
            if (s.getEmail().equalsIgnoreCase(email)) return s;
        }
        return null;
    }

    public synchronized Student findById(String id) {
        for (Student s : findAll()) {
            if (s.getId().equals(id)) return s;
        }
        return null;
    }

    public synchronized List<Student> findAll() {
        if (!storageFile.exists()) return new ArrayList<>();
        List<Student> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(storageFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Format: id|name|email|password|enrolmentCount|[enrolmentId,mark,subjectId,code,title];...
                String[] parts = line.split("\\|", -1);
                if (parts.length < 5) continue;
                String id = parts[0];
                String name = parts[1];
                String email = parts[2];
                String password = parts[3];
                int enrolmentCount = Integer.parseInt(parts[4]);
                Student s = new Student(id, name, email, password);
                if (parts.length >= 6 && enrolmentCount > 0) {
                    String[] enrolments = parts[5].split(";", -1);
                    for (String e : enrolments) {
                        if (e.isEmpty()) continue;
                        String[] ep = e.split(",", -1);
                        if (ep.length < 5) continue;
                        int enrolId = Integer.parseInt(ep[0]);
                        int mark = Integer.parseInt(ep[1]);
                        int subjectId = Integer.parseInt(ep[2]);
                        String code = ep[3];
                        String title = ep[4];
                        Subject subj = new Subject(subjectId, code, title);
                        uniapp.model.EnrolledSubject es = new uniapp.model.EnrolledSubject(enrolId, mark, subj);
                        s.addEnrolment(es);
                    }
                }
                result.add(s);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return result;
    }

    public synchronized void deleteById(String id) {
        List<Student> all = findAll();
        all.removeIf(s -> s.getId().equals(id));
        writeAll(all);
    }

    public synchronized void clearAll() {
        writeAll(new ArrayList<>());
    }

    private Map<String, Student> indexById(List<Student> list) {
        Map<String, Student> map = new LinkedHashMap<>();
        for (Student s : list) map.put(s.getId(), s);
        return map;
    }

    private void writeAll(List<Student> students) {
        try {
            if (!storageFile.getParentFile().exists()) storageFile.getParentFile().mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(storageFile, false), StandardCharsets.UTF_8))) {
                for (Student s : students) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(s.getId()).append('|')
                      .append(s.getName()).append('|')
                      .append(s.getEmail()).append('|')
                      .append(s.getPassword()).append('|');
                    List<EnrolledSubject> es = s.getEnrolments();
                    sb.append(es.size()).append('|');
                    List<String> entries = new ArrayList<>();
                    for (EnrolledSubject e : es) {
                        Subject subj = e.getSubject();
                        entries.add(e.getId() + "," + e.getMark() + "," + subj.getId() + "," + subj.getCode() + "," + subj.getTitle());
                    }
                    sb.append(String.join(";", entries));
                    bw.write(sb.toString());
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private Student deepCopy(Student s) {
        Student copy = new Student(s.getId(), s.getName(), s.getEmail(), s.getPassword());
        for (EnrolledSubject e : s.getEnrolments()) {
            Subject subj = e.getSubject();
            copy.addEnrolment(new EnrolledSubject(e.getId(), e.getMark(), new Subject(subj.getId(), subj.getCode(), subj.getTitle())));
        }
        return copy;
    }
}


