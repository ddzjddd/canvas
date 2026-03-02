package com.canvas.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "node_runs")
public class NodeRunEntity {
    @Id
    private String id = UUID.randomUUID().toString();
    private String runId;
    private String nodeId;
    private String status;
    private Instant startedAt;
    private Instant finishedAt;
    @Lob
    private String outputsJson;
    @Lob
    private String error;

    public String getId() { return id; }
    public String getRunId() { return runId; }
    public void setRunId(String runId) { this.runId = runId; }
    public String getNodeId() { return nodeId; }
    public void setNodeId(String nodeId) { this.nodeId = nodeId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public String getOutputsJson() { return outputsJson; }
    public void setOutputsJson(String outputsJson) { this.outputsJson = outputsJson; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
