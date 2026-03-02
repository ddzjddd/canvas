
def run(node, db):
    host_id = node["config"]["host_id"]
    host = db.vps_host_get(host_id)
    return {
        "host_id": host.id,
        "host_masked": f"{host.username}@{host.host}:{host.port}",
    }
