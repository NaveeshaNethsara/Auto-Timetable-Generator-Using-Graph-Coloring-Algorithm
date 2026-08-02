package com.autotimetable.datastructure;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConflictGraph {
    // Map representation of Adjacency List: SessionID -> Set of SessionIDs that clash with it
    private final Map<String, Set<String>> adjacencyList;

    public ConflictGraph() {
        this.adjacencyList = new HashMap<>();
    }

    public void addVertex(String sessionId) {
        adjacencyList.putIfAbsent(sessionId, new HashSet<>());
    }

    public void addEdge(String sessionA, String sessionB) {
        addVertex(sessionA);
        addVertex(sessionB);
        
        // Undirected graph: add both ways
        adjacencyList.get(sessionA).add(sessionB);
        adjacencyList.get(sessionB).add(sessionA);
    }

    public Set<String> getNeighbors(String sessionId) {
        return adjacencyList.getOrDefault(sessionId, Collections.emptySet());
    }

    public int getDegree(String sessionId) {
        return adjacencyList.containsKey(sessionId) ? adjacencyList.get(sessionId).size() : 0;
    }

    public Set<String> getVertices() {
        return adjacencyList.keySet();
    }

    public boolean hasEdge(String sessionA, String sessionB) {
        return adjacencyList.containsKey(sessionA) && adjacencyList.get(sessionA).contains(sessionB);
    }

    public void clear() {
        adjacencyList.clear();
    }
}
