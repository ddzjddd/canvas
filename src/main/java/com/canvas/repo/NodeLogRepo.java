package com.canvas.repo;

import com.canvas.domain.NodeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NodeLogRepo extends JpaRepository<NodeLogEntity, Long> {
    List<NodeLogEntity> findByIdGreaterThanOrderByIdAsc(Long id);
    List<NodeLogEntity> findByNodeRunIdInAndIdGreaterThanOrderByIdAsc(List<String> nodeRunIds, Long id);
}
