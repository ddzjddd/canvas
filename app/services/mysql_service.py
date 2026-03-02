import sqlite3
from app.core.crypto import decrypt_text


class MySQLService:
    """MVP service: executes SELECT using sqlite path in host field for local demo/testing."""

    def execute_select(self, conn, sql: str, max_rows: int = 1000):
        lowered = sql.strip().lower()
        if not lowered.startswith("select"):
            raise ValueError("Only SELECT is allowed in MVP")

        _ = decrypt_text(conn.password_enc)
        db = sqlite3.connect(conn.host)
        db.row_factory = sqlite3.Row
        cur = db.cursor()
        cur.execute(sql)
        rows = cur.fetchmany(max_rows)
        result = [dict(r) for r in rows]
        columns = list(result[0].keys()) if result else []
        cur.close()
        db.close()
        return result, columns
