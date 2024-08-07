package gov.cms.madie.cql_elm_translator.utils.cql.parsing.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CQLGraph {

  private final Map<String, Set<String>> graph = new HashMap<>();

  public void addNode(String identifier) {
    Set<String> set = new HashSet<>();
    this.graph.put(identifier, set);
  }

  public void addEdge(String parent, String child) {
    // check to see if the node exists yet.
    if (this.graph.get(parent) == null) {
      addNode(parent);
    }

    if (this.graph.get(child) == null) {
      addNode(child);
    }

    // create an edge between the parent to the child.
    // parent ---> child
    this.graph.get(parent).add(child);
  }

  public boolean isPath(String source, String destination) {

    // if a node is itself, return false because an expression cannot call itself
    if (source.equals(destination)) {
      return false;
    }

    // if the String is in this set, it has been visited
    Set<String> visited = new HashSet<>();

    Queue<String> queue = new ArrayDeque<>();
    visited.add(source); // mark source as visited.
    queue.add(source);

    while (!queue.isEmpty()) {
      String currentNode = queue.remove();
      // MAT-7189 unable run test
      if (this.graph.get(currentNode) != null) {
        List<String> adjacentVertices = new ArrayList<>(this.graph.get(currentNode));

        for (String adjacentNode : adjacentVertices) {
          // we've found the destination node that we were looking for, so return true.
          if (adjacentNode.equals(destination)) {
            return true;
          }

          // if it's not the destination node and the node hasn't been visited yet, add it to the
          // queue to be visited.
          if (!visited.contains(adjacentNode)) {
            visited.add(adjacentNode);
            queue.add(adjacentNode);
          }
        }
      } else {
        log.error(
            "source = "
                + source
                + " destination = "
                + destination
                + " this.graph.get currentNode: "
                + currentNode
                + " is null");
      }
    }
    // if we never find the destination node...
    return false;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (String node : graph.keySet()) {
      builder.append(node).append(" ---> ").append(graph.get(node)).append("\n");
    }
    return builder.toString();
  }

  public Map<String, Set<String>> getAdjacencyList() {
    return new HashMap<>(graph);
  }
}
