# Chess Trainer Server

Minimal Flask backend for puzzles and openings data.

## Requirements

- Python 3.12+

## Setup

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

If you prefer `pyproject.toml` tooling, install dependencies with your usual workflow.

## Run

```bash
python app.py
```

Demo script (creates venv if missing, installs deps, then starts the server):

```bash
chmod +x start_demo_server.sh
./start_demo_server.sh
```

## Configuration

- Default port is `5000`.
- Override port with `PORT`.
- SQLite database path defaults to `data/app.db` (override with `DB_PATH`).
- JWT secret defaults to `dev-secret-change-me-please-32chars` (override with `JWT_SECRET`).
- JWT expiration defaults to 1440 minutes (override with `JWT_EXP_MINUTES`).

```bash
PORT=5000 python app.py
DB_PATH=/tmp/chess_trainer.db python app.py
JWT_SECRET=change-me JWT_EXP_MINUTES=60 python app.py
```

## Emulator and Device URLs

- Android emulator base URL: `http://10.0.2.2:5000/`
- Physical device base URL: `http://<host-ip>:5000/`

Find your host IP (example):

```bash
hostname -I
```

## Endpoints

- `GET /health`
- `GET /puzzles`
- `GET /puzzles/<id>`
- `GET /openings`
- `GET /openings/<id>`
- `POST /ai-move` with JSON body `{"fen": "..."}` (stubbed response)
- `POST /auth/register` with JSON body `{"email": "...", "password": "..."}`
- `POST /auth/login` with JSON body `{"email": "...", "password": "..."}`
- `GET /auth/me` requires `Authorization: Bearer <token>`

## Verification

```bash
curl -s http://localhost:5000/health
curl -s http://localhost:5000/puzzles | head -c 200
curl -s http://localhost:5000/openings | head -c 200
curl -s -X POST http://localhost:5000/ai-move \
  -H 'Content-Type: application/json' \
  -d '{"fen":"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"}'
```

Auth check (uses Flask test client):

```bash
python scripts/verify_auth.py
```

## Data

JSON datasets live in `data/puzzles.json` and `data/openings.json`.

## API Documentation

See `API_DOCS.md` for full request/response examples.

