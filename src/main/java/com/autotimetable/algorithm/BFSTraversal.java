package com.autotimetable.algorithm;

import com.autotimetable.datastructure.ConflictGraph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class BFSTraversal {

    /**
     * Traverses the conflict graph using BFS and returns the list of connected components.
     * Each connected component represents a set of session IDs that are transitively clashing.
     */
    public List<Set<String>> findConnectedComponents(ConflictGraph graph) {
        List<Set<String>> components = new ArrayList<>();
        Set<String> visited = new HashSet<>();

        // Perform BFS starting from every unvisited vertex to identify components
        for (String vertex : graph.getVertices()) {
            if (!visited.contains(vertex)) {
                Set<String> component = new HashSet<>();
                Queue<String> queue = new LinkedList<>();

                queue.add(vertex);
                visited.add(vertex);

                while (!queue.isEmpty()) {
                    String current = queue.poll();
                    component.add(current);

                    // Inspect all neighbors of current vertex
                    for (String neighbor : graph.getNeighbors(current)) {
                        if (!visited.contains(neighbor)) {
                            visited.add(neighbor);
                            queue.add(neighbor);
                        }
                    }
                }
                components.add(component);
            }
        }
        return components;
    }
}
