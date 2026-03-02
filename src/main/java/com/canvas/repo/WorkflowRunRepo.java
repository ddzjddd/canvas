package com.canvas.repo;

import com.canvas.domain.WorkflowRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRunRepo extends JpaRepository<WorkflowRunEntity, String> {}
