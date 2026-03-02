package com.canvas.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vps_hosts")
public class VpsHostEntity {
    @Id
    private String id = UUID.randomUUID().toString();
    private String name;
    private String host;
    private Integer port;
    private String username;
    @Lob
    private String privateKeyEnc;
    private String knownHostsPolicy;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = Instant.now(); }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPrivateKeyEnc() { return privateKeyEnc; }
    public void setPrivateKeyEnc(String privateKeyEnc) { this.privateKeyEnc = privateKeyEnc; }
    public String getKnownHostsPolicy() { return knownHostsPolicy; }
    public void setKnownHostsPolicy(String knownHostsPolicy) { this.knownHostsPolicy = knownHostsPolicy; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
