package com.autotimetable.service;

import com.autotimetable.model.Batch;
import com.autotimetable.model.Classroom;
import com.autotimetable.model.LectureSession;
import com.autotimetable.model.TimeSlot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QualityCalculator {

    public static class QualityMetrics {
        private final double overallScore;       // 0% - 100%
        private final double schedulingRate;      // 0% - 100%
        private final double roomUtilizationRate; // 0% - 100%
        private final double slotUtilizationRate; // 0% - 100%
        
        private final int totalSessions;
        private final int scheduledSessions;
        private final int unscheduledSessions;

        public QualityMetrics(double overallScore, double schedulingRate, double roomUtilizationRate, 
                              double slotUtilizationRate, int totalSessions, int scheduledSessions) {
            this.overallScore = overallScore;
            this.schedulingRate = schedulingRate;
            this.roomUtilizationRate = roomUtilizationRate;
            this.slotUtilizationRate = slotUtilizationRate;
            this.totalSessions = totalSessions;
            this.scheduledSessions = scheduledSessions;
            this.unscheduledSessions = totalSessions - scheduledSessions;
        }

        public double getOverallScore() { return overallScore; }
        public double getSchedulingRate() { return schedulingRate; }
        public double getRoomUtilizationRate() { return roomUtilizationRate; }
        public double getSlotUtilizationRate() { return slotUtilizationRate; }
        public int getTotalSessions() { return totalSessions; }
        public int getScheduledSessions() { return scheduledSessions; }
        public int getUnscheduledSessions() { return unscheduledSessions; }
    }

    /**
     * Calculates the overall scheduling quality score and its sub-metrics.
     */
    public QualityMetrics calculateQuality(
            List<LectureSession> sessions, 
            List<Classroom> classrooms, 
            List<TimeSlot> systemSlots, 
            Map<String, Batch> batches) {

        if (sessions.isEmpty()) {
            return new QualityMetrics(0, 0, 0, 0, 0, 0);
        }

        int totalSessions = sessions.size();
        int scheduledSessions = 0;
        
        long sumStudents = 0;
        long sumRoomCapacities = 0;
        
        Set<String> uniqueUsedSlots = new HashSet<>();
        
        // Map rooms for quick lookup
        Map<String, Classroom> roomMap = new HashMap<>();
        for (Classroom c : classrooms) {
            roomMap.put(c.getId(), c);
        }

        for (LectureSession session : sessions) {
            if (session.getAssignedSlotId() != null) {
                scheduledSessions++;
                uniqueUsedSlots.add(session.getAssignedSlotId());

                // Calculate room utilization if room is allocated
                if (session.getAssignedRoomId() != null) {
                    Classroom room = roomMap.get(session.getAssignedRoomId());
                    Batch batch = batches.get(session.getModule().getBatchId());
                    if (room != null && batch != null) {
                        sumStudents += batch.getStudentCount();
                        sumRoomCapacities += room.getCapacity();
                    }
                }
            }
        }

        // 1. Scheduling Success Rate (50% weight)
        double schedulingRate = (double) scheduledSessions / totalSessions;

        // 2. Room Capacity Utilization Rate (30% weight)
        // Ratio of actual student size to assigned classroom capacity
        double roomUtilization = (sumRoomCapacities > 0) ? (double) sumStudents / sumRoomCapacities : 0.0;

        // 3. Time Slots Usage Rate (20% weight)
        // Ratio of distinct slot IDs used to total slots available in system
        double slotsUsedRatio = (!systemSlots.isEmpty()) ? (double) uniqueUsedSlots.size() / systemSlots.size() : 0.0;

        // Overall score formula:
        // Score = 0.50 * schedulingRate + 0.30 * roomUtilization + 0.20 * slotsUsedRatio
        double overallScoreVal = (0.50 * schedulingRate + 0.30 * roomUtilization + 0.20 * slotsUsedRatio) * 100.0;

        // Round to 1 decimal place
        overallScoreVal = Math.round(overallScoreVal * 10.0) / 10.0;
        double schedPct = Math.round(schedulingRate * 1000.0) / 10.0;
        double roomPct = Math.round(roomUtilization * 1000.0) / 10.0;
        double slotPct = Math.round(slotsUsedRatio * 1000.0) / 10.0;

        return new QualityMetrics(
                overallScoreVal, 
                schedPct, 
                roomPct, 
                slotPct, 
                totalSessions, 
                scheduledSessions
        );
    }
}
