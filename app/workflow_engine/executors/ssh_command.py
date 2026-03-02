import time


def run(node, resolved_inputs, ssh_service, db):
    start = time.time()
    host_id = resolved_inputs.get("host_id") or node["config"].get("host_id")
    host = db.vps_host_get(host_id)
    cmd = resolved_inputs.get("cmd") or node["config"]["cmd"]
    stdout, stderr, exit_code = ssh_service.execute(host, cmd, timeout_s=float(node["config"].get("timeout_ms", 10000)) / 1000)
    elapsed_ms = int((time.time() - start) * 1000)
    return {
        "stdout": stdout,
        "stderr": stderr,
        "exit_code": exit_code,
        "elapsed_ms": elapsed_ms,
    }
