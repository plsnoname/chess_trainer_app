# Chess Trainer App

Unified repository for the Android client and the Python server used by the Chess Trainer App.

## Repository layout

- `chess_trainer_client/` – Android client (Kotlin, Gradle).
- `chess_trainer_server/` – Python server (Flask).

## What the app targets

- Auth-first flow (login/register screen on app start).
- A post-login menu with available game options.
- A unified game screen with a full board, pieces, and legal move UI.
- Local save/resume of game state on Android (SQLite via Room).
- A saved-games menu to reopen previous games.

## Getting started

### Android client (local)

Open `chess_trainer_client/` in Android Studio and let Gradle sync. From there you can run on an emulator or device. If you prefer command-line builds, use the Gradle wrapper in that folder.

### Python server (local)

A typical setup looks like this (adjust paths/versions to your environment):

```bash
cd /home/plsnoname/UNV_IA/OF2/chess_trainer_app/chess_trainer_server
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python app.py
```

## Notes

- API details live in `chess_trainer_server/API_DOCS.md`.
- Client documentation lives in `chess_trainer_client/README.md`.

