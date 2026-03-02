from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.crypto import encrypt_text
from app.core.db import get_db
from app.models.mysql_conn import MySQLConnection
from app.schemas.mysql_conn import MySQLConnectionCreate

router = APIRouter(prefix="/api/mysql-connections", tags=["mysql-connections"])


def to_out(conn: MySQLConnection):
    return {
        "id": conn.id,
        "name": conn.name,
        "host": conn.host,
        "port": conn.port,
        "database": conn.database,
        "username": conn.username,
        "dsn_masked": f"{conn.username}@{conn.host}:{conn.port}/{conn.database}",
        "created_at": conn.created_at,
        "updated_at": conn.updated_at,
    }


@router.post("")
def create_connection(payload: MySQLConnectionCreate, db: Session = Depends(get_db)):
    conn = MySQLConnection(**payload.model_dump(exclude={"password"}), password_enc=encrypt_text(payload.password))
    db.add(conn)
    db.commit()
    db.refresh(conn)
    return to_out(conn)


@router.get("")
def list_connections(db: Session = Depends(get_db)):
    return [to_out(c) for c in db.query(MySQLConnection).all()]


@router.get("/{conn_id}")
def get_connection(conn_id: str, db: Session = Depends(get_db)):
    conn = db.query(MySQLConnection).filter(MySQLConnection.id == conn_id).first()
    if not conn:
        raise HTTPException(404, "Not found")
    return to_out(conn)
