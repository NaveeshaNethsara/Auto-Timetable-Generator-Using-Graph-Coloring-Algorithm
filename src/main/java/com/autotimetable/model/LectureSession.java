package com.autotimetable.model;

public class LectureSession implements Comparable<LectureSession> {
    private String sessionId;
    private Module module;
    private String assignedSlotId;
    private String assignedRoomId;
    private int conflictDegree;

    public LectureSession(String sessionId, Module module) {
        this.sessionId = sessionId;
        this.module = module;
        this.conflictDegree = 0;
    }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
    public Module getModule() { return module; }
    public void setModule(Module module) { this.module = module; }
    
    public String getAssignedSlotId() { return assignedSlotId; }
    public void setAssignedSlotId(String slotId) { this.assignedSlotId = slotId; }
    
    public String getAssignedRoomId() { return assignedRoomId; }
    public void setAssignedRoomId(String roomId) { this.assignedRoomId = roomId; }
    
    public int getConflictDegree() { return conflictDegree; }
    public void setConflictDegree(int degree) { this.conflictDegree = degree; }

    @Override
    public int compareTo(LectureSession other) {
        // Higher conflict degree comes first in PriorityQueue (Max-Heap)
        // If degrees are equal, sort by session ID alphabetically to guarantee stable ordering
        int degreeCompare = Integer.compare(other.conflictDegree, this.conflictDegree);
        if (degreeCompare != 0) {
            return degreeCompare;
        }
        return this.sessionId.compareTo(other.sessionId);
    }

    @Override
    public String toString() {
        return "Session: " + sessionId + " (Module: " + module.getCode() + ", Deg: " + conflictDegree + ")";
    }
}
