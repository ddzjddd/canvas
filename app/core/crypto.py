import base64
import hashlib
from cryptography.fernet import Fernet

from app.core.config import settings


def _derive_key(master_key: str) -> bytes:
    digest = hashlib.sha256(master_key.encode("utf-8")).digest()
    return base64.urlsafe_b64encode(digest)


fernet = Fernet(_derive_key(settings.master_key))


def encrypt_text(value: str) -> str:
    return fernet.encrypt(value.encode("utf-8")).decode("utf-8")


def decrypt_text(value: str) -> str:
    return fernet.decrypt(value.encode("utf-8")).decode("utf-8")
