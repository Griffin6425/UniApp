package uniapp.util;

import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\n\r]+@university\\.com$");
    // Password: starts with upper-case letter, contains at least 5 letters followed by 3+ digits
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[A-Z][A-Za-z]{4,}[0-9]{3,}$");

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }
}


