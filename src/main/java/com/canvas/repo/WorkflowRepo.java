package com.canvas.repo;

import com.canvas.domain.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRepo extends JpaRepository<WorkflowEntity, String> {}
