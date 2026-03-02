import subprocess
import tempfile
from app.core.crypto import decrypt_text


class SSHService:
    def execute(self, host, cmd: str, timeout_s: float = 10.0):
        key = decrypt_text(host.private_key_enc)
        with tempfile.NamedTemporaryFile("w", delete=True) as f:
            f.write(key)
            f.flush()
            proc = subprocess.run(
                [
                    "ssh",
                    "-i",
                    f.name,
                    "-p",
                    str(host.port),
                    "-o",
                    "StrictHostKeyChecking=accept-new" if host.known_hosts_policy == "accept-new" else "StrictHostKeyChecking=yes",
                    f"{host.username}@{host.host}",
                    cmd,
                ],
                capture_output=True,
                text=True,
                timeout=timeout_s,
            )
            return proc.stdout, proc.stderr, proc.returncode
