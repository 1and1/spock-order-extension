/*
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.spock.orderextension;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
            throw new IllegalArgumentException(from + " is not a valid feature name or skipped");
        }
        if (!allowedVertices.contains(to)) {
            throw new IllegalArgumentException(to + " is not a valid feature name or skipped");
        }
        if (adj.containsKey(from) || adj.containsValue(to)) {
            String graphRep = adj.entrySet().stream().map(e -> "'" + e.getKey() + "' -> '" + e.getValue() + "'").collect(Collectors.joining(", "));
            throw new IllegalArgumentException("Unclear execution order. Failing to add order dependency '" + from
                    + "' -> '" + to + "'. Graph already contains: " + graphRep + "");

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
