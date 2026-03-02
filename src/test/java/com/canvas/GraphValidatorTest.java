package com.canvas;

import com.canvas.workflow.GraphValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class GraphValidatorTest {
    @Test
    void validateOk() throws Exception {
        String graph = """
                {
                  "nodes": [
                    {"id":"n1","type":"mysql_connection_ref","config":{"conn_id":"c1"}},
                    {"id":"n2","type":"mysql_query","config":{"sql":"SELECT 1"}}
                  ],
                  "edges": [
                    {"from":{"node":"n1","port":"conn_id"},"to":{"node":"n2","port":"conn_id"}}
                  ]
                }
                """;
        new GraphValidator().validate(new ObjectMapper().readTree(graph));
    }
}
