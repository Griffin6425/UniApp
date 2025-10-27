package uniapp.service;

import java.io.*;
import java.nio.charset.StandardCharsets;

import uniapp.util.Validator;

public class AdminAuthService {
    private final File storageFile;
    private String username;
    private String password;
    private int failedAttempts;
    private long lockUntilMs;

    private final int maxAttempts = 5;
    private final long lockDurationMs = 5 * 60 * 1000L; // 5 minutes

    public AdminAuthService(File storageFile) {
        this.storageFile = storageFile;
        loadOrInit();
    }

    private void loadOrInit() {
        if (!storageFile.exists()) {
            this.username = "admin";
            this.password = "admin123";
            this.failedAttempts = 0;
            this.lockUntilMs = 0L;
            save();
            return;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(storageFile), StandardCharsets.UTF_8))) {
            String line = br.readLine();
            if (line == null || line.trim().isEmpty()) {
                this.username = "admin";
                this.password = "admin123";
                this.failedAttempts = 0;
                this.lockUntilMs = 0L;
                save();
            } else {
                String[] p = line.split(",", -1);
                this.username = p.length > 0 ? p[0] : "admin";
                this.password = p.length > 1 ? p[1] : "admin123";
                this.failedAttempts = p.length > 2 ? safeParseInt(p[2], 0) : 0;
                this.lockUntilMs = p.length > 3 ? safeParseLong(p[3], 0L) : 0L;
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void save() {
        try {
            if (!storageFile.getParentFile().exists()) storageFile.getParentFile().mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(storageFile, false), StandardCharsets.UTF_8))) {
                bw.write(username + "," + password + "," + failedAttempts + "," + lockUntilMs);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public boolean login(String username, String password) {
        long now = System.currentTimeMillis();
        if (now < lockUntilMs) {
            return false;
        }
        boolean ok = this.username.equals(username) && this.password.equals(password);
        if (ok) {
            failedAttempts = 0;
            lockUntilMs = 0L;
            save();
            return true;
        } else {
            failedAttempts++;
            if (failedAttempts >= maxAttempts) {
                lockUntilMs = now + lockDurationMs;
                failedAttempts = 0; // reset counter after locking
            }
            save();
            return false;
        }
    }

    public void changePassword(String currentPassword, String newPassword) {
        if (!this.password.equals(currentPassword)) throw new IllegalArgumentException("Current password incorrect");
        if (!Validator.isValidPassword(newPassword)) throw new IllegalArgumentException("Invalid password format");
        this.password = newPassword;
        save();
    }

    public boolean isLocked() {
        return System.currentTimeMillis() < lockUntilMs;
    }

    public int remainingAttempts() {
        if (isLocked()) return 0;
        return Math.max(0, maxAttempts - failedAttempts);
    }

    public long lockedUntilEpochMs() { return lockUntilMs; }

    private int safeParseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception ex) { return def; }
    }
    private long safeParseLong(String s, long def) {
        try { return Long.parseLong(s); } catch (Exception ex) { return def; }
    }
}


