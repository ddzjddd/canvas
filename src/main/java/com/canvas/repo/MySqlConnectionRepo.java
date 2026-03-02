package com.canvas.repo;

import com.canvas.domain.MySqlConnectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MySqlConnectionRepo extends JpaRepository<MySqlConnectionEntity, String> {}
