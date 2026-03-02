package com.canvas.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "node_logs")
public class NodeLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nodeRunId;
    private Instant ts = Instant.now();
    private String stream;
    @Lob
    private String line;

    public Long getId() { return id; }
    public String getNodeRunId() { return nodeRunId; }
    public void setNodeRunId(String nodeRunId) { this.nodeRunId = nodeRunId; }
    public Instant getTs() { return ts; }
    public String getStream() { return stream; }
    public void setStream(String stream) { this.stream = stream; }
    public String getLine() { return line; }
    public void setLine(String line) { this.line = line; }
}
