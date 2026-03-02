package com.canvas.service;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class MySqlExecutionService {
    public Map<String, Object> executeSelect(String jdbcUrl, String username, String password, String sql, int maxRows) throws Exception {
        if (!sql.trim().toLowerCase(Locale.ROOT).startsWith("select")) throw new IllegalArgumentException("Only SELECT allowed in MVP");
        long st = System.currentTimeMillis();
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setMaxRows(maxRows);
            try (ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> rows = new ArrayList<>();
                ResultSetMetaData md = rs.getMetaData();
                int cc = md.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    for (int i = 1; i <= cc; i++) r.put(md.getColumnLabel(i), rs.getObject(i));
                    rows.add(r);
                }
                List<String> columns = new ArrayList<>();
                for (int i = 1; i <= cc; i++) columns.add(md.getColumnLabel(i));
                return Map.of("rows", rows, "columns", columns, "row_count", rows.size(), "elapsed_ms", System.currentTimeMillis() - st);
            }
        }
    }
}
