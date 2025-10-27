package uniapp.model;

public enum Grade {
    Z,   // Fail / Withdrawn
    P,   // Pass
    C,   // Credit
    D,   // Distinction
    HD;  // High Distinction

    public static Grade fromMark(int mark) {
        if (mark < 50) return Z;
        if (mark < 65) return P;
        if (mark < 75) return C;
        if (mark < 85) return D;
        return HD;
    }
}


