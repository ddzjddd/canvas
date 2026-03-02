package com.canvas.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GraphValidator {
    private static final Map<String, Map<String, String>> INPUTS = Map.of(
            "mysql_connection_ref", Map.of(),
            "mysql_query", Map.of("conn_id", "string"),
            "vps_host_ref", Map.of(),
            "ssh_command", Map.of("host_id", "string")
    );
    private static final Map<String, Map<String, String>> OUTPUTS = Map.of(
            "mysql_connection_ref", Map.of("conn_id", "string", "dsn_masked", "string"),
            "mysql_query", Map.of("rows", "array", "row_count", "number", "columns", "array", "elapsed_ms", "number"),
            "vps_host_ref", Map.of("host_id", "string", "host_masked", "string"),
            "ssh_command", Map.of("stdout", "string", "stderr", "string", "exit_code", "number", "elapsed_ms", "number")
    );
    private static final Map<String, List<String>> REQUIRED = Map.of(
            "mysql_connection_ref", List.of("conn_id"),
            "mysql_query", List.of("sql"),
            "vps_host_ref", List.of("host_id"),
            "ssh_command", List.of("cmd")
    );

    public void validate(JsonNode graph) {
        Map<String, JsonNode> nodes = new HashMap<>();
        graph.get("nodes").forEach(n -> nodes.put(n.get("id").asText(), n));

        for (JsonNode n : graph.get("nodes")) {
            String type = n.get("type").asText();
            if (!INPUTS.containsKey(type)) throw new IllegalArgumentException("Unsupported node type: " + type);
            for (String key : REQUIRED.get(type)) {
                if (n.path("config").get(key) == null) throw new IllegalArgumentException("Missing config: " + key);
            }
        }

        Map<String, Integer> indegree = new HashMap<>();
        Map<String, List<String>> adj = new HashMap<>();
        nodes.keySet().forEach(id -> { indegree.put(id, 0); adj.put(id, new ArrayList<>()); });

        for (JsonNode e : graph.get("edges")) {
            String fromNode = e.get("from").get("node").asText();
            String fromPort = e.get("from").get("port").asText();
            String toNode = e.get("to").get("node").asText();
            String toPort = e.get("to").get("port").asText();

            JsonNode src = nodes.get(fromNode); JsonNode dst = nodes.get(toNode);
            if (src == null || dst == null) throw new IllegalArgumentException("Edge references unknown nodes");

            String outType = OUTPUTS.get(src.get("type").asText()).get(fromPort);
            String inType = INPUTS.get(dst.get("type").asText()).get(toPort);
            if (inType == null || !Objects.equals(outType, inType)) throw new IllegalArgumentException("Type mismatch");

            adj.get(fromNode).add(toNode); indegree.put(toNode, indegree.get(toNode) + 1);
        }

        ArrayDeque<String> q = new ArrayDeque<>();
        indegree.forEach((k, v) -> { if (v == 0) q.add(k); });
        int visited = 0;
        while (!q.isEmpty()) {
            String cur = q.poll(); visited++;
            for (String nxt : adj.get(cur)) {
                indegree.put(nxt, indegree.get(nxt) - 1);
                if (indegree.get(nxt) == 0) q.add(nxt);
            }
        }
        if (visited != nodes.size()) throw new IllegalArgumentException("Graph contains cycle");
    }
}
