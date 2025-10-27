package uniapp;

import uniapp.model.EnrolledSubject;
import uniapp.model.Student;
import uniapp.model.Subject;
import uniapp.repo.StudentRepository;
import uniapp.repo.SubjectRepository;
import uniapp.service.AdminService;
import uniapp.service.AuthService;
import uniapp.service.AdminAuthService;
import uniapp.service.StudentService;
import uniapp.service.StudentAuthService;
import uniapp.util.IdGenerator;
import uniapp.util.Validator;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class CLIUniApp {
    public static void main(String[] args) {
        File base = new File("uniapp/data");
        StudentRepository sRepo = new StudentRepository(new File(base, "students.data"));
        SubjectRepository subjRepo = new SubjectRepository(new File(base, "subjects.data"));
        IdGenerator ids = new IdGenerator(0, 0, 0);
        AuthService auth = new AuthService(sRepo, ids);
        StudentAuthService studentAuth = new StudentAuthService(new File(base, "student_auth.data"));
        auth.setStudentAuthService(studentAuth);
        AdminAuthService adminAuth = new AdminAuthService(new File(base, "admin.data"));
        StudentService studentService = new StudentService(sRepo, subjRepo, ids);
        AdminService adminService = new AdminService(sRepo, subjRepo);

        // Seed default subjects if empty
        if (subjRepo.findAll().isEmpty()) {
            subjRepo.add(new Subject(101, "CS101", "Intro to CS"));
            subjRepo.add(new Subject(102, "CS102", "Data Structures"));
            subjRepo.add(new Subject(201, "CS201", "OOP"));
            subjRepo.add(new Subject(202, "CS202", "Databases"));
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("Welcome to CLIUniApp");
        loop:
        while (true) {
            System.out.println("1) Register  2) Login  3) Forgot/Reset Password  0) Exit");
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        String name;
                        while (true) {
                            System.out.print("Name: ");
                            name = sc.nextLine().trim();
                            if (name.isEmpty()) { System.out.println("Error: Name is required"); continue; }
                            break;
                        }
                        String email;
                        while (true) {
                            System.out.print("Email (@university.com): ");
                            email = sc.nextLine().trim();
                            if (!Validator.isValidEmail(email)) { System.out.println("Error: Invalid email (must end with @university.com)"); continue; }
                            break;
                        }
                        String pw;
                        while (true) {
                            System.out.print("Password: ");
                            pw = sc.nextLine().trim();
                            if (!Validator.isValidPassword(pw)) { System.out.println("Error: Password must start with uppercase, 5+ letters then 3+ digits"); continue; }
                            break;
                        }
                        while (true) {
                            try {
                                Student s = auth.register(name, email, pw);
                                System.out.println("Registered with ID: " + s.getId());
                                break;
                            } catch (Exception ex) {
                                String msg = ex.getMessage() == null ? "" : ex.getMessage();
                                if (msg.toLowerCase().contains("email")) {
                                    System.out.println("Error: " + msg);
                                    // re-enter only email until valid & unique
                                    while (true) {
                                        System.out.print("Email (@university.com): ");
                                        email = sc.nextLine().trim();
                                        if (!Validator.isValidEmail(email)) { System.out.println("Error: Invalid email (must end with @university.com)"); continue; }
                                        break;
                                    }
                                    continue; // retry register with new email
                                } else {
                                    System.out.println("Error: " + msg);
                                    break;
                                }
                            }
                        }
                        break;
                    case "2":
                        // Unified login: try admin first, then student
                        System.out.print("Username: ");
                        String identifier = sc.nextLine().trim();
                        System.out.print("Password: ");
                        String lpw = sc.nextLine().trim();
                        // Try admin
                        boolean handled = false;
                        if (!identifier.contains("@")) { // likely admin or student id
                            if (adminAuth.isLocked()) {
                                System.out.println("Admin account locked. Try later.");
                            } else if (adminAuth.login(identifier, lpw)) {
                                adminMenu(sc, adminService, adminAuth);
                                handled = true;
                            } else {
                                // not admin, continue to try student
                                if (identifier.equalsIgnoreCase("admin")) {
                                    System.out.println("Invalid admin credentials. Remaining attempts: " + adminAuth.remainingAttempts());
                                }
                            }
                        }
                        if (!handled) {
                            try {
                                if (auth != null && identifier != null && auth != null) {
                                    if (auth != null) {
                                        Student me = auth.login(identifier, lpw);
                                        System.out.println("Welcome, " + me.getName() + " (#" + me.getId() + ")");
                                        studentMenu(sc, me, studentService);
                                    }
                                }
                            } catch (Exception ex) {
                                // If student lockout service exists, show remaining attempts
                                System.out.println("Error: " + ex.getMessage());
                                if (identifier != null) {
                                    try {
                                        System.out.println("Remaining attempts: " + auth != null ? "" : "");
                                    } catch (Exception ignore) {}
                                }
                            }
                        }
                        break;
                    case "3":
                        // Forgot/Reset Password
                        System.out.println("1) Send Reset Code  2) Reset Password  0) Back");
                        String sub2 = sc.nextLine().trim();
                        if ("1".equals(sub2)) {
                            System.out.print("Email (@university.com): ");
                            String e1 = sc.nextLine().trim();
                            try {
                                String code = auth.issueResetCode(e1);
                                System.out.println("Reset code (simulated email): " + code);
                            } catch (Exception ex) {
                                System.out.println("Error: " + ex.getMessage());
                            }
                        } else if ("2".equals(sub2)) {
                            System.out.print("Email: ");
                            String e2 = sc.nextLine().trim();
                            System.out.print("Code: ");
                            String code = sc.nextLine().trim();
                            System.out.print("New Password: ");
                            String npass = sc.nextLine().trim();
                            try {
                                auth.resetPassword(e2, code, npass);
                                System.out.println("Password reset successful.");
                            } catch (Exception ex) {
                                System.out.println("Error: " + ex.getMessage());
                            }
                        }
                        break;
                    case "4":
                        System.out.println("1) Send Reset Code  2) Reset Password  0) Back");
                        String sub = sc.nextLine().trim();
                        if ("1".equals(sub)) {
                            System.out.print("Email (@university.com): ");
                            String e1 = sc.nextLine().trim();
                            try {
                                String code = auth.issueResetCode(e1);
                                System.out.println("Reset code (simulated email): " + code);
                            } catch (Exception ex) {
                                System.out.println("Error: " + ex.getMessage());
                            }
                        } else if ("2".equals(sub)) {
                            System.out.print("Email: ");
                            String e2 = sc.nextLine().trim();
                            System.out.print("Code: ");
                            String code = sc.nextLine().trim();
                            System.out.print("New Password: ");
                            String npass = sc.nextLine().trim();
                            try {
                                auth.resetPassword(e2, code, npass);
                                System.out.println("Password reset successful.");
                            } catch (Exception ex) {
                                System.out.println("Error: " + ex.getMessage());
                            }
                        }
                        break;
                    case "0":
                        break loop;
                    default:
                        System.out.println("Unknown option");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
        System.out.println("Bye.");
    }

    private static void studentMenu(Scanner sc, Student me, StudentService studentService) {
        while (true) {
            System.out.println("Student Menu: 1) View 2) Enrol 3) Remove 4) Change PW 0) Back");
            String c = sc.nextLine().trim();
            try {
                if ("1".equals(c)) {
                    List<EnrolledSubject> list = studentService.viewEnrolment(me.getId());
                    for (EnrolledSubject es : list) {
                        Subject subj = es.getSubject();
                        System.out.println("[" + es.getId() + "] " + subj.getCode() + " " + subj.getTitle() + " mark=" + es.getMark() + " grade=" + es.getGrade());
                    }
                } else if ("2".equals(c)) {
                    System.out.print("Subject ID: ");
                    String sraw = sc.nextLine().trim();
                    Integer sid = tryParseInt(sraw);
                    if (sid == null) { System.out.println("Invalid number"); continue; }
                    studentService.enrolSubject(me.getId(), sid);
                    System.out.println("Enrolled.");
                } else if ("3".equals(c)) {
                    System.out.print("Subject ID to remove: ");
                    String sraw = sc.nextLine().trim();
                    Integer sid = tryParseInt(sraw);
                    if (sid == null) { System.out.println("Invalid number"); continue; }
                    System.out.print("Confirm remove subject " + sid + "? (y/N): ");
                    String confirm = sc.nextLine().trim().toLowerCase();
                    if (!"y".equals(confirm)) { System.out.println("Cancelled."); continue; }
                    studentService.removeSubject(me.getId(), sid);
                    System.out.println("Removed.");
                } else if ("4".equals(c)) {
                    System.out.print("Current Password: ");
                    String cpw = sc.nextLine().trim();
                    System.out.print("New Password: ");
                    String npw = sc.nextLine().trim();
                    System.out.print("Confirm New Password: ");
                    String npw2 = sc.nextLine().trim();
                    if (!npw.equals(npw2)) { System.out.println("Error: Passwords do not match"); continue; }
                    studentService.changePassword(me.getId(), cpw, npw);
                    System.out.println("Password changed.");
                } else if ("0".equals(c)) {
                    return;
                } else {
                    System.out.println("Unknown");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void adminMenu(Scanner sc, AdminService adminService, AdminAuthService adminAuth) {
        while (true) {
            System.out.println("Admin Menu: 1) List students 2) Remove student 3) Clear students 4) List subjects 5) Add subject 6) Remove subject 7) Group by Grade 8) PASS/FAIL 9) Export CSV 10) Change Admin Password 0) Back");
            String c = sc.nextLine().trim();
            try {
                switch (c) {
                    case "1":
                        for (Student s : adminService.viewAllStudents()) {
                            System.out.println(s.getId() + " | " + s.getName() + " | " + s.getEmail());
                        }
                        break;
                    case "2":
                        System.out.print("Student ID: ");
                        String sid = sc.nextLine().trim();
                        System.out.print("Confirm remove student " + sid + "? (y/N): ");
                        String conf = sc.nextLine().trim().toLowerCase();
                        if (!"y".equals(conf)) { System.out.println("Cancelled."); break; }
                        adminService.removeStudent(sid);
                        System.out.println("Removed.");
                        break;
                    case "3":
                        System.out.print("Confirm CLEAR ALL students? (type YES): ");
                        String cc = sc.nextLine().trim();
                        if (!"YES".equals(cc)) { System.out.println("Cancelled."); break; }
                        adminService.clearAllStudents();
                        System.out.println("Cleared all students.");
                        break;
                    case "4":
                        for (Subject s : adminService.listSubjects()) {
                            System.out.println(s.getId() + " | " + s.getCode() + " | " + s.getTitle());
                        }
                        break;
                    case "5":
                        System.out.print("Subject ID (1..999): ");
                        Integer id = tryParseInt(sc.nextLine().trim());
                        if (id == null) { System.out.println("Invalid number"); break; }
                        System.out.print("Code: ");
                        String code = sc.nextLine().trim();
                        System.out.print("Title: ");
                        String title = sc.nextLine().trim();
                        adminService.addSubject(id, code, title);
                        System.out.println("Added.");
                        break;
                    case "6":
                        System.out.print("Subject ID: ");
                        Integer rid = tryParseInt(sc.nextLine().trim());
                        if (rid == null) { System.out.println("Invalid number"); break; }
                        System.out.print("Confirm remove subject " + rid + "? (y/N): ");
                        String cs = sc.nextLine().trim().toLowerCase();
                        if (!"y".equals(cs)) { System.out.println("Cancelled."); break; }
                        adminService.removeSubject(rid);
                        System.out.println("Removed.");
                        break;
                    case "7":
                        java.util.Map<uniapp.model.Grade, java.util.List<uniapp.model.Student>> gmap = adminService.groupByGrade();
                        for (uniapp.model.Grade g : uniapp.model.Grade.values()) {
                            System.out.println("== " + g + " ==");
                            for (Student s : gmap.get(g)) {
                                System.out.println("  " + s.getId() + " | " + s.getName());
                            }
                        }
                        break;
                    case "8":
                        java.util.Map<String, java.util.List<Student>> pf = adminService.categorizePassFail();
                        System.out.println("== PASS ==");
                        for (Student s : pf.get("PASS")) System.out.println("  " + s.getId() + " | " + s.getName());
                        System.out.println("== FAIL ==");
                        for (Student s : pf.get("FAIL")) System.out.println("  " + s.getId() + " | " + s.getName());
                        break;
                    case "9":
                        exportCsv(adminService);
                        break;
                    case "10":
                        System.out.print("Current Password: ");
                        String cpw = sc.nextLine().trim();
                        System.out.print("New Password: ");
                        String npw = sc.nextLine().trim();
                        System.out.print("Confirm New Password: ");
                        String npw2 = sc.nextLine().trim();
                        if (!npw.equals(npw2)) { System.out.println("Error: Passwords do not match"); break; }
                        adminAuth.changePassword(cpw, npw);
                        System.out.println("Admin password changed.");
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Unknown");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }

    private static void exportCsv(AdminService adminService) {
        java.io.File outDir = new java.io.File("uniapp/exports");
        if (!outDir.exists()) outDir.mkdirs();
        java.io.File studentsCsv = new java.io.File(outDir, "students.csv");
        try (java.io.PrintWriter pw = new java.io.PrintWriter(studentsCsv, java.nio.charset.StandardCharsets.UTF_8)) {
            pw.println("studentId,name,email,numEnrolments");
            for (Student s : adminService.viewAllStudents()) {
                pw.println(s.getId() + "," + s.getName() + "," + s.getEmail() + "," + s.getEnrolments().size());
            }
            System.out.println("Exported: " + studentsCsv.getAbsolutePath());
        } catch (Exception ex) {
            System.out.println("Export failed: " + ex.getMessage());
        }
    }

    private static Integer tryParseInt(String raw) {
        try {
            return Integer.parseInt(raw);
        } catch (Exception ex) {
            return null;
        }
    }
}


