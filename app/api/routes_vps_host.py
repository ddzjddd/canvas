from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

from app.core.crypto import encrypt_text
from app.core.db import get_db
from app.models.vps_host import VPSHost
from app.schemas.vps_host import VPSHostCreate

router = APIRouter(prefix="/api/vps-hosts", tags=["vps-hosts"])


def to_out(host: VPSHost):
    return {
        "id": host.id,
        "name": host.name,
        "host": host.host,
        "port": host.port,
        "username": host.username,
        "known_hosts_policy": host.known_hosts_policy,
        "host_masked": f"{host.username}@{host.host}:{host.port}",
        "created_at": host.created_at,
        "updated_at": host.updated_at,
    }


@router.post("")
def create_host(payload: VPSHostCreate, db: Session = Depends(get_db)):
    host = VPSHost(**payload.model_dump(exclude={"private_key"}), private_key_enc=encrypt_text(payload.private_key))
    db.add(host)
    db.commit()
    db.refresh(host)
    return to_out(host)


@router.get("")
def list_hosts(db: Session = Depends(get_db)):
    return [to_out(h) for h in db.query(VPSHost).all()]


@router.get("/{host_id}")
def get_host(host_id: str, db: Session = Depends(get_db)):
    host = db.query(VPSHost).filter(VPSHost.id == host_id).first()
    if not host:
        raise HTTPException(404, "Not found")
    return to_out(host)
