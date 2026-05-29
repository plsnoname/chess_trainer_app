# Chess Trainer App — Server Development Plan (Detailed)

> Platform: Python Flask
> Architecture: REST API + SQLite + JWT
> This plan uses the current project status as the baseline.

---

## 0) Baseline Snapshot (What Exists Now)

**Implemented**
- `GET /health`, `GET /puzzles`, `GET /openings`, `GET /puzzles/<id>`, `GET /openings/<id>`.
- `POST /ai-move` stub.
- Auth endpoints: `POST /auth/register`, `POST /auth/login`, `GET /auth/me`.
- SQLite users table + JWT token issuance.
- Dataset JSONs populated with puzzles and openings.

**Missing vs required scope**
- Tighten auth validation and error contract to match client expectations.
- Confirm response shapes are stable for Android auth-first flow.
- Add explicit contracts for error messages and field validation failures.

---

## 1) Auth API (Login + Register)

### 1.1 Register
**Endpoint**
- `POST /auth/register` with `{ email, password }`.
**Behavior**
- Validate email format and password length >= 8.
- Store user and return `{ id, email, token }`.
- Return 409 for existing email and 400 for invalid payload.

**Verification gate**
- New user registers successfully; duplicate email returns 409.

### 1.2 Login
**Endpoint**
- `POST /auth/login` with `{ email, password }`.
**Behavior**
- Return `{ id, email, token }` on success.
- Return 401 for invalid credentials and 400 for invalid payload.

**Verification gate**
- Invalid credentials return 401.

### 1.3 Current user
**Endpoint**
- `GET /auth/me` with `Authorization: Bearer <token>`.

**Verification gate**
- Valid token returns user id and email.

---

## 2) Data Endpoints (Puzzles/Openings)

### 2.1 Puzzles
- `GET /puzzles`
- `GET /puzzles/<id>`

### 2.2 Openings
- `GET /openings`
- `GET /openings/<id>`

**Verification gate**
- Returns JSON arrays, 200 on success, 404 if not found.

---

## 3) AI Move Stub (Client Compatibility)

**Endpoint**
- `POST /ai-move` with `{ fen }`.
**Behavior**
- Return `{ move }` (stubbed) to keep client compatible.

**Verification gate**
- Missing `fen` returns 400.

---

## 4) Error Contract (Uniform JSON)

**Goal:** Uniform error responses across all endpoints.

**Format**
```
{ "error": "message" }
```

**Verification gate**
- 400, 401, 404, 409, 500 all return JSON with `error` key.

---

## 5) CORS + Config

**Tasks**
- Keep CORS enabled for Android client.
- Support env vars: `PORT`, `DB_PATH`, `JWT_SECRET`, `JWT_EXP_MINUTES`.

**Verification gate**
- Server runs with defaults and with env overrides.

---

## 6) End-to-End Server Test Checklist

1. `GET /health` returns ok.
2. Register, login, and `GET /auth/me` with token.
3. `GET /puzzles` and `GET /openings` return arrays.
4. `POST /ai-move` returns a move.

---

## Done Criteria
- Auth endpoints are stable and match the client login/register flow.
- Data endpoints return correct JSON for puzzles/openings.
- Error responses are consistent across endpoints.
