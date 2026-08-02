package com.autotimetable.algorithm;

import com.autotimetable.model.Batch;
import com.autotimetable.model.Classroom;
import com.autotimetable.model.LectureSession;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ClassroomAllocator {

    /**
     * Allocates classrooms to scheduled lecture sessions based on capacity and availability.
     * Implements the Auto Room Optimizer (assigns the smallest room that fits).
     */
    public void allocateClassrooms(List<LectureSession> sessions, List<Classroom> classrooms, Map<String, Batch> batches) {
        // 1. Reset occupied slots for all classrooms
        for (Classroom classroom : classrooms) {
            classroom.clearBookings();
        }

        // 2. Sort classrooms by capacity in ascending order (Auto Room Optimizer)
        List<Classroom> sortedClassrooms = new ArrayList<>(classrooms);
        sortedClassrooms.sort(Comparator.comparingInt(Classroom::getCapacity));

        // Reset room assignments on sessions
        for (LectureSession session : sessions) {
            session.setAssignedRoomId(null);
        }

        // 3. Match sessions to rooms
        for (LectureSession session : sessions) {
            String slotId = session.getAssignedSlotId();
            if (slotId == null) {
                continue; // Skip unscheduled sessions
            }

            // Retrieve batch size
            String batchId = session.getModule().getBatchId();
            Batch batch = batches.get(batchId);
            int studentCount = (batch != null) ? batch.getStudentCount() : 0;

            // Find the smallest room that matches the capacity and is available
            boolean allocated = false;
            for (Classroom room : sortedClassrooms) {
                if (room.getCapacity() >= studentCount && room.isAvailable(slotId)) {
                    room.bookSlot(slotId);
                    session.setAssignedRoomId(room.getId());
                    allocated = true;
                    break; // Allocated the smallest matching room, move to next session
                }
            }

            if (!allocated) {
                // If we get here, it means we couldn't find a room matching capacity AND availability.
                // We leave assignedRoomId as null to represent a Room Allocation Conflict.
                session.setAssignedRoomId(null);
            }
        }
    }
}
