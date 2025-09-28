package uniapp.repo;

import uniapp.model.Subject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SubjectRepository {
    private final File storageFile;

    public SubjectRepository(File storageFile) {
        this.storageFile = storageFile;
    }

    public synchronized void add(Subject subject) {
        List<Subject> all = findAll();
        all.removeIf(s -> s.getId() == subject.getId());
        all.add(subject);
        writeAll(all);
    }

    public synchronized Subject findById(int id) {
        for (Subject s : findAll()) if (s.getId() == id) return s;
        return null;
    }

    public synchronized List<Subject> findAll() {
        if (!storageFile.exists()) return new ArrayList<>();
        List<Subject> result = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(storageFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                // Format: id|code|title
                String[] p = line.split("\\|", -1);
                if (p.length < 3) continue;
                result.add(new Subject(Integer.parseInt(p[0]), p[1], p[2]));
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return result;
    }

    public synchronized void removeById(int id) {
        List<Subject> all = findAll();
        all.removeIf(s -> s.getId() == id);
        writeAll(all);
    }

    public synchronized void clearAll() { writeAll(new ArrayList<>()); }

    private void writeAll(List<Subject> subjects) {
        try {
            if (!storageFile.getParentFile().exists()) storageFile.getParentFile().mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(storageFile, false), StandardCharsets.UTF_8))) {
                for (Subject s : subjects) {
                    bw.write(s.getId() + "|" + s.getCode() + "|" + s.getTitle());
                    bw.newLine();
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}


