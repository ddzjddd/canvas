from app.workflow_engine.parser import validate_graph


def test_validate_graph_ok():
    graph = {
        "nodes": [
            {"id": "n1", "type": "mysql_connection_ref", "config": {"conn_id": "c1"}},
            {"id": "n2", "type": "mysql_query", "config": {"sql": "SELECT 1"}},
        ],
        "edges": [
            {"from": {"node": "n1", "port": "conn_id"}, "to": {"node": "n2", "port": "conn_id"}}
        ],
    }
    validate_graph(graph)
