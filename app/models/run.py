import uuid
from datetime import datetime
from sqlalchemy import String, DateTime, JSON, ForeignKey, Text, Integer
from sqlalchemy.orm import Mapped, mapped_column

from app.core.db import Base


class WorkflowRun(Base):
    __tablename__ = "workflow_runs"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    workflow_id: Mapped[str] = mapped_column(String(36), ForeignKey("workflows.id"), nullable=False)
    status: Mapped[str] = mapped_column(String(20), default="PENDING")
    inputs_json: Mapped[dict] = mapped_column(JSON, default=dict)
    started_at: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    finished_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)


class NodeRun(Base):
    __tablename__ = "node_runs"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    run_id: Mapped[str] = mapped_column(String(36), ForeignKey("workflow_runs.id"), nullable=False)
    node_id: Mapped[str] = mapped_column(String(100), nullable=False)
    status: Mapped[str] = mapped_column(String(20), default="PENDING")
    started_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    finished_at: Mapped[datetime | None] = mapped_column(DateTime, nullable=True)
    outputs_json: Mapped[dict | None] = mapped_column(JSON, nullable=True)
    error: Mapped[str | None] = mapped_column(Text, nullable=True)


class NodeLog(Base):
    __tablename__ = "node_logs"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    node_run_id: Mapped[str] = mapped_column(String(36), ForeignKey("node_runs.id"), nullable=False)
    ts: Mapped[datetime] = mapped_column(DateTime, default=datetime.utcnow)
    stream: Mapped[str] = mapped_column(String(20), default="system")
    line: Mapped[str] = mapped_column(Text, nullable=False)
