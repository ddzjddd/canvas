from pydantic import BaseModel
import os


class Settings(BaseModel):
    database_url: str = os.getenv("DATABASE_URL", "sqlite:///./canvas.db")
    master_key: str = os.getenv("CANVAS_MASTER_KEY", "dev-master-key-change-me")


settings = Settings()
