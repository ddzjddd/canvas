import time


def run(node, resolved_inputs, mysql_service, db):
    start = time.time()
    conn_id = resolved_inputs.get("conn_id") or node["config"].get("conn_id")
    conn = db.mysql_conn_get(conn_id)
    sql = resolved_inputs.get("sql") or node["config"]["sql"]
    rows, columns = mysql_service.execute_select(conn, sql, int(node["config"].get("max_rows", 1000)))
    elapsed_ms = int((time.time() - start) * 1000)
    return {
        "rows": rows,
        "columns": columns,
        "row_count": len(rows),
        "elapsed_ms": elapsed_ms,
    }
