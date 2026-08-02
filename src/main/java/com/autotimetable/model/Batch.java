package com.autotimetable.model;

public class Batch {
    private String id;
    private String name;
    private int studentCount;

    public Batch(String id, String name, int studentCount) {
        this.id = id;
        this.name = name;
        this.studentCount = studentCount;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getStudentCount() { return studentCount; }
    public void setStudentCount(int studentCount) { this.studentCount = studentCount; }

    @Override
    public String toString() {
        return name + " (" + studentCount + " stds)";
    }
}
