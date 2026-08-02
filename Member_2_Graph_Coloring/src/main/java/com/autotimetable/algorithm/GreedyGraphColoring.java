package com.autotimetable.algorithm;

import com.autotimetable.datastructure.ConflictGraph;
import com.autotimetable.model.LectureSession;
import com.autotimetable.model.Lecturer;
import com.autotimetable.model.TimeSlot;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class GreedyGraphColoring {

    /**
     * Colors the conflict graph (assigns timeslots to sessions).
     * Assumes conflictDegrees have already been calculated and set on sessions.
     */
    public void colorGraph(List<LectureSession> sessions, ConflictGraph graph, 
                           List<TimeSlot> systemSlots, Map<String, Lecturer> lecturers) {
        
        // 1. Put all sessions into a PriorityQueue to sort them by conflict degree descending (Max-Heap)
        PriorityQueue<LectureSession> queue = new PriorityQueue<>(sessions);
        
        // Map to quickly find LectureSession objects by ID during coloring
        java.util.Map<String, LectureSession> sessionMap = new java.util.HashMap<>();
        for (LectureSession s : sessions) {
            sessionMap.put(s.getSessionId(), s);
            s.setAssignedSlotId(null); // Reset any previous assignment
        }

        // 2. Process sessions in descending order of conflict degree
        while (!queue.isEmpty()) {
            LectureSession currentSession = queue.poll();
            String currentLecturerId = currentSession.getModule().getLecturerId();
            Lecturer lecturer = lecturers.get(currentLecturerId);

            // Find slot assignments of all neighboring sessions
            Set<String> neighborOccupiedSlots = new HashSet<>();
            for (String neighborId : graph.getNeighbors(currentSession.getSessionId())) {
                LectureSession neighborSession = sessionMap.get(neighborId);
                if (neighborSession != null && neighborSession.getAssignedSlotId() != null) {
                    neighborOccupiedSlots.add(neighborSession.getAssignedSlotId());
                }
            }

            // Find the first available system slot that matches constraints:
            // - The lecturer is available in this slot
            // - No clashing neighbor is scheduled in this slot
            String assignedSlotId = null;
            for (TimeSlot slot : systemSlots) {
                String slotId = slot.getId();
                
                // Constraint A: Lecturer availability (if lecturer has availability defined; if list is empty, assume open)
                boolean lecturerAvailable = lecturer == null || 
                                             lecturer.getAvailableSlots().isEmpty() || 
                                             lecturer.getAvailableSlots().contains(slotId);
                
                // Constraint B: No conflict with neighbor colors (same lecturer or same student batch at the same time)
                boolean slotFreeOfConflicts = !neighborOccupiedSlots.contains(slotId);

                if (lecturerAvailable && slotFreeOfConflicts) {
                    assignedSlotId = slotId;
                    break; // Found the smallest color index that fits
                }
            }

            // Assign color (timeslot ID) to current session
            currentSession.setAssignedSlotId(assignedSlotId);
        }
    }
}
