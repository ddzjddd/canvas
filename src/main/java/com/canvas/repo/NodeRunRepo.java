package com.canvas.repo;

import com.canvas.domain.NodeRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NodeRunRepo extends JpaRepository<NodeRunEntity, String> {
    List<NodeRunEntity> findByRunId(String runId);
}
