# Chess Trainer Client

Android client for the Chess Trainer app. Includes the chess engine, AI, gameplay, puzzles, and openings trainer.

## Run Tests

```sh
./gradlew test
```

## Server Setup (Local)

The Android client expects the Flask server from the companion project.

- Local base URL: `http://localhost:5000/`
- Android emulator: `http://10.0.2.2:5000/`
- Physical device: `http://<host-ip>:5000/`

To point the app at a different server, update `RetrofitClient.BASE_URL` in
`app/src/main/java/com/example/chess_trainer_client/data/network/RetrofitClient.kt`.

## Manual Test Checklist (Phase 9.2)

- Two-Player Game: launch app, play moves, verify turn changes and checkmate dialog.
- Play vs AI: make a move as White, verify AI responds within ~2 seconds.
- Puzzle Mode: correct move shows "Correct!", incorrect move resets board.
- Openings Study: use Next/Previous, verify move counter updates.
- Openings Training: correct move shows "Correct!", incorrect reverts.
- Offline Fallback: stop server, verify puzzles/openings load from assets.

## Release Build (Phase 9.3)

```sh
./gradlew assembleRelease
```

The APK will be under `app/build/outputs/apk/release/`.

## Features

- Two-player mode with full rule validation.
- Play vs AI (1-ply evaluation, 800ms guard).
- Puzzle mode with feedback and retry/next flows.
- Openings trainer with study and training modes.

## Engine Notes

- Pure Kotlin engine under `app/src/main/java/com/example/chess_trainer_client/engine`.
- AI uses a 1-ply evaluation with an 800ms response guard.
