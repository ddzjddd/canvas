from datetime import datetime

from app.models.run import NodeRun, WorkflowRun
from app.workflow_engine.context import build_template_context
from app.workflow_engine.parser import topological_sort
from app.workflow_engine.renderer import render_template
from app.workflow_engine.executors import mysql_conn_ref, mysql_query, ssh_command, vps_host_ref
from app.storage.logs import append_log


class DBFacade:
    def __init__(self, db):
        self.db = db

    def mysql_conn_get(self, conn_id: str):
        from app.models.mysql_conn import MySQLConnection

        conn = self.db.query(MySQLConnection).filter(MySQLConnection.id == conn_id).first()
        if not conn:
            raise ValueError(f"MySQL connection not found: {conn_id}")
        return conn

    def vps_host_get(self, host_id: str):
        from app.models.vps_host import VPSHost

        host = self.db.query(VPSHost).filter(VPSHost.id == host_id).first()
        if not host:
            raise ValueError(f"VPS host not found: {host_id}")
        return host


def run_workflow(db, workflow, run_inputs: dict, mysql_service, ssh_service):
    graph = workflow.graph_json
    node_map = {n["id"]: n for n in graph.get("nodes", [])}
    edges = graph.get("edges", [])
    order = topological_sort(graph)

    run = WorkflowRun(workflow_id=workflow.id, status="RUNNING", inputs_json=run_inputs)
    db.add(run)
    db.commit()
    db.refresh(run)

    fac = DBFacade(db)
    outputs = {}
    edge_index = {}
    for e in edges:
        edge_index.setdefault(e["to"]["node"], []).append(e)

    try:
        for node_id in order:
            node = node_map[node_id]
            nr = NodeRun(run_id=run.id, node_id=node_id, status="RUNNING", started_at=datetime.utcnow())
            db.add(nr)
            db.commit()
            db.refresh(nr)

            inputs = {}
            for edge in edge_index.get(node_id, []):
                src = edge["from"]
                dst = edge["to"]
                inputs[dst["port"]] = outputs[src["node"]][src["port"]]

            rendered = render_template({**node.get("config", {}), **inputs}, build_template_context(run_inputs, outputs))

            ntype = node["type"]
            if ntype == "mysql_connection_ref":
                out = mysql_conn_ref.run(node, fac)
            elif ntype == "vps_host_ref":
                out = vps_host_ref.run(node, fac)
            elif ntype == "mysql_query":
                out = mysql_query.run(node, rendered, mysql_service, fac)
            elif ntype == "ssh_command":
                out = ssh_command.run(node, rendered, ssh_service, fac)
            else:
                raise ValueError(f"Unsupported node type: {ntype}")

            outputs[node_id] = out
            nr.status = "SUCCESS"
            nr.outputs_json = out
            nr.finished_at = datetime.utcnow()
            db.commit()
            append_log(db, nr.id, "system", f"Node {node_id} success")

        run.status = "SUCCESS"
        run.finished_at = datetime.utcnow()
        db.commit()
    except Exception as exc:
        run.status = "FAILED"
        run.finished_at = datetime.utcnow()
        db.commit()
        append_log(db, nr.id, "stderr", str(exc))
        nr.status = "FAILED"
        nr.error = str(exc)
        nr.finished_at = datetime.utcnow()
        db.commit()

    return run.id
