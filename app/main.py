from fastapi import FastAPI

from app.core.db import Base, engine
from app.api.routes_mysql_conn import router as mysql_router
from app.api.routes_vps_host import router as vps_router
from app.api.routes_workflow import router as wf_router
from app.api.routes_run import router as run_router

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Canvas Workflow API")
app.include_router(mysql_router)
app.include_router(vps_router)
app.include_router(wf_router)
app.include_router(run_router)


@app.get("/healthz")
def healthz():
    return {"ok": True}
