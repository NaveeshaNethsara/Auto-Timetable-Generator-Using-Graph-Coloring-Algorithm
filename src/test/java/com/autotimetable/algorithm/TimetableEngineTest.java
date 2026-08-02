package com.autotimetable.algorithm;

import com.autotimetable.datastructure.ConflictGraph;
import com.autotimetable.model.Lecturer;
import com.autotimetable.model.Module;
import com.autotimetable.model.Batch;
import com.autotimetable.model.Classroom;
import com.autotimetable.model.TimeSlot;
import com.autotimetable.model.LectureSession;
import com.autotimetable.model.ScheduleEntry;
import com.autotimetable.service.QualityCalculator;
import com.autotimetable.service.QualityCalculator.QualityMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class TimetableEngineTest {

    private List<Lecturer> lecturers;
    private List<Batch> batches;
    private List<Module> modules;
    private List<Classroom> classrooms;
    private List<TimeSlot> timeSlots;

    @BeforeEach
    public void setUp() {
        lecturers = new ArrayList<>();
        batches = new ArrayList<>();
        modules = new ArrayList<>();
        classrooms = new ArrayList<>();
        timeSlots = new ArrayList<>();

        // Create standard mock data
        lecturers.add(new Lecturer("L1", "Lecturer One", "l1@nibm.lk"));
        lecturers.get(0).addAvailableSlot("SLOT1");
        lecturers.get(0).addAvailableSlot("SLOT2");

        lecturers.add(new Lecturer("L2", "Lecturer Two", "l2@nibm.lk"));
        lecturers.get(1).addAvailableSlot("SLOT1");
        lecturers.get(1).addAvailableSlot("SLOT2");

        batches.add(new Batch("B1", "Batch One", 30));
        batches.add(new Batch("B2", "Batch Two", 50));

        classrooms.add(new Classroom("R1", "Small Lab", 40));
        classrooms.add(new Classroom("R2", "Large Hall", 60));

        timeSlots.add(new TimeSlot("SLOT1", "Monday", "08:30", "10:30"));
        timeSlots.add(new TimeSlot("SLOT2", "Monday", "10:30", "12:30"));

        // Module(id, code, name, lecturerId, batchId, requiredSessions)
        modules.add(new Module("M1", "CS101", "Intro CS", "L1", "B1", 1)); // lecturer L1, batch B1
        modules.add(new Module("M2", "CS102", "Databases", "L1", "B2", 1)); // lecturer L1, batch B2 (clashes with M1 on lecturer)
        modules.add(new Module("M3", "CS103", "Networking", "L2", "B1", 1)); // lecturer L2, batch B1 (clashes with M1 on batch)
    }

    @Test
    public void testConflictDetectorAndGraph() {
        ConflictDetector detector = new ConflictDetector();
        List<LectureSession> sessions = new ArrayList<>();
        sessions.add(new LectureSession("M1_S1", modules.get(0))); // M1
        sessions.add(new LectureSession("M2_S1", modules.get(1))); // M2
        sessions.add(new LectureSession("M3_S1", modules.get(2))); // M3

        ConflictGraph graph = detector.buildConflictGraph(sessions);

        // Verify Vertices count
        assertEquals(3, graph.getVertices().size());
        assertTrue(graph.getVertices().contains("M1_S1"));
        assertTrue(graph.getVertices().contains("M2_S1"));
        assertTrue(graph.getVertices().contains("M3_S1"));

        // Verify conflict edges
        // M1 and M2 conflict on Lecturer L1
        assertTrue(graph.hasEdge("M1_S1", "M2_S1"));
        // M1 and M3 conflict on Batch B1
        assertTrue(graph.hasEdge("M1_S1", "M3_S1"));
        // M2 and M3 do NOT conflict (different lecturer and different batch)
        assertFalse(graph.hasEdge("M2_S1", "M3_S1"));

        // Verify degrees
        // M1 has 2 clashes
        assertEquals(2, graph.getDegree("M1_S1"));
        assertEquals(2, sessions.get(0).getConflictDegree());
        // M2 and M3 each have 1 clash
        assertEquals(1, graph.getDegree("M2_S1"));
        assertEquals(1, graph.getDegree("M3_S1"));
    }

    @Test
    public void testBFSTraversalComponents() {
        // Create an isolated graph structure
        ConflictGraph graph = new ConflictGraph();
        graph.addVertex("A");
        graph.addVertex("B");
        graph.addEdge("A", "B"); // Component 1: {A, B}

        graph.addVertex("C");
        graph.addVertex("D");
        graph.addEdge("C", "D"); // Component 2: {C, D}

        graph.addVertex("E"); // Component 3: {E}

        BFSTraversal bfs = new BFSTraversal();
        List<Set<String>> components = bfs.findConnectedComponents(graph);

        assertEquals(3, components.size());

        // Verify contents
        boolean foundAB = false, foundCD = false, foundE = false;
        for (Set<String> component : components) {
            if (component.size() == 2 && component.contains("A") && component.contains("B")) foundAB = true;
            if (component.size() == 2 && component.contains("C") && component.contains("D")) foundCD = true;
            if (component.size() == 1 && component.contains("E")) foundE = true;
        }

        assertTrue(foundAB);
        assertTrue(foundCD);
        assertTrue(foundE);
    }

    @Test
    public void testGreedyGraphColoring() {
        ConflictDetector detector = new ConflictDetector();
        List<LectureSession> sessions = new ArrayList<>();
        sessions.add(new LectureSession("M1_S1", modules.get(0)));
        sessions.add(new LectureSession("M2_S1", modules.get(1)));
        sessions.add(new LectureSession("M3_S1", modules.get(2)));

        ConflictGraph graph = detector.buildConflictGraph(sessions);

        Map<String, Lecturer> lecturerMap = new HashMap<>();
        for (Lecturer l : lecturers) lecturerMap.put(l.getId(), l);

        GreedyGraphColoring colorer = new GreedyGraphColoring();
        colorer.colorGraph(sessions, graph, timeSlots, lecturerMap);

        // Verify slot assignments
        // M1_S1 has conflict degree 2, colored first. Should get SLOT1.
        String m1Slot = sessions.get(0).getAssignedSlotId();
        assertNotNull(m1Slot);

        // M2_S1 conflicts with M1_S1, so it cannot get the same slot. Should get SLOT2.
        String m2Slot = sessions.get(1).getAssignedSlotId();
        assertNotNull(m2Slot);
        assertNotEquals(m1Slot, m2Slot);

        // M3_S1 conflicts with M1_S1, so it cannot get SLOT1. It should get SLOT2.
        String m3Slot = sessions.get(2).getAssignedSlotId();
        assertNotNull(m3Slot);
        assertNotEquals(m1Slot, m3Slot);

        // M2 and M3 can share SLOT2 since they don't conflict
        assertEquals(m2Slot, m3Slot);
    }

    @Test
    public void testClassroomAllocator() {
        // Setup mock sessions with timeslots assigned
        List<LectureSession> sessions = new ArrayList<>();
        
        // M1_S1 (Batch B1, size 30) -> Assigned SLOT1
        LectureSession s1 = new LectureSession("M1_S1", modules.get(0));
        s1.setAssignedSlotId("SLOT1");
        sessions.add(s1);

        // M2_S1 (Batch B2, size 50) -> Assigned SLOT1
        LectureSession s2 = new LectureSession("M2_S1", modules.get(1));
        s2.setAssignedSlotId("SLOT1");
        sessions.add(s2);

        Map<String, Batch> batchMap = new HashMap<>();
        for (Batch b : batches) batchMap.put(b.getId(), b);

        ClassroomAllocator allocator = new ClassroomAllocator();
        allocator.allocateClassrooms(sessions, classrooms, batchMap);

        // Classrooms are R1 (Small Lab, Cap: 40) and R2 (Large Hall, Cap: 60)
        // B1 (size 30) fits in R1 (40) and R2 (60). Auto Room Optimizer should assign smaller room R1 first.
        assertEquals("R1", s1.getAssignedRoomId());

        // B2 (size 50) fits ONLY in R2 (60) because R1 cap is 40 (too small).
        assertEquals("R2", s2.getAssignedRoomId());
    }

    @Test
    public void testQualityCalculator() {
        List<LectureSession> sessions = new ArrayList<>();
        LectureSession s1 = new LectureSession("M1_S1", modules.get(0));
        s1.setAssignedSlotId("SLOT1");
        s1.setAssignedRoomId("R1");
        sessions.add(s1);

        LectureSession s2 = new LectureSession("M2_S1", modules.get(1));
        // s2 is unscheduled

        sessions.add(s2);

        Map<String, Batch> batchMap = new HashMap<>();
        for (Batch b : batches) batchMap.put(b.getId(), b);

        QualityCalculator calc = new QualityCalculator();
        QualityMetrics metrics = calc.calculateQuality(sessions, classrooms, timeSlots, batchMap);

        // 1 of 2 scheduled = 50% scheduling rate
        assertEquals(50.0, metrics.getSchedulingRate());

        // s1: Batch B1 (30 students) assigned to room R1 (capacity 40).
        // Room Utilization = 30 / 40 = 75.0%
        assertEquals(75.0, metrics.getRoomUtilizationRate());

        // 1 slot used of 2 total slots = 50%
        assertEquals(50.0, metrics.getSlotUtilizationRate());

        // Overall Score = 0.50 * 50% + 0.30 * 75% + 0.20 * 50% = 25.0% + 22.5% + 10% = 57.5%
        assertEquals(57.5, metrics.getOverallScore());
    }
}
