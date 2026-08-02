package com.autotimetable.service;

import com.autotimetable.model.Batch;
import com.autotimetable.model.Classroom;
import com.autotimetable.model.Lecturer;
import com.autotimetable.model.Module;
import com.autotimetable.model.TimeSlot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConflictReportService {

    public static class ConflictAnalyzerReport {
        private final List<String> lecturerWarnings = new ArrayList<>();
        private final List<String> batchWarnings = new ArrayList<>();
        private final List<String> roomWarnings = new ArrayList<>();

        public void addLecturerWarning(String warning) { lecturerWarnings.add(warning); }
        public void addBatchWarning(String warning) { batchWarnings.add(warning); }
        public void addRoomWarning(String warning) { roomWarnings.add(warning); }

        public List<String> getLecturerWarnings() { return lecturerWarnings; }
        public List<String> getBatchWarnings() { return batchWarnings; }
        public List<String> getRoomWarnings() { return roomWarnings; }

        public boolean hasWarnings() {
            return !lecturerWarnings.isEmpty() || !batchWarnings.isEmpty() || !roomWarnings.isEmpty();
        }

        public int getTotalWarningCount() {
            return lecturerWarnings.size() + batchWarnings.size() + roomWarnings.size();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== PRE-GENERATION DIAGNOSTIC CONFLICT REPORT ===\n");
            
            if (lecturerWarnings.isEmpty()) {
                sb.append("[OK] Lecturer availability constraints look healthy.\n");
            } else {
                sb.append("[WARN] Lecturer Availability Bottlenecks:\n");
                for (String w : lecturerWarnings) {
                    sb.append("  - ").append(w).append("\n");
                }
            }

            if (batchWarnings.isEmpty()) {
                sb.append("[OK] Batch slot requests are within system limits.\n");
            } else {
                sb.append("[WARN] Batch Overload Bottlenecks:\n");
                for (String w : batchWarnings) {
                    sb.append("  - ").append(w).append("\n");
                }
            }

            if (roomWarnings.isEmpty()) {
                sb.append("[OK] Classroom capacities fit all batch sizes.\n");
            } else {
                sb.append("[WARN] Classroom Capacity Deficits:\n");
                for (String w : roomWarnings) {
                    sb.append("  - ").append(w).append("\n");
                }
            }
            
            sb.append("=================================================");
            return sb.toString();
        }
    }

    /**
     * Analyzes input data prior to scheduling to check for impossible scheduling conditions.
     */
    public ConflictAnalyzerReport analyzeConflicts(
            List<Lecturer> lecturers,
            List<Module> modules,
            List<Batch> batches,
            List<Classroom> classrooms,
            List<TimeSlot> timeSlots) {

        ConflictAnalyzerReport report = new ConflictAnalyzerReport();

        // Convert lists to maps for easy lookup
        Map<String, Lecturer> lecturerMap = new HashMap<>();
        for (Lecturer l : lecturers) lecturerMap.put(l.getId(), l);

        Map<String, Batch> batchMap = new HashMap<>();
        for (Batch b : batches) batchMap.put(b.getId(), b);

        // 1. Lecturer Availability Diagnostics
        Map<String, Integer> lecturerRequiredSessions = new HashMap<>();
        for (Module module : modules) {
            String licId = module.getLecturerId();
            lecturerRequiredSessions.put(licId, lecturerRequiredSessions.getOrDefault(licId, 0) + module.getRequiredSessions());
        }

        for (Map.Entry<String, Integer> entry : lecturerRequiredSessions.entrySet()) {
            Lecturer lecturer = lecturerMap.get(entry.getKey());
            if (lecturer == null) {
                report.addLecturerWarning("Module is assigned to non-existent Lecturer ID: " + entry.getKey());
                continue;
            }

            int reqCount = entry.getValue();
            int availCount = lecturer.getAvailableSlots().size();

            if (availCount == 0) {
                report.addLecturerWarning("Lecturer " + lecturer.getName() + " (" + lecturer.getId() + ") has no available slots set but is scheduled to teach " + reqCount + " session(s).");
            } else if (availCount < reqCount) {
                report.addLecturerWarning("Lecturer " + lecturer.getName() + " (" + lecturer.getId() + ") has only " + availCount + " slot(s) available, but modules require " + reqCount + " session(s) of teaching.");
            }
        }

        // 2. Batch Overload Diagnostics
        Map<String, Integer> batchRequiredSessions = new HashMap<>();
        for (Module module : modules) {
            String bId = module.getBatchId();
            batchRequiredSessions.put(bId, batchRequiredSessions.getOrDefault(bId, 0) + module.getRequiredSessions());
        }

        int systemSlotsCount = timeSlots.size();
        for (Map.Entry<String, Integer> entry : batchRequiredSessions.entrySet()) {
            Batch batch = batchMap.get(entry.getKey());
            if (batch == null) {
                report.addBatchWarning("Module is assigned to non-existent Student Batch ID: " + entry.getKey());
                continue;
            }

            int reqCount = entry.getValue();
            if (reqCount > systemSlotsCount) {
                report.addBatchWarning("Batch " + batch.getName() + " requires " + reqCount + " sessions, which exceeds the total slots available in the system (" + systemSlotsCount + ").");
            }
        }

        // 3. Room Capacity Diagnostics
        int maxRoomCapacity = 0;
        for (Classroom room : classrooms) {
            if (room.getCapacity() > maxRoomCapacity) {
                maxRoomCapacity = room.getCapacity();
            }
        }

        for (Batch batch : batches) {
            if (batch.getStudentCount() > maxRoomCapacity) {
                report.addRoomWarning("Batch " + batch.getName() + " has " + batch.getStudentCount() + " students, but the largest room available (" + 
                                       (classrooms.isEmpty() ? "None" : maxRoomCapacity) + " seats) cannot fit this cohort.");
            }
        }

        return report;
    }
}
