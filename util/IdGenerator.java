package uniapp.util;

import java.util.concurrent.atomic.AtomicInteger;

public class IdGenerator {
    private final AtomicInteger studentSeq;
    private final AtomicInteger subjectSeq;
    private final AtomicInteger enrolmentSeq;

    public IdGenerator(int studentStart, int subjectStart, int enrolmentStart) {
        this.studentSeq = new AtomicInteger(studentStart);
        this.subjectSeq = new AtomicInteger(subjectStart);
        this.enrolmentSeq = new AtomicInteger(enrolmentStart);
    }

    public String nextStudentId() {
        int next = studentSeq.updateAndGet(v -> v >= 999999 ? 1 : v + 1);
        return String.format("%06d", next);
    }

    public int nextSubjectId() {
        return subjectSeq.updateAndGet(v -> v >= 999 ? 1 : v + 1);
    }

    public int nextEnrolmentId() {
        return enrolmentSeq.updateAndGet(v -> v >= 999 ? 1 : v + 1);
    }
}


