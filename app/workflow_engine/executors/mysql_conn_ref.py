
def run(node, db):
    conn_id = node["config"]["conn_id"]
    conn = db.mysql_conn_get(conn_id)
    return {
        "conn_id": conn.id,
        "dsn_masked": f"{conn.username}@{conn.host}:{conn.port}/{conn.database}",
    }
