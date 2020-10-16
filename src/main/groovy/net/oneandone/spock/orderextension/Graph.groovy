package net.oneandone.spock.orderextension

import groovy.transform.PackageScope

@PackageScope
class Graph {
    private Map<String, String> adj = new HashMap<String, String>()

    private Set<String> allowedVertices

    Graph(Set<String> allowedVertices) {
        this.allowedVertices = allowedVertices
    }

    boolean containsTo(String to) {
        return adj.containsValue(to)
    }

    void addEdge(String from, String to) {
        if (!allowedVertices.contains(from)) {
            throw new IllegalArgumentException("'$from' is not a valid feature name or skippeed")
        }
        if (!allowedVertices.contains(to)) {
            throw new IllegalArgumentException("'$to' is not a valid feature name or skipped")
        }
        if (adj.containsValue(to)) {
            throw new IllegalArgumentException("Unclear execution order. Failing to add '$to' to '$from'. '$from' is already followed by " + adj[from])
        }
        if (adj.containsKey(from)) {
            throw new IllegalArgumentException("Unclear execution order. Failing to add '$to' to '$from'. '$from' is already followed by " + adj[from])
        }
        adj[from] = to
    }

    private void markAsVisitedAndPushToStack(String vertex, Set<String> visited, Stack<String> stack) {
        visited.add(vertex)

        // follow all unvisited edges
        adj.get(vertex).tap {
            if (it && !visited.contains(it)) {
                markAsVisitedAndPushToStack(it, visited, stack)
            }
        }

        stack.push(vertex)
    }

    List<String> topologicalSort() {
        Stack<String> resultStack = new Stack<String>()
        Set<String> visited = new HashSet<String>()

        adj.keySet().forEach {
            if (!visited.contains(it)) {
                markAsVisitedAndPushToStack(it, visited, resultStack)
            }
        }

        return resultStack.reverse().collect()
    }
}
