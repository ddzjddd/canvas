from app.models.run import NodeLog


def append_log(db, node_run_id: str, stream: str, line: str):
    db.add(NodeLog(node_run_id=node_run_id, stream=stream, line=line))
    db.commit()
