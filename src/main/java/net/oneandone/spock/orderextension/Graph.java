package net.oneandone.spock.orderextension;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Graph {
    private final Map<String, String> adj = new HashMap<>();

    private final Set<String> allowedVertices;

    Graph(Set<String> allowedVertices) {
        this.allowedVertices = allowedVertices;
    }

    boolean containsTo(String to) {
        return adj.containsValue(to);
    }

    void addEdge(String from, String to) {
        if (!allowedVertices.contains(from)) {
            throw new IllegalArgumentException("'$from' is not a valid feature name or skipped");
        }
        if (!allowedVertices.contains(to)) {
            throw new IllegalArgumentException("'$to' is not a valid feature name or skipped");
        }
        if (adj.containsValue(to)) {
            String key = adj.entrySet().stream().filter(e -> e.getValue().equals(to)).findAny().get().getKey();
            throw new IllegalArgumentException("Unclear execution order. Failing to add '" + to
                    + "' to '" + from + "'. '" + to + "' is already after " + key);
        }
        if (adj.containsKey(from)) {
            throw new IllegalArgumentException("Unclear execution order. Failing to add '" + to
                    + "' to '" + from + "'. '" + from + "' is already followed by " + adj.get(from));

        }
        adj.put(from, to);
    }

    private void markAsVisitedAndPushToStack(String vertex, Set<String> visited, Deque<String> stack) {
        visited.add(vertex);

        // follow all unvisited edges
        String to = adj.get(vertex);
        if (to != null && !visited.contains(to)) {
                markAsVisitedAndPushToStack(to, visited, stack);
        }

        stack.push(vertex);
    }

    List<String> topologicalSort() {
        Deque<String> resultStack = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();

        for (String vertex : adj.keySet()) {
            if (!visited.contains(vertex)) {
                markAsVisitedAndPushToStack(vertex, visited, resultStack);
            }
        }

        return new ArrayList<>(resultStack);
    }
}
