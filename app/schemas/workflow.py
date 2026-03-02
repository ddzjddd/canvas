from datetime import datetime
from pydantic import BaseModel


class WorkflowCreate(BaseModel):
    name: str
    graph_json: dict


class WorkflowOut(BaseModel):
    id: str
    name: str
    graph_json: dict
    version: int
    created_by: str
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
