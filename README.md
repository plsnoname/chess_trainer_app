# Chess Trainer App

A unified project containing the Android Chess Trainer client and the Flask backend server.

## Overview

This repository provides a complete chess training platform with:

- a Kotlin Android app for gameplay, training modes, and local persistence
- a Python Flask API for authentication, chess content, and AI move requests

The client and server are designed to run together during development, with offline fallback support for core training content.

## Repository Structure

```text
chess_trainer_app/
  chess_trainer_client/   - Android app (Kotlin, MVVM, Room, Retrofit)
  chess_trainer_server/   - Backend API (Flask, SQLite, JWT)
```

## Architecture and Stack

### Client (Android)

- **Language**: Kotlin
- **Pattern**: MVVM + Repository
- **UI**: Fragments, Navigation Component, XML layouts
- **Storage**: Room (saved games), SharedPreferences (session token)
- **Networking**: Retrofit + Gson + Coroutines
- **Engine**: Pure Kotlin move generation and game-state logic

### Server (Backend)

- **Language**: Python 3.12+
- **Framework**: Flask
- **Auth**: JWT (PyJWT) + password hashing (Werkzeug)
- **Database**: SQLite
- **Data Sources**: JSON files for puzzles and openings
- **CORS**: flask-cors

## Core Features

### Authentication

- Register and login with email/password
- JWT-based session validation
- Session persistence in client local storage
- Endpoints: `POST /auth/register`, `POST /auth/login`, `GET /auth/me`

### Chess Gameplay and Training

- Two-player local game mode
- Play vs AI mode
- Puzzle training mode
- Openings study and training
- Checkmate and stalemate detection
- FEN serialization and restore support

### Persistence and Data

- Saved games stored locally in Room
- User token stored in SharedPreferences
- Puzzles/openings fetched from API with local fallback behavior in client flows

### Backend APIs

- `GET /health`
- `GET /puzzles`
- `GET /puzzles/<id>`
- `GET /openings`
- `GET /openings/<id>`
- `POST /ai-move`
- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me`

## Getting Started

### 1. Start the Server

```bash
cd chess_trainer_server
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python app.py
```

Server default URL:

```text
http://localhost:5000
```

### 2. Build and Run the Client

```bash
cd chess_trainer_client
./gradlew assembleDebug
```

Run from Android Studio on emulator or device.

## Client-Server Connectivity

- Emulator to host: `http://10.0.2.2:5000/`
- Physical device on same Wi-Fi: `http://<host-ip>:5000/`
- Physical device over USB with reverse proxy: `adb reverse tcp:5000 tcp:5000`

If needed, update the client base URL in:

`chess_trainer_client/app/src/main/java/com/example/chess_trainer_client/data/network/RetrofitClient.kt`

## Build and Test

Client tests:

```bash
cd chess_trainer_client
./gradlew test
```

Server auth verification script:

```bash
cd chess_trainer_server
python scripts/verify_auth.py
```

## Documentation

- Client documentation: `chess_trainer_client/README.md`
- Server documentation: `chess_trainer_server/README.md`
- API contract details: `chess_trainer_server/API_DOCS.md`
