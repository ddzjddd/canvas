package com.canvas.workflow;

import com.canvas.config.CryptoService;
import com.canvas.domain.*;
import com.canvas.repo.*;
import com.canvas.service.MySqlExecutionService;
import com.canvas.service.SshExecutionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class WorkflowOrchestrator {
    private final ObjectMapper mapper;
    private final WorkflowRunRepo runRepo;
    private final NodeRunRepo nodeRunRepo;
    private final NodeLogRepo nodeLogRepo;
    private final MySqlConnectionRepo connRepo;
    private final VpsHostRepo hostRepo;
    private final CryptoService crypto;
    private final MySqlExecutionService mySqlExec;
    private final SshExecutionService sshExec;

    public WorkflowOrchestrator(ObjectMapper mapper, WorkflowRunRepo runRepo, NodeRunRepo nodeRunRepo, NodeLogRepo nodeLogRepo,
                                MySqlConnectionRepo connRepo, VpsHostRepo hostRepo, CryptoService crypto,
                                MySqlExecutionService mySqlExec, SshExecutionService sshExec) {
        this.mapper = mapper; this.runRepo = runRepo; this.nodeRunRepo = nodeRunRepo; this.nodeLogRepo = nodeLogRepo;
        this.connRepo = connRepo; this.hostRepo = hostRepo; this.crypto = crypto; this.mySqlExec = mySqlExec; this.sshExec = sshExec;
    }

    public String run(WorkflowEntity wf, Map<String, Object> inputs) throws Exception {
        JsonNode g = mapper.readTree(wf.getGraphJson());
        List<String> order = topo(g);
        Map<String, JsonNode> nodes = new HashMap<>();
        g.get("nodes").forEach(n -> nodes.put(n.get("id").asText(), n));
        Map<String, List<JsonNode>> inbound = new HashMap<>();
        g.get("edges").forEach(e -> inbound.computeIfAbsent(e.get("to").get("node").asText(), k -> new ArrayList<>()).add(e));

        WorkflowRunEntity run = new WorkflowRunEntity();
        run.setWorkflowId(wf.getId()); run.setStatus("RUNNING"); run.setInputsJson(mapper.writeValueAsString(inputs));
        run = runRepo.save(run);

        Map<String, Map<String, Object>> outputs = new HashMap<>();
        NodeRunEntity current = null;
        try {
            for (String nodeId : order) {
                JsonNode n = nodes.get(nodeId);
                current = new NodeRunEntity();
                current.setRunId(run.getId()); current.setNodeId(nodeId); current.setStatus("RUNNING"); current.setStartedAt(Instant.now());
                current = nodeRunRepo.save(current);

                Map<String, Object> in = new HashMap<>();
                for (JsonNode e : inbound.getOrDefault(nodeId, List.of())) {
                    String srcNode = e.get("from").get("node").asText();
                    String srcPort = e.get("from").get("port").asText();
                    String dstPort = e.get("to").get("port").asText();
                    in.put(dstPort, outputs.get(srcNode).get(srcPort));
                }

                Map<String, Object> out = executeNode(n, in, inputs, outputs);
                outputs.put(nodeId, out);
                current.setOutputsJson(mapper.writeValueAsString(out));
                current.setStatus("SUCCESS"); current.setFinishedAt(Instant.now()); nodeRunRepo.save(current);
                log(current.getId(), "system", "Node " + nodeId + " success");
            }
            run.setStatus("SUCCESS"); run.setFinishedAt(Instant.now()); runRepo.save(run);
        } catch (Exception ex) {
            run.setStatus("FAILED"); run.setFinishedAt(Instant.now()); runRepo.save(run);
            if (current != null) {
                current.setStatus("FAILED"); current.setError(ex.getMessage()); current.setFinishedAt(Instant.now()); nodeRunRepo.save(current);
                log(current.getId(), "stderr", ex.getMessage());
            }
        }
        return run.getId();
    }

    private Map<String, Object> executeNode(JsonNode node, Map<String, Object> injected, Map<String, Object> runInputs,
                                            Map<String, Map<String, Object>> outputs) throws Exception {
        String type = node.get("type").asText();
        JsonNode cfg = node.path("config");
        if ("mysql_connection_ref".equals(type)) {
            String id = cfg.get("conn_id").asText();
            MySqlConnectionEntity c = connRepo.findById(id).orElseThrow();
            return Map.of("conn_id", c.getId(), "dsn_masked", c.getUsername() + "@" + c.getHost() + ":" + c.getPort() + "/" + c.getDatabaseName());
        }
        if ("vps_host_ref".equals(type)) {
            String id = cfg.get("host_id").asText();
            VpsHostEntity h = hostRepo.findById(id).orElseThrow();
            return Map.of("host_id", h.getId(), "host_masked", h.getUsername() + "@" + h.getHost() + ":" + h.getPort());
        }
        if ("mysql_query".equals(type)) {
            String connId = String.valueOf(injected.getOrDefault("conn_id", cfg.path("conn_id").asText()));
            MySqlConnectionEntity c = connRepo.findById(connId).orElseThrow();
            String sql = render(cfg.get("sql").asText(), runInputs, outputs);
            return mySqlExec.executeSelect(c.getHost(), c.getUsername(), crypto.decrypt(c.getPasswordEnc()), sql, cfg.path("max_rows").asInt(1000));
        }
        if ("ssh_command".equals(type)) {
            String hostId = String.valueOf(injected.getOrDefault("host_id", cfg.path("host_id").asText()));
            VpsHostEntity h = hostRepo.findById(hostId).orElseThrow();
            String cmd = render(cfg.get("cmd").asText(), runInputs, outputs);
            return sshExec.execute(h.getHost(), h.getPort(), h.getUsername(), crypto.decrypt(h.getPrivateKeyEnc()), cmd,
                    cfg.path("timeout_ms").asInt(10000), h.getKnownHostsPolicy());
        }
        throw new IllegalArgumentException("unsupported node type");
    }

    private String render(String template, Map<String, Object> runInputs, Map<String, Map<String, Object>> outputs) {
        String out = template;
        for (var e : runInputs.entrySet()) out = out.replace("{{ inputs." + e.getKey() + " }}", String.valueOf(e.getValue()));
        for (var n : outputs.entrySet()) {
            for (var o : n.getValue().entrySet()) {
                out = out.replace("{{ " + n.getKey() + ".outputs." + o.getKey() + " }}", String.valueOf(o.getValue()));
            }
        }
        return out;
    }

    private List<String> topo(JsonNode graph) {
        Map<String, Integer> indegree = new HashMap<>();
        Map<String, List<String>> adj = new HashMap<>();
        graph.get("nodes").forEach(n -> { indegree.put(n.get("id").asText(), 0); adj.put(n.get("id").asText(), new ArrayList<>()); });
        graph.get("edges").forEach(e -> {
            String s = e.get("from").get("node").asText();
            String d = e.get("to").get("node").asText();
            adj.get(s).add(d); indegree.put(d, indegree.get(d) + 1);
        });
        ArrayDeque<String> q = new ArrayDeque<>(); indegree.forEach((k, v) -> { if (v == 0) q.add(k); });
        List<String> order = new ArrayList<>();
        while (!q.isEmpty()) {
            String c = q.poll(); order.add(c);
            for (String n : adj.get(c)) { indegree.put(n, indegree.get(n) - 1); if (indegree.get(n) == 0) q.add(n); }
        }
        if (order.size() != indegree.size()) throw new IllegalArgumentException("Graph contains cycle");
        return order;
    }

    private void log(String nodeRunId, String stream, String line) {
        NodeLogEntity l = new NodeLogEntity(); l.setNodeRunId(nodeRunId); l.setStream(stream); l.setLine(line); nodeLogRepo.save(l);
    }
}
