from flask import Flask, jsonify, request
from flask_cors import CORS
import json
import os
import random
import re
import sqlite3
from datetime import datetime, timezone, timedelta
import jwt
from werkzeug.security import check_password_hash, generate_password_hash

app = Flask(__name__)
CORS(app)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATA_DIR = os.path.join(BASE_DIR, "data")
PUZZLES_PATH = os.path.join(DATA_DIR, "puzzles.json")
OPENINGS_PATH = os.path.join(DATA_DIR, "openings.json")
DB_PATH = os.getenv("DB_PATH", os.path.join(DATA_DIR, "app.db"))
JWT_SECRET = os.getenv("JWT_SECRET", "dev-secret-change-me-please-32chars")
JWT_ALGORITHM = "HS256"
JWT_EXP_MINUTES = int(os.getenv("JWT_EXP_MINUTES", "1440"))

EMAIL_RE = re.compile(r"^[^@\s]+@[^@\s]+\.[^@\s]+$")


def load_json(path: str):
    with open(path, "r", encoding="utf-8") as handle:
        return json.load(handle)


def json_error(message: str, status_code: int):
    response = jsonify({"error": message})
    response.status_code = status_code
    return response


def create_access_token(user_id: int, email: str):
    expires_at = datetime.now(timezone.utc) + timedelta(minutes=JWT_EXP_MINUTES)
    payload = {
        "sub": str(user_id),
        "email": email,
        "exp": expires_at,
    }
    return jwt.encode(payload, JWT_SECRET, algorithm=JWT_ALGORITHM)


def get_bearer_token():
    auth_header = request.headers.get("Authorization", "")
    if not auth_header.startswith("Bearer "):
        return None
    return auth_header.replace("Bearer ", "", 1).strip()


def decode_access_token(token: str):
    return jwt.decode(token, JWT_SECRET, algorithms=[JWT_ALGORITHM])


def get_db_connection():
    connection = sqlite3.connect(DB_PATH)
    connection.row_factory = sqlite3.Row
    return connection


def init_db():
    os.makedirs(DATA_DIR, exist_ok=True)
    with get_db_connection() as connection:
        connection.execute(
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                created_at TEXT NOT NULL
            )
            """
        )
        connection.commit()


def validate_auth_payload(payload):
    email = payload.get("email")
    password = payload.get("password")

    if not isinstance(email, str) or not EMAIL_RE.match(email.strip()):
        return None, None, "Invalid or missing email"
    if not isinstance(password, str) or len(password.strip()) < 8:
        return None, None, "Password must be at least 8 characters"
    return email.strip().lower(), password, None


@app.route("/health", methods=["GET"])
def health_check():
    return jsonify({"status": "ok"})


@app.route("/puzzles", methods=["GET"])
def get_puzzles():
    try:
        puzzles = load_json(PUZZLES_PATH)
    except (OSError, json.JSONDecodeError) as exc:
        return json_error(f"Failed to load puzzles: {exc}", 500)
    return jsonify(puzzles)


@app.route("/puzzles/<puzzle_id>", methods=["GET"])
def get_puzzle_by_id(puzzle_id: str):
    try:
        puzzles = load_json(PUZZLES_PATH)
    except (OSError, json.JSONDecodeError) as exc:
        return json_error(f"Failed to load puzzles: {exc}", 500)

    for puzzle in puzzles:
        if puzzle.get("id") == puzzle_id:
            return jsonify(puzzle)

    return json_error("Puzzle not found", 404)


@app.route("/openings", methods=["GET"])
def get_openings():
    try:
        openings = load_json(OPENINGS_PATH)
    except (OSError, json.JSONDecodeError) as exc:
        return json_error(f"Failed to load openings: {exc}", 500)
    return jsonify(openings)


@app.route("/openings/<opening_id>", methods=["GET"])
def get_opening_by_id(opening_id: str):
    try:
        openings = load_json(OPENINGS_PATH)
    except (OSError, json.JSONDecodeError) as exc:
        return json_error(f"Failed to load openings: {exc}", 500)

    for opening in openings:
        if opening.get("id") == opening_id:
            return jsonify(opening)

    return json_error("Opening not found", 404)


@app.route("/ai-move", methods=["POST"])
def ai_move_stub():
    payload = request.get_json(silent=True) or {}
    fen = payload.get("fen")
    if not isinstance(fen, str) or not fen.strip():
        return json_error("Missing or invalid 'fen' field", 400)

    # Stub: return a safe placeholder move while AI logic lives on-device.
    move = random.choice(["e2e4", "d2d4", "g1f3"])
    return jsonify({"move": move})


@app.route("/auth/register", methods=["POST"])
def register():
    payload = request.get_json(silent=True) or {}
    email, password, error_message = validate_auth_payload(payload)
    if error_message:
        return json_error(error_message, 400)

    password_hash = generate_password_hash(password)
    created_at = datetime.now(timezone.utc).isoformat()

    try:
        with get_db_connection() as connection:
            cursor = connection.execute(
                "INSERT INTO users (email, password_hash, created_at) VALUES (?, ?, ?)",
                (email, password_hash, created_at),
            )
            connection.commit()
    except sqlite3.IntegrityError:
        return json_error("Email already registered", 409)
    except sqlite3.Error as exc:
        return json_error(f"Database error: {exc}", 500)

    token = create_access_token(cursor.lastrowid, email)
    return jsonify({"id": cursor.lastrowid, "email": email, "token": token}), 201


@app.route("/auth/login", methods=["POST"])
def login():
    payload = request.get_json(silent=True) or {}
    email, password, error_message = validate_auth_payload(payload)
    if error_message:
        return json_error(error_message, 400)

    try:
        with get_db_connection() as connection:
            cursor = connection.execute(
                "SELECT id, email, password_hash FROM users WHERE email = ?",
                (email,),
            )
            user = cursor.fetchone()
    except sqlite3.Error as exc:
        return json_error(f"Database error: {exc}", 500)

    if not user or not check_password_hash(user["password_hash"], password):
        return json_error("Invalid email or password", 401)

    token = create_access_token(user["id"], user["email"])
    return jsonify({"id": user["id"], "email": user["email"], "token": token})


@app.route("/auth/me", methods=["GET"])
def auth_me():
    token = get_bearer_token()
    if not token:
        return json_error("Missing bearer token", 401)

    try:
        payload = decode_access_token(token)
    except jwt.ExpiredSignatureError:
        return json_error("Token expired", 401)
    except jwt.InvalidTokenError:
        return json_error("Invalid token", 401)

    return jsonify({"id": payload.get("sub"), "email": payload.get("email")})


if __name__ == '__main__':
    init_db()
    port = int(os.getenv("PORT", "5000"))
    app.run(host="0.0.0.0", port=port)
