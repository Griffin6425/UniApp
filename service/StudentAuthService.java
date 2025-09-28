package uniapp.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StudentAuthService {
    private final File storageFile;
    private final int maxAttempts = 5;
    private final long lockDurationMs = 5 * 60 * 1000L; // 5 minutes

    public StudentAuthService(File storageFile) {
        this.storageFile = storageFile;
        if (!storageFile.getParentFile().exists()) storageFile.getParentFile().mkdirs();
    }

    public boolean isLocked(String studentIdOrEmail) {
        Record r = read().getOrDefault(studentIdOrEmail.toLowerCase(), new Record());
        return System.currentTimeMillis() < r.lockUntilMs;
    }

    public int remainingAttempts(String studentIdOrEmail) {
        Record r = read().getOrDefault(studentIdOrEmail.toLowerCase(), new Record());
        if (System.currentTimeMillis() < r.lockUntilMs) return 0;
        return Math.max(0, maxAttempts - r.failedAttempts);
    }

    public void noteLoginResult(String studentIdOrEmail, boolean success) {
        Map<String, Record> map = read();
        String key = studentIdOrEmail.toLowerCase();
        Record r = map.getOrDefault(key, new Record());
        long now = System.currentTimeMillis();
        if (now < r.lockUntilMs) {
            // still locked, keep as is
        } else if (success) {
            r.failedAttempts = 0;
            r.lockUntilMs = 0L;
        } else {
            r.failedAttempts++;
            if (r.failedAttempts >= maxAttempts) {
                r.lockUntilMs = now + lockDurationMs;
                r.failedAttempts = 0;
            }
        }
        map.put(key, r);
        write(map);
    }

    public String issueResetCode(String email) {
        Map<String, Record> map = read();
        String key = email.toLowerCase();
        Record r = map.getOrDefault(key, new Record());
        r.resetCode = String.valueOf(100000 + new java.util.Random().nextInt(900000));
        r.resetCodeExpireMs = System.currentTimeMillis() + 10 * 60 * 1000L; // 10 minutes
        map.put(key, r);
        write(map);
        return r.resetCode;
    }

    public boolean verifyAndConsumeResetCode(String email, String code) {
        Map<String, Record> map = read();
        String key = email.toLowerCase();
        Record r = map.getOrDefault(key, new Record());
        long now = System.currentTimeMillis();
        if (r.resetCode != null && r.resetCode.equals(code) && now <= r.resetCodeExpireMs) {
            r.resetCode = null;
            r.resetCodeExpireMs = 0L;
            write(map);
            return true;
        }
        return false;
    }

    private Map<String, Record> read() {
        Map<String, Record> map = new LinkedHashMap<>();
        if (!storageFile.exists()) return map;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(storageFile), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|", -1);
                if (p.length < 4) continue;
                Record r = new Record();
                r.failedAttempts = safeParseInt(p[1], 0);
                r.lockUntilMs = safeParseLong(p[2], 0L);
                r.resetCode = p[3].isEmpty() ? null : p[3];
                r.resetCodeExpireMs = p.length > 4 ? safeParseLong(p[4], 0L) : 0L;
                map.put(p[0].toLowerCase(), r);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return map;
    }

    private void write(Map<String, Record> map) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(storageFile, false), StandardCharsets.UTF_8))) {
            for (Map.Entry<String, Record> e : map.entrySet()) {
                Record r = e.getValue();
                bw.write(e.getKey() + "|" + r.failedAttempts + "|" + r.lockUntilMs + "|" + (r.resetCode == null ? "" : r.resetCode) + "|" + r.resetCodeExpireMs);
                bw.newLine();
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private int safeParseInt(String s, int def) { try { return Integer.parseInt(s); } catch (Exception ex) { return def; } }
    private long safeParseLong(String s, long def) { try { return Long.parseLong(s); } catch (Exception ex) { return def; } }

    private static class Record {
        int failedAttempts;
        long lockUntilMs;
        String resetCode;
        long resetCodeExpireMs;
    }
}


