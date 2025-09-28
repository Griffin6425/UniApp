package uniapp.model;

public class EnrolledSubject {
    private int id;        // 1..999 unique enrolment id
    private int mark;      // 25..100 randomly assigned
    private Grade grade;   // derived from mark
    private Subject subject;

    public EnrolledSubject(int id, int mark, Subject subject) {
        this.id = id;
        this.mark = mark;
        this.grade = Grade.fromMark(mark);
        this.subject = subject;
    }

    public int getId() { return id; }
    public int getMark() { return mark; }
    public Grade getGrade() { return grade; }
    public Subject getSubject() { return subject; }

    public void setMark(int mark) {
        this.mark = mark;
        this.grade = Grade.fromMark(mark);
    }

    public Grade computeGrade() {
        this.grade = Grade.fromMark(this.mark);
        return this.grade;
    }
}


