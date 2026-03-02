from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.db import get_db
from app.models.run import NodeRun, WorkflowRun, NodeLog
from app.models.workflow import Workflow
from app.schemas.run import RunCreate
from app.services.mysql_service import MySQLService
from app.services.ssh_service import SSHService
from app.workflow_engine.orchestrator import run_workflow

router = APIRouter(prefix="/api", tags=["runs"])


@router.post("/workflows/{workflow_id}/runs")
def create_run(workflow_id: str, payload: RunCreate, db: Session = Depends(get_db)):
    wf = db.query(Workflow).filter(Workflow.id == workflow_id).first()
    if not wf:
        raise HTTPException(404, "workflow not found")
    run_id = run_workflow(db, wf, payload.inputs, mysql_service=MySQLService(), ssh_service=SSHService())
    return {"run_id": run_id}


@router.get("/runs/{run_id}")
def get_run(run_id: str, db: Session = Depends(get_db)):
    run = db.query(WorkflowRun).filter(WorkflowRun.id == run_id).first()
    if not run:
        raise HTTPException(404, "run not found")
    nodes = db.query(NodeRun).filter(NodeRun.run_id == run_id).all()
    return {"run": run, "node_runs": nodes}


@router.get("/runs/{run_id}/nodes/{node_run_id}")
def get_node_run(run_id: str, node_run_id: str, db: Session = Depends(get_db)):
    node = db.query(NodeRun).filter(NodeRun.run_id == run_id, NodeRun.id == node_run_id).first()
    if not node:
        raise HTTPException(404, "node run not found")
    return node


@router.get("/runs/{run_id}/events")
def get_events(run_id: str, last_id: int = 0, db: Session = Depends(get_db)):
    logs = (
        db.query(NodeLog)
        .join(NodeRun, NodeRun.id == NodeLog.node_run_id)
        .filter(NodeRun.run_id == run_id, NodeLog.id > last_id)
        .order_by(NodeLog.id.asc())
        .all()
    )
    return [
        {"id": l.id, "node_run_id": l.node_run_id, "stream": l.stream, "line": l.line, "ts": l.ts.isoformat()} for l in logs
    ]
