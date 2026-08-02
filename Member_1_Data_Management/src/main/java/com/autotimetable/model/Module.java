package com.autotimetable.model;

public class Module {
    private String id;
    private String code;
    private String name;
    private String lecturerId;
    private String batchId;
    private int requiredSessions;

    public Module(String id, String code, String name, String lecturerId, String batchId, int requiredSessions) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.lecturerId = lecturerId;
        this.batchId = batchId;
        this.requiredSessions = requiredSessions;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getLecturerId() { return lecturerId; }
    public void setLecturerId(String lecturerId) { this.lecturerId = lecturerId; }
    
    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    
    public int getRequiredSessions() { return requiredSessions; }
    public void setRequiredSessions(int requiredSessions) { this.requiredSessions = requiredSessions; }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
