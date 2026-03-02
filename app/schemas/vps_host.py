from datetime import datetime
from pydantic import BaseModel


class VPSHostCreate(BaseModel):
    name: str
    host: str
    port: int = 22
    username: str
    private_key: str
    known_hosts_policy: str = "accept-new"


class VPSHostOut(BaseModel):
    id: str
    name: str
    host: str
    port: int
    username: str
    known_hosts_policy: str
    host_masked: str
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
