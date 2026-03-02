from datetime import datetime
from pydantic import BaseModel


class RunCreate(BaseModel):
    inputs: dict = {}


class WorkflowRunOut(BaseModel):
    id: str
    workflow_id: str
    status: str
    inputs_json: dict
    started_at: datetime
    finished_at: datetime | None

    class Config:
        from_attributes = True
