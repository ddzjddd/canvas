from datetime import datetime
from pydantic import BaseModel


class MySQLConnectionCreate(BaseModel):
    name: str
    host: str
    port: int = 3306
    database: str
    username: str
    password: str


class MySQLConnectionOut(BaseModel):
    id: str
    name: str
    host: str
    port: int
    database: str
    username: str
    dsn_masked: str
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True
