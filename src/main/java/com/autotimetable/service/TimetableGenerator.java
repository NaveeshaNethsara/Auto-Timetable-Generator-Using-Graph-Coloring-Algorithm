package com.autotimetable.service;

import com.autotimetable.algorithm.ClassroomAllocator;
import com.autotimetable.algorithm.ConflictDetector;
import com.autotimetable.algorithm.GreedyGraphColoring;
import com.autotimetable.datastructure.ConflictGraph;
import com.autotimetable.model.Lecturer;
import com.autotimetable.model.Module;
import com.autotimetable.model.Batch;
import com.autotimetable.model.Classroom;
import com.autotimetable.model.TimeSlot;
import com.autotimetable.model.LectureSession;
import com.autotimetable.model.ScheduleEntry;
import com.autotimetable.service.QualityCalculator.QualityMetrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableGenerator {

    private final ConflictDetector conflictDetector;
    private final GreedyGraphColoring graphColorer;
    private final ClassroomAllocator classroomAllocator;
    private final QualityCalculator qualityCalculator;

    public TimetableGenerator() {
        this.conflictDetector = new ConflictDetector();
        this.graphColorer = new GreedyGraphColoring();
        this.classroomAllocator = new ClassroomAllocator();
        this.qualityCalculator = new QualityCalculator();
    }

    public static class GenerationResult {
        private final List<ScheduleEntry> scheduleEntries;
        private final List<LectureSession> allSessions;
        private final List<LectureSession> unscheduledSessions;
        private final ConflictGraph conflictGraph;
        private final QualityMetrics qualityMetrics;
        private final long executionTimeMs;

        public GenerationResult(List<ScheduleEntry> scheduleEntries, List<LectureSession> allSessions,
                                List<LectureSession> unscheduledSessions, ConflictGraph conflictGraph,
                                QualityMetrics qualityMetrics, long executionTimeMs) {
            this.scheduleEntries = scheduleEntries;
            this.allSessions = allSessions;
            this.unscheduledSessions = unscheduledSessions;
            this.conflictGraph = conflictGraph;
            this.qualityMetrics = qualityMetrics;
            this.executionTimeMs = executionTimeMs;
        }

        public List<ScheduleEntry> getScheduleEntries() { return scheduleEntries; }
        public List<LectureSession> getAllSessions() { return allSessions; }
        public List<LectureSession> getUnscheduledSessions() { return unscheduledSessions; }
        public ConflictGraph getConflictGraph() { return conflictGraph; }
        public QualityMetrics getQualityMetrics() { return qualityMetrics; }
        public long getExecutionTimeMs() { return executionTimeMs; }
    }

    /**
     * Generates a conflict-free timetable schedule from lists of academic entities.
     */
    public GenerationResult generateTimetable(
            List<Lecturer> lecturers,
            List<Module> modules,
            List<Batch> batches,
            List<Classroom> classrooms,
            List<TimeSlot> timeSlots) {

        long startTime = System.nanoTime();

        // 1. Generate discrete LectureSession vertices from modules based on requiredSessions count
        List<LectureSession> sessions = new ArrayList<>();
        for (Module module : modules) {
            int sessionsCount = module.getRequiredSessions();
            for (int s = 1; s <= sessionsCount; s++) {
                // E.g., Module code "PDSA" -> session ID "PDSA_S1", "PDSA_S2"
                String sessionId = module.getCode() + "_S" + s;
                sessions.add(new LectureSession(sessionId, module));
            }
        }

        // Convert lookup lists to maps for O(1) retrieval
        Map<String, Lecturer> lecturerMap = new HashMap<>();
        for (Lecturer l : lecturers) lecturerMap.put(l.getId(), l);

        Map<String, Batch> batchMap = new HashMap<>();
        for (Batch b : batches) batchMap.put(b.getId(), b);

        Map<String, Classroom> roomMap = new HashMap<>();
        for (Classroom c : classrooms) roomMap.put(c.getId(), c);

        Map<String, TimeSlot> slotMap = new HashMap<>();
        for (TimeSlot ts : timeSlots) slotMap.put(ts.getId(), ts);

        // 2. Build Conflict Graph & Compute Vertex Degrees
        ConflictGraph graph = conflictDetector.buildConflictGraph(sessions);

        // 3. Color Graph (Assign Timeslots)
        graphColorer.colorGraph(sessions, graph, timeSlots, lecturerMap);

        // 4. Allocate Classrooms
        classroomAllocator.allocateClassrooms(sessions, classrooms, batchMap);

        // 5. Build final ScheduleEntry list and find unscheduled elements
        List<ScheduleEntry> scheduleEntries = new ArrayList<>();
        List<LectureSession> unscheduledSessions = new ArrayList<>();

        for (LectureSession session : sessions) {
            String slotId = session.getAssignedSlotId();
            String roomId = session.getAssignedRoomId();

            if (slotId != null && roomId != null) {
                Module module = session.getModule();
                Lecturer lecturer = lecturerMap.get(module.getLecturerId());
                Batch batch = batchMap.get(module.getBatchId());
                TimeSlot timeSlot = slotMap.get(slotId);
                Classroom classroom = roomMap.get(roomId);

                scheduleEntries.add(new ScheduleEntry(session, module, lecturer, batch, timeSlot, classroom));
            } else {
                unscheduledSessions.add(session);
            }
        }

        // 6. Compute Quality Score Metrics
        QualityMetrics metrics = qualityCalculator.calculateQuality(sessions, classrooms, timeSlots, batchMap);

        long endTime = System.nanoTime();
        long executionTimeMs = (endTime - startTime) / 1_000_000; // Convert nanoseconds to milliseconds

        return new GenerationResult(
                scheduleEntries,
                sessions,
                unscheduledSessions,
                graph,
                metrics,
                executionTimeMs
        );
    }
}
