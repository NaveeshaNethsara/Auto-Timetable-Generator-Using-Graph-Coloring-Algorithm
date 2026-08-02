package com.autotimetable.algorithm;

import com.autotimetable.datastructure.ConflictGraph;
import com.autotimetable.model.LectureSession;
import java.util.List;

public class ConflictDetector {

    /**
     * Builds a conflict graph from a list of lecture sessions.
     * Two sessions conflict if they share the same lecturer or student batch.
     */
    public ConflictGraph buildConflictGraph(List<LectureSession> sessions) {
        ConflictGraph graph = new ConflictGraph();
        
        // Add all sessions as vertices
        for (LectureSession session : sessions) {
            graph.addVertex(session.getSessionId());
        }

        // Compare all pairs of sessions to detect conflicts
        int size = sessions.size();
        for (int i = 0; i < size; i++) {
            LectureSession sessionA = sessions.get(i);
            String lecturerA = sessionA.getModule().getLecturerId();
            String batchA = sessionA.getModule().getBatchId();

            for (int j = i + 1; j < size; j++) {
                LectureSession sessionB = sessions.get(j);
                String lecturerB = sessionB.getModule().getLecturerId();
                String batchB = sessionB.getModule().getBatchId();

                // Conflict criteria: Same lecturer OR same student batch
                if (lecturerA.equals(lecturerB) || batchA.equals(batchB)) {
                    graph.addEdge(sessionA.getSessionId(), sessionB.getSessionId());
                }
            }
        }

        // Update the conflict degree (vertex degree) for each session
        for (LectureSession session : sessions) {
            int degree = graph.getDegree(session.getSessionId());
            session.setConflictDegree(degree);
        }

        return graph;
    }
}
