from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.db import get_db
from app.models.workflow import Workflow
from app.schemas.workflow import WorkflowCreate
from app.workflow_engine.parser import ValidationError, validate_graph

router = APIRouter(prefix="/api/workflows", tags=["workflows"])


@router.post("")
def create_workflow(payload: WorkflowCreate, db: Session = Depends(get_db)):
    validate_graph(payload.graph_json)
    wf = Workflow(name=payload.name, graph_json=payload.graph_json)
    db.add(wf)
    db.commit()
    db.refresh(wf)
    return wf


@router.get("")
def list_workflows(db: Session = Depends(get_db)):
    return db.query(Workflow).all()


@router.get("/{workflow_id}")
def get_workflow(workflow_id: str, db: Session = Depends(get_db)):
    wf = db.query(Workflow).filter(Workflow.id == workflow_id).first()
    if not wf:
        raise HTTPException(404, "Not found")
    return wf


@router.put("/{workflow_id}")
def update_workflow(workflow_id: str, payload: WorkflowCreate, db: Session = Depends(get_db)):
    wf = db.query(Workflow).filter(Workflow.id == workflow_id).first()
    if not wf:
        raise HTTPException(404, "Not found")
    validate_graph(payload.graph_json)
    wf.name = payload.name
    wf.graph_json = payload.graph_json
    wf.version += 1
    db.commit()
    db.refresh(wf)
    return wf


@router.post("/{workflow_id}/validate")
def validate_workflow(workflow_id: str, db: Session = Depends(get_db)):
    wf = db.query(Workflow).filter(Workflow.id == workflow_id).first()
    if not wf:
        raise HTTPException(404, "Not found")
    try:
        validate_graph(wf.graph_json)
        return {"valid": True}
    except ValidationError as exc:
        return {"valid": False, "error": str(exc)}
