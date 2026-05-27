import json
import os
import sys

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
if ROOT_DIR not in sys.path:
    sys.path.insert(0, ROOT_DIR)

from app import app, init_db


def run_checks():
    init_db()
    client = app.test_client()

    email = "demo.user@example.com"
    password = "secret123"

    register = client.post(
        "/auth/register",
        json={"email": email, "password": password},
    )
    if register.status_code not in (201, 409):
        raise AssertionError(f"register status {register.status_code}: {register.data}")

    login = client.post(
        "/auth/login",
        json={"email": email, "password": password},
    )
    if login.status_code != 200:
        raise AssertionError(f"login status {login.status_code}: {login.data}")

    payload = json.loads(login.data)
    if payload.get("email") != email:
        raise AssertionError("login response email mismatch")

    token = payload.get("token")
    if not token:
        raise AssertionError("login response missing token")

    me = client.get("/auth/me", headers={"Authorization": f"Bearer {token}"})
    if me.status_code != 200:
        raise AssertionError(f"auth/me status {me.status_code}: {me.data}")

    bad_login = client.post(
        "/auth/login",
        json={"email": email, "password": "wrongpass"},
    )
    if bad_login.status_code != 401:
        raise AssertionError("invalid login should return 401")

    print("Auth verification: PASS")


if __name__ == "__main__":
    run_checks()
