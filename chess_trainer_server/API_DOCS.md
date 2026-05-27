# Chess Trainer Server API

Base URL

- Local: `http://localhost:5000/`
- Android emulator: `http://10.0.2.2:5000/`
- Physical device: `http://<host-ip>:5000/`

Notes

- All responses are JSON and include `Content-Type: application/json`.
- Errors return `{"error": "<message>"}` with an appropriate status code.
- CORS is enabled for all endpoints.

## Health

GET `/health`

Response 200

```json
{
  "status": "ok"
}
```

Curl

```bash
curl -s http://localhost:5000/health
```

## Authentication

POST `/auth/register`

Request body

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

Response 201

```json
{
  "id": 1,
  "email": "user@example.com",
  "token": "<jwt>"
}
```

Response 409

```json
{
  "error": "Email already registered"
}
```

Curl

```bash
curl -s -X POST http://localhost:5000/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"secret123"}'
```

POST `/auth/login`

Request body

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

Response 200

```json
{
  "id": 1,
  "email": "user@example.com",
  "token": "<jwt>"
}
```

Response 401

```json
{
  "error": "Invalid email or password"
}
```

Curl

```bash
curl -s -X POST http://localhost:5000/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"user@example.com","password":"secret123"}'
```

GET `/auth/me`

Headers

```
Authorization: Bearer <token>
```

Response 200

```json
{
  "id": "1",
  "email": "user@example.com"
}
```

Response 401

```json
{
  "error": "Invalid token"
}
```

Curl

```bash
curl -s http://localhost:5000/auth/me \
  -H 'Authorization: Bearer <token>'
```

## Puzzles

GET `/puzzles`

Response 200

```json
[
  {
    "id": "puzzle-001",
    "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
    "solution": ["e2e4"],
    "description": "Open with the king's pawn.",
    "difficulty": "easy"
  }
]
```

Curl

```bash
curl -s http://localhost:5000/puzzles
```

GET `/puzzles/<id>`

Response 200

```json
{
  "id": "puzzle-001",
  "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
  "solution": ["e2e4"],
  "description": "Open with the king's pawn.",
  "difficulty": "easy"
}
```

Response 404

```json
{
  "error": "Puzzle not found"
}
```

Curl

```bash
curl -s http://localhost:5000/puzzles/puzzle-001
curl -s http://localhost:5000/puzzles/invalid-id
```

## Openings

GET `/openings`

Response 200

```json
[
  {
    "id": "opening-001",
    "name": "Ruy Lopez",
    "moves": ["e2e4", "e7e5", "g1f3", "b8c6", "f1b5"],
    "description": "Classical development targeting the e5 pawn."
  }
]
```

Curl

```bash
curl -s http://localhost:5000/openings
```

GET `/openings/<id>`

Response 200

```json
{
  "id": "opening-001",
  "name": "Ruy Lopez",
  "moves": ["e2e4", "e7e5", "g1f3", "b8c6", "f1b5"],
  "description": "Classical development targeting the e5 pawn."
}
```

Response 404

```json
{
  "error": "Opening not found"
}
```

Curl

```bash
curl -s http://localhost:5000/openings/opening-001
curl -s http://localhost:5000/openings/invalid-id
```

## AI Move (stub)

POST `/ai-move`

Request body

```json
{
  "fen": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
}
```

Response 200

```json
{
  "move": "e2e4"
}
```

Response 400

```json
{
  "error": "Missing or invalid 'fen' field"
}
```

Curl

```bash
curl -s -X POST http://localhost:5000/ai-move \
  -H 'Content-Type: application/json' \
  -d '{"fen":"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"}'
```

## Status Codes

- 200 OK: successful request
- 201 Created: successful resource creation
- 400 Bad Request: missing or invalid input
- 401 Unauthorized: invalid credentials
- 404 Not Found: item does not exist
- 409 Conflict: duplicate resource
- 500 Internal Server Error: data load failures
