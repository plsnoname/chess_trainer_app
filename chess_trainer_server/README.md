# Chess Trainer Server

A Flask backend for the Chess Trainer app that provides authentication, chess content APIs, and AI move stubs.

## Overview

The server exposes REST endpoints used by the Android client for:

- account registration and login
- JWT-based session validation
- puzzle and opening retrieval
- AI move requests

It stores users in SQLite and serves static chess datasets from JSON files.

## Architecture and Technology Stack

### Architecture Pattern

- Flask application with route-based controllers
- SQLite persistence layer for authentication
- Stateless JWT authentication for protected endpoints

### Technology Stack

- **Language**: Python 3.12+
- **Framework**: Flask
- **Auth**: JWT (PyJWT) + Werkzeug password hashing
- **Database**: SQLite
- **CORS**: flask-cors

### Directory Structure

```text
chess_trainer_server/
  app.py                - Flask app and API routes
  requirements.txt      - Python dependencies
  start_demo_server.sh  - Convenience startup script
  scripts/
    verify_auth.py      - Auth flow verification script
  data/
    app.db              - SQLite database (created at runtime)
    puzzles.json        - Puzzle dataset
    openings.json       - Opening dataset
```

## Core Features

### 1. Authentication and Session Handling

- `POST /auth/register` creates a user with hashed password storage
- `POST /auth/login` validates credentials and returns JWT token
- `GET /auth/me` validates bearer token and returns current user identity

### 2. Chess Content APIs

- `GET /puzzles` and `GET /puzzles/<id>`
- `GET /openings` and `GET /openings/<id>`
- Data is served from JSON files in `data/`

### 3. AI Move Endpoint

- `POST /ai-move` accepts `{"fen":"..."}` and returns a legal placeholder move
- Endpoint is intentionally stubbed while game AI logic remains client-side

### 4. Health and Error Handling

- `GET /health` for service status
- Consistent JSON error responses with proper HTTP status codes

## API Endpoints

- `GET /health`
- `GET /puzzles`
- `GET /puzzles/<id>`
- `GET /openings`
- `GET /openings/<id>`
- `POST /ai-move` body: `{"fen":"..."}`
- `POST /auth/register` body: `{"email":"...","password":"..."}`
- `POST /auth/login` body: `{"email":"...","password":"..."}`
- `GET /auth/me` header: `Authorization: Bearer <token>`

See `API_DOCS.md` for full request and response examples.

## Building and Running

### Prerequisites

- Python 3.12+

### Setup

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

### Run

```bash
python app.py
```

### Convenience Script

```bash
chmod +x start_demo_server.sh
./start_demo_server.sh
```

## Configuration

Environment variables:

- `PORT` (default: `5000`)
- `DB_PATH` (default: `data/app.db`)
- `JWT_SECRET` (default development value in code)
- `JWT_EXP_MINUTES` (default: `1440`)

Examples:

```bash
PORT=5000 python app.py
DB_PATH=/tmp/chess_trainer.db python app.py
JWT_SECRET=change-me JWT_EXP_MINUTES=60 python app.py
```

## Client Connectivity

- Android emulator: `http://10.0.2.2:5000/`
- Physical device on Wi-Fi: `http://<host-ip>:5000/`

Find host IP:

```bash
hostname -I
```

## Verification and Testing

Quick endpoint checks:

```bash
curl -s http://localhost:5000/health
curl -s http://localhost:5000/puzzles | head -c 200
curl -s http://localhost:5000/openings | head -c 200
curl -s -X POST http://localhost:5000/ai-move \
  -H 'Content-Type: application/json' \
  -d '{"fen":"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"}'
```

Auth flow verification:

```bash
python scripts/verify_auth.py
```
