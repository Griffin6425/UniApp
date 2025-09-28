package uniapp.model;

public class Subject {
    private int id;         // 1..999
    private String code;    // e.g., CS101
    private String title;   // subject name

    public Subject(int id, String code, String title) {
        this.id = id;
        this.code = code;
        this.title = title;
    }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getTitle() { return title; }

    public void setId(int id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setTitle(String title) { this.title = title; }

    @Override
    public String toString() {
        return code + " - " + title + " (#" + id + ")";
    }
}


