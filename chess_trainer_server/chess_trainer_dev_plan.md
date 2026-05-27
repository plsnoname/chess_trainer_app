# ♟️ Chess Trainer App — Full Development Plan
> **Platform:** Android (Kotlin) + Python Flask Backend  
> **Architecture:** MVVM (Mobile) · REST API (Server)  
> **Audience:** AI model implementation — each phase is self-contained with explicit inputs, outputs, and verification gates.

---

## 📐 How to Use This Plan

- Each **Phase** is a standalone unit of work assignable to an AI model.
- Each step lists its **Inputs**, **Tasks**, and **Verification Gate** — the AI must confirm all gate criteria before the phase is considered complete.
- Steps marked 🔗 have explicit integration dependencies on prior phases.
- Do **not** skip verification gates. They exist to catch regressions before the next phase builds on top.

---

## 🗂️ Project Structure Overview

```
chess-trainer/
├── server/                  # Python Flask backend
│   ├── app.py
│   ├── data/
│   │   ├── puzzles.json
│   │   └── openings.json
│   └── requirements.txt
│
└── android/                 # Kotlin Android app
    └── app/src/main/
        ├── data/            # Local JSON assets + data models
        ├── engine/          # Chess rules + AI engine
        ├── ui/              # Activities, Fragments, Views
        └── viewmodel/       # ViewModels per feature
```

---

## 📦 Phase 0 — Project Scaffolding & Environment Setup

**Goal:** Establish the full project skeleton so every subsequent phase has a known, consistent base to build on.

### 0.1 — Server Scaffold

**Inputs:** Nothing (greenfield).

**Tasks:**
- Create the `server/` directory.
- Initialize a Python virtual environment.
- Create `requirements.txt` listing: `flask`, `flask-cors`.
- Create `app.py` with a Flask app instance, CORS enabled, and a single `GET /health` endpoint that returns `{"status": "ok"}`.
- Create `data/puzzles.json` as an empty JSON array `[]`.
- Create `data/openings.json` as an empty JSON array `[]`.

**Verification Gate:**
- Running `python app.py` starts the server on port `5000` without errors.
- `GET /health` returns HTTP 200 with `{"status": "ok"}`.
- The `data/` folder exists with both JSON files present and valid (parseable).

---

### 0.2 — Android Project Scaffold

**Inputs:** Nothing (greenfield).

**Tasks:**
- Create a new Android project targeting API level 26+ with an Empty Activity.
- Set the application package name to `com.chesstrainer.app`.
- Add the following Gradle dependencies:
  - `Retrofit2` (HTTP client)
  - `Gson` (JSON parsing)
  - `Lifecycle ViewModel & LiveData`
  - `Navigation Component`
  - `RecyclerView`
- Create the top-level package structure:
  - `com.chesstrainer.app.data`
  - `com.chesstrainer.app.engine`
  - `com.chesstrainer.app.ui`
  - `com.chesstrainer.app.viewmodel`
- Add placeholder `MainActivity.kt` that displays a blank screen.
- Add `assets/` folder containing empty `puzzles.json` and `openings.json`.

**Verification Gate:**
- Project builds successfully (`./gradlew assembleDebug` completes with no errors).
- The app installs and launches on an emulator or device showing a blank screen.
- All four package directories are present in the source tree.

---

## ♟️ Phase 1 — Chess Engine (Core Rules)

**Goal:** Implement a pure, dependency-free chess logic module. This is the foundation for every feature in the app — it must be fully correct before any UI or AI work begins.

### 1.1 — Board Representation

**Inputs:** Phase 0.2 complete.

**Tasks:**
- Create `engine/Board.kt`:
  - Represent the board as an 8×8 grid.
  - Define all piece types: King, Queen, Rook, Bishop, Knight, Pawn (White and Black variants).
  - Define a `Square` data class with rank and file coordinates.
  - Define a `Piece` data class with type and color.
  - Implement `initializeStartingPosition()` which places all 32 pieces in their standard starting squares.
  - Implement `getPiece(square: Square): Piece?` to read piece at a position.
  - Implement `setPiece(square: Square, piece: Piece?)` to write.
  - Implement `movePiece(from: Square, to: Square)` performing a raw move with no validation.
  - Implement FEN import: `loadFromFen(fen: String)` that parses a FEN string and sets up the board accordingly. Only the piece placement part of FEN is required at this stage.

**Verification Gate:**
- A unit test initializes the board and asserts that every piece is in its correct starting square (e.g., White King at e1, Black Queen at d8, all 8 white pawns on rank 2).
- FEN import test: loading `"rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"` produces the standard starting position.
- FEN import test: loading a mid-game FEN places pieces only on the squares indicated.

---

### 1.2 — Legal Move Generation

**Inputs:** Phase 1.1 complete.

**Tasks:**
- Create `engine/MoveGenerator.kt`:
  - Implement `getLegalMoves(board: Board, square: Square): List<Square>` that returns all squares a piece on the given square can legally move to.
  - Implement movement rules for each piece type:
    - **Pawn:** Forward one square; two squares from starting rank; diagonal capture; no backward movement.
    - **Rook:** Any number of squares horizontally or vertically; blocked by pieces.
    - **Bishop:** Any number of squares diagonally; blocked by pieces.
    - **Queen:** Combination of Rook and Bishop movement.
    - **Knight:** L-shape jumps; not blocked by intermediate pieces.
    - **King:** One square in any direction.
  - Enforce: a piece cannot move to a square occupied by a friendly piece.
  - Enforce: a move that would leave the moving side's King in check is illegal.
  - Implement `isInCheck(board: Board, color: PieceColor): Boolean`.
  - Implement `isCheckmate(board: Board, color: PieceColor): Boolean`.
  - Implement `isStalemate(board: Board, color: PieceColor): Boolean`.
  - Implement castling (both kingside and queenside): valid only if neither King nor Rook has moved, no pieces between them, and King does not pass through check.
  - Implement en passant capture.
  - Implement pawn promotion: when a pawn reaches the last rank, promote to Queen by default.

**Verification Gate:**
- Unit test: from starting position, White has exactly 20 legal moves.
- Unit test: a pinned piece has its legal moves restricted appropriately.
- Unit test: a King cannot move into check.
- Unit test: castling is legal when all conditions are met and illegal when the King is in check.
- Unit test: en passant move is generated correctly after the opponent's double pawn push.
- Unit test: `isCheckmate` returns true for the Fool's Mate position.
- Unit test: `isStalemate` returns true for a known stalemate position.

---

### 1.3 — Game State Manager

**Inputs:** Phase 1.2 complete.

**Tasks:**
- Create `engine/GameState.kt`:
  - Track: current board, active color (whose turn it is), move history as a list of moves.
  - Implement `applyMove(from: Square, to: Square): MoveResult` where `MoveResult` is a sealed class: `Success`, `IllegalMove`, `Checkmate`, `Stalemate`.
  - On `applyMove`, validate the move via `MoveGenerator`, apply it to the board, switch the active color, and return the appropriate `MoveResult`.
  - Implement `reset()` to restore the starting position.
  - Implement `undoLastMove()` to reverse the most recent move (required for puzzle retry and opening navigation).
  - Track castling rights and en passant target square as part of state.

**Verification Gate:**
- Unit test: applying a legal move returns `MoveResult.Success` and switches the active player.
- Unit test: applying an illegal move returns `MoveResult.IllegalMove` and does not change the board.
- Unit test: `undoLastMove()` restores the previous board state exactly, including castling rights.
- Unit test: a sequence of moves leading to checkmate returns `MoveResult.Checkmate`.

---

## 🤖 Phase 2 — AI Engine

**Goal:** Build the lightweight heuristic AI that generates moves for the opponent in Play vs AI mode and assists in puzzle/opening validation.

### 2.1 — Evaluation Function

**Inputs:** Phase 1.3 complete.

**Tasks:**
- Create `engine/Evaluator.kt`:
  - Implement `evaluate(board: Board, color: PieceColor): Int` returning a score from the perspective of `color`.
  - Material scoring:
    - Pawn = 100 points
    - Knight = 300 points
    - Bishop = 300 points
    - Rook = 500 points
    - Queen = 900 points
    - King = 10000 points (for check detection purposes)
  - Score = sum of own material − sum of opponent material.
  - Add check bonus: +50 if the opponent is in check.
  - Add capture bonus: already reflected in material difference; no extra needed.

**Verification Gate:**
- Unit test: evaluating the starting position returns 0 (perfectly balanced).
- Unit test: a board where White is missing one pawn returns a negative score for White.
- Unit test: a board where White has an extra Queen returns a strongly positive score for White.

---

### 2.2 — Move Decision Engine

**Inputs:** Phase 2.1 complete.

**Tasks:**
- Create `engine/AIEngine.kt`:
  - Implement `getBestMove(gameState: GameState, color: PieceColor): Pair<Square, Square>?` that:
    1. Enumerates all legal moves for `color` using `MoveGenerator`.
    2. For each move, applies it to a copy of the board.
    3. Evaluates the resulting position with `Evaluator`.
    4. Returns the move with the highest evaluation score.
    5. In case of a tie, selects randomly among tied moves.
  - Implement a board copy mechanism so that evaluation does not mutate the live game state.
  - Add a maximum response time guard: if more than 800ms have elapsed, return the best move found so far.

**Verification Gate:**
- Unit test: AI presented with a free queen capture always takes the queen.
- Unit test: AI does not make illegal moves (call `MoveGenerator.getLegalMoves` and confirm the AI's chosen move is in the list).
- Performance test: `getBestMove` completes in under 1 second on a standard mid-game position.
- Unit test: when no legal moves exist, `getBestMove` returns null.

---

## 🖥️ Phase 3 — Flask Server

**Goal:** Build the minimal backend that serves static puzzle and opening data, with an optional AI-move endpoint.

### 3.1 — Data Files

**Inputs:** Phase 0.1 complete.

**Tasks:**
- Populate `data/puzzles.json` with a minimum of 10 chess puzzles. Each puzzle object must have:
  - `id` (string, unique)
  - `fen` (string, valid FEN of the puzzle starting position)
  - `solution` (array of strings, each move in algebraic notation e.g. `"e2e4"`)
  - `description` (string, brief human-readable label)
  - `difficulty` (string: `"easy"`, `"medium"`, or `"hard"`)
- Populate `data/openings.json` with a minimum of 5 chess openings. Each opening object must have:
  - `id` (string, unique)
  - `name` (string, e.g. `"Ruy Lopez"`)
  - `moves` (array of strings in algebraic notation, e.g. `["e2e4", "e7e5", "g1f3"]`)
  - `description` (string, brief explanation)

**Verification Gate:**
- Both JSON files parse without errors.
- Every puzzle's `fen` field is a valid, parseable FEN string.
- Every puzzle has at least one move in `solution`.
- Every opening has at least 3 moves in `moves`.

---

### 3.2 — API Endpoints

**Inputs:** Phase 3.1 complete.

**Tasks:**
- In `app.py`, implement the following endpoints:
  - `GET /puzzles` — reads `data/puzzles.json`, returns the full array as JSON with HTTP 200.
  - `GET /puzzles/<id>` — returns a single puzzle by ID, or HTTP 404 if not found.
  - `GET /openings` — reads `data/openings.json`, returns the full array as JSON with HTTP 200.
  - `GET /openings/<id>` — returns a single opening by ID, or HTTP 404 if not found.
  - `POST /ai-move` — accepts a JSON body `{"fen": "<fen_string>"}`, returns `{"move": "<algebraic>"}`. For now, this endpoint may return a hardcoded or random legal move; the full AI logic is on-device. Mark it as a stub with a comment.
- Enable CORS for all endpoints to allow requests from any origin.
- All endpoints return `Content-Type: application/json`.
- Errors (404, 400, 500) return a JSON body `{"error": "<message>"}`.

**Verification Gate:**
- `GET /puzzles` returns HTTP 200 and a JSON array of 10+ items.
- `GET /puzzles/invalid-id` returns HTTP 404 with `{"error": "..."}`.
- `GET /openings` returns HTTP 200 and a JSON array of 5+ items.
- `POST /ai-move` with a valid FEN returns HTTP 200 with a `move` field.
- `POST /ai-move` with a missing `fen` field returns HTTP 400.
- All responses have `Content-Type: application/json`.

---

## 📱 Phase 4 — Android Data Layer

**Goal:** Set up all data models, local asset loading, and the Retrofit network client. This layer sits between the server and the ViewModels.

### 4.1 — Data Models

**Inputs:** Phase 0.2 complete.

**Tasks:**
- In `data/`, create the following Kotlin data classes (all serializable with Gson):
  - `Puzzle(id: String, fen: String, solution: List<String>, description: String, difficulty: String)`
  - `Opening(id: String, name: String, moves: List<String>, description: String)`
  - `AiMoveRequest(fen: String)`
  - `AiMoveResponse(move: String)`

**Verification Gate:**
- All data classes compile without errors.
- A unit test deserializes a hardcoded JSON string into each model and asserts field values are correct.

---

### 4.2 — Network Client (Retrofit)

**Inputs:** Phase 4.1 complete.

**Tasks:**
- Create `data/network/ApiService.kt` defining the Retrofit interface with:
  - `suspend fun getPuzzles(): List<Puzzle>`
  - `suspend fun getPuzzleById(id: String): Puzzle`
  - `suspend fun getOpenings(): List<Opening>`
  - `suspend fun getOpeningById(id: String): Opening`
  - `suspend fun getAiMove(request: AiMoveRequest): AiMoveResponse`
- Create `data/network/RetrofitClient.kt`:
  - Base URL configurable via a constant (default: `http://10.0.2.2:5000/` for emulator, `http://<local-ip>:5000/` for device).
  - Use Gson converter factory.
  - Use a 10-second read/connect timeout.
- Create `data/repository/ChessRepository.kt`:
  - Wraps `ApiService` calls in try/catch, returning a `Result<T>` (success or failure) for each call.
  - Implement `loadLocalPuzzles(context: Context): List<Puzzle>` that reads from `assets/puzzles.json` as a fallback.
  - Implement `loadLocalOpenings(context: Context): List<Opening>` that reads from `assets/openings.json` as a fallback.

**Verification Gate:**
- With the Flask server running, a direct Retrofit call to `GET /puzzles` from an Android integration test returns a non-empty list.
- If the server is unreachable, the repository returns a `Result.failure` without crashing.
- Local asset fallback correctly parses the bundled JSON and returns the list.

---

## 🎨 Phase 5 — UI Components

**Goal:** Build the shared visual components used across all screens.

### 5.1 — Chessboard View

**Inputs:** Phase 1.1 complete, Phase 0.2 complete.

**Tasks:**
- Create a custom `ChessBoardView` (extend `View` or `SurfaceView`):
  - Draw an 8×8 grid with alternating light and dark squares (standard chess colors).
  - Render piece icons on the correct squares. Use vector drawables or bitmap assets for all 12 piece types (6 types × 2 colors).
  - Implement touch handling: `onTouchEvent` maps a screen tap to a board square.
  - Expose a public method `setBoard(board: Board)` that causes the view to redraw.
  - Expose a callback interface `OnSquareTappedListener(square: Square)`.
  - Implement `highlightSquares(squares: List<Square>)` to visually highlight a set of squares (use a semi-transparent colored overlay). Used to show legal moves.
  - Implement `highlightSelectedSquare(square: Square?)` to highlight the currently selected piece.
  - Ensure the board is always square and scales correctly to any screen width.

**Verification Gate:**
- A test Activity displaying `ChessBoardView` initialized with the starting position renders all 32 pieces in the correct squares.
- Tapping a square triggers `OnSquareTappedListener` with the correct `Square` coordinates.
- Calling `highlightSquares` visually marks the target squares with an overlay.
- The board remains square on at least two different screen sizes (e.g., a phone and a tablet emulator).

---

### 5.2 — Navigation & Home Screen

**Inputs:** Phase 0.2 complete.

**Tasks:**
- Set up the Android Navigation Component with a `nav_graph.xml` containing destinations for:
  - `HomeFragment`
  - `GameFragment` (shared between 2-player and AI modes, differentiated by argument)
  - `PuzzleFragment`
  - `OpeningsFragment`
- Create `HomeFragment` with four clearly labeled navigation buttons:
  - **Play vs Player** → `GameFragment` with argument `mode = "two_player"`
  - **Play vs AI** → `GameFragment` with argument `mode = "vs_ai"`
  - **Puzzles** → `PuzzleFragment`
  - **Openings** → `OpeningsFragment`
- Apply a consistent visual theme (dark background with gold/white accents is recommended).

**Verification Gate:**
- Tapping each button navigates to the correct destination.
- The back button from any destination returns to `HomeFragment`.
- The navigation graph has no disconnected destinations.

---

## 🎮 Phase 6 — Gameplay Screen

**Goal:** Implement the full game screen used for both 2-player and AI modes.

### 6.1 — Two-Player Mode

**Inputs:** Phase 1.3 complete, Phase 5.1 complete, Phase 5.2 complete.

**Tasks:**
- Create `viewmodel/GameViewModel.kt`:
  - Hold a `GameState` instance.
  - Expose `LiveData<Board>` for the current board state.
  - Expose `LiveData<PieceColor>` for the active player.
  - Expose `LiveData<GameResult>` (sealed class: `Ongoing`, `Checkmate(winner)`, `Stalemate`).
  - Expose `LiveData<List<Square>>` for legal moves of the selected piece.
  - Implement `onSquareTapped(square: Square)`:
    - If no piece is selected: select the tapped piece if it belongs to the active player, emit its legal moves.
    - If a piece is already selected and the tapped square is a legal move: apply the move via `GameState`, clear selection, update board.
    - If the tapped square is not a legal move: clear selection.
  - Implement `resetGame()`.
- Create `GameFragment`:
  - Display `ChessBoardView`.
  - Display a turn indicator label (`"White to move"` / `"Black to move"`).
  - Display a **Reset** button.
  - Observe `GameViewModel` LiveData and update the view accordingly.
  - Show a dialog when `GameResult` is `Checkmate` or `Stalemate` with the result message and a **Play Again** option.

**Verification Gate:**
- From the starting position, tapping a white pawn highlights its two possible move squares.
- Making a legal move updates the board and switches the turn indicator.
- Tapping a square with no legal moves clears the selection without error.
- After Fool's Mate, the checkmate dialog appears with the correct winner.
- The **Reset** button restores the starting position.

---

### 6.2 — Play vs AI Mode

**Inputs:** Phase 2.2 complete, Phase 6.1 complete.

**Tasks:**
- Extend `GameViewModel` with an `isAiMode: Boolean` flag set via the navigation argument.
- When `isAiMode` is true:
  - The human player is always White.
  - After the human makes a move (and it is now Black's turn), automatically invoke `AIEngine.getBestMove()` on a background coroutine (not the main thread).
  - Display a loading indicator (e.g., a spinning progress bar) while the AI is computing.
  - Apply the AI's move and update the board once the result is returned.
  - Dismiss the loading indicator after the AI moves.
- The AI must never be triggered on the human's turn.

**Verification Gate:**
- After the human plays a move, the AI responds automatically within 2 seconds.
- The loading indicator appears while the AI is computing and disappears afterward.
- The AI never makes an illegal move (confirmed by checking the move against `MoveGenerator.getLegalMoves`).
- The game reaches checkmate and stalemate states correctly in AI mode.
- The human cannot interact with the board while the AI is computing.

---

## 🧩 Phase 7 — Puzzle Mode

**Goal:** Implement the puzzle-solving screen backed by server data with local fallback.

### 7.1 — Puzzle ViewModel & Logic

**Inputs:** Phase 1.3 complete, Phase 4.2 complete, Phase 2.2 complete.

**Tasks:**
- Create `viewmodel/PuzzleViewModel.kt`:
  - On initialization, fetch puzzles from the server via `ChessRepository`. Fall back to local assets if the network call fails.
  - Expose `LiveData<Puzzle?>` for the currently active puzzle.
  - Expose `LiveData<Board>` for the current board state (initialized from the puzzle's FEN).
  - Expose `LiveData<PuzzleResult>` (sealed class: `Idle`, `Correct`, `Incorrect`, `Completed`).
  - Implement `loadPuzzle(puzzle: Puzzle)`: parse FEN into a `Board`, initialize a `GameState`, set the active color from the FEN.
  - Implement `onSquareTapped(square: Square)`:
    - Follow the same selection logic as `GameViewModel`.
    - When a move is applied, check it against the puzzle's `solution`:
      - If the move matches the next expected move in the solution, emit `PuzzleResult.Correct`. If this was the final solution move, emit `PuzzleResult.Completed`.
      - If the move does not match, emit `PuzzleResult.Incorrect` and undo the move using `GameState.undoLastMove()`.
  - Implement `nextPuzzle()` to advance to the next puzzle in the loaded list.
  - Implement `retryPuzzle()` to reload the current puzzle's FEN.

**Verification Gate:**
- Loading a puzzle sets the board to the correct FEN position.
- Playing the correct first move emits `PuzzleResult.Correct`.
- Playing an incorrect move emits `PuzzleResult.Incorrect` and restores the board to the pre-move state.
- After completing all solution moves, `PuzzleResult.Completed` is emitted.
- Fallback to local assets works when the server is unreachable.

---

### 7.2 — Puzzle Fragment UI

**Inputs:** Phase 7.1 complete, Phase 5.1 complete.

**Tasks:**
- Create `PuzzleFragment`:
  - Display `ChessBoardView` in the puzzle's starting position.
  - Display the puzzle description and difficulty label.
  - Display a feedback area that shows:
    - ✅ `"Correct!"` in green on `PuzzleResult.Correct`.
    - ❌ `"Incorrect — try again"` in red on `PuzzleResult.Incorrect`.
    - 🏆 `"Puzzle solved!"` with a **Next Puzzle** button on `PuzzleResult.Completed`.
  - Display a **Retry** button to reset the current puzzle.
  - Display a **Skip** button to move to the next puzzle without solving.
  - Show a loading indicator while puzzles are being fetched.
  - Show an error message if both network and local fallback fail.

**Verification Gate:**
- The board is set up correctly for each loaded puzzle.
- Feedback messages appear correctly for correct, incorrect, and completed states.
- The **Retry** button resets the board to the puzzle's starting FEN.
- The **Next Puzzle** button loads the following puzzle.
- The loading state is visible during the network fetch and disappears when data is ready.

---

## 📖 Phase 8 — Openings Trainer

**Goal:** Implement the openings learning and practice screen.

### 8.1 — Openings ViewModel & Logic

**Inputs:** Phase 1.3 complete, Phase 4.2 complete.

**Tasks:**
- Create `viewmodel/OpeningsViewModel.kt`:
  - On initialization, fetch openings from the server via `ChessRepository`. Fall back to local assets if needed.
  - Expose `LiveData<List<Opening>>` for the openings list.
  - Expose `LiveData<Opening?>` for the selected opening.
  - Expose `LiveData<Board>` for the current board state.
  - Expose `LiveData<Int>` for the current move index within the opening.
  - Expose `LiveData<OpeningMode>` (sealed class: `Study`, `Training`).
  - **Study Mode:**
    - Implement `nextMove()`: apply the next move from the opening's move list to the board, increment move index.
    - Implement `previousMove()`: undo the last move, decrement move index.
  - **Training Mode:**
    - The current move is hidden.
    - Implement `onSquareTapped(square: Square)`:
      - When a move is made, compare it to the expected next move in the opening.
      - Emit `TrainingResult.Correct` or `TrainingResult.Incorrect` accordingly.
      - On incorrect, undo the move.
  - Implement `selectOpening(opening: Opening)` to load an opening and reset to the starting position.
  - Implement `resetOpening()` to return to the starting position of the current opening.
  - Implement `switchMode(mode: OpeningMode)`.

**Verification Gate:**
- Selecting an opening loads the starting position (standard chess starting FEN).
- In Study Mode, `nextMove()` advances the board correctly one move at a time.
- In Study Mode, `previousMove()` restores the previous board state exactly.
- `nextMove()` does nothing when all moves in the opening have been played.
- In Training Mode, playing the correct next move emits `TrainingResult.Correct`.
- In Training Mode, playing an incorrect move emits `TrainingResult.Incorrect` and reverts the board.

---

### 8.2 — Openings Fragment UI

**Inputs:** Phase 8.1 complete, Phase 5.1 complete.

**Tasks:**
- Create `OpeningsFragment`:
  - Display a `RecyclerView` listing all available openings by name.
  - Tapping an opening name loads it into the board view.
  - Display `ChessBoardView` showing the current board state.
  - Display the opening name and a move counter (`"Move 3 / 7"`).
  - Display **← Previous** and **Next →** navigation buttons for Study Mode.
  - Display a **Study / Train** toggle to switch modes.
  - In Training Mode, hide the **← Previous** and **Next →** buttons and instead show a feedback label.
  - Display a **Reset** button.

**Verification Gate:**
- The openings list displays all fetched openings.
- Tapping an opening loads it and resets the board.
- Previous/Next buttons work correctly and the move counter updates.
- The Study/Train toggle correctly switches between modes.
- In Training Mode, feedback is shown after each attempted move.
- The Reset button returns the board to the opening's start.

---

## 🔗 Phase 9 — Integration & End-to-End Demo

**Goal:** Connect all components, verify the full user journey from server to UI, and prepare a working demo.

### 9.1 — Network Configuration & Server Connection

**Inputs:** Phase 3.2 complete, Phase 4.2 complete.

**Tasks:**
- Confirm the emulator base URL is set to `http://10.0.2.2:5000/`.
- Add `android:usesCleartextTraffic="true"` to `AndroidManifest.xml` (required for HTTP in development; note: replace with HTTPS before any production deployment).
- Add `INTERNET` permission to `AndroidManifest.xml`.
- Confirm the Flask server binds to `0.0.0.0` (not just `127.0.0.1`) so it is reachable from both the emulator and a physical device on the same Wi-Fi network.
- Document the steps for connecting a physical device: how to find the host machine's local IP and set it as the base URL.

**Verification Gate:**
- With the server running on the host machine, the Android app (on emulator) fetches puzzles and openings from the server successfully.
- If the server is stopped, the app gracefully falls back to local assets and displays data without crashing.
- An error toast or message is shown when the network is unavailable (not a silent failure).

---

### 9.2 — Full User Journey Test

**Inputs:** All prior phases complete.

**Tasks:**
- Execute the following manual test scenarios end-to-end and confirm each passes:

**Scenario A — Two-Player Game:**
1. Launch app → Home Screen visible.
2. Tap **Play vs Player** → Game Screen with starting position.
3. White taps a pawn → legal moves highlighted.
4. White taps a legal move → pawn moves, turn switches to Black.
5. Black plays; continue until checkmate.
6. Checkmate dialog appears; tap **Play Again** → board resets.

**Scenario B — Play vs AI:**
1. Tap **Play vs AI** → Game Screen.
2. White makes a move → loading indicator appears → AI responds → board updates.
3. Play to completion; verify AI never makes an illegal move.
4. Tap **Reset** → board resets to starting position.

**Scenario C — Puzzle Mode:**
1. Tap **Puzzles** → loading indicator → puzzle board displayed.
2. Play the correct first move → `"Correct!"` feedback shown.
3. Complete all solution moves → `"Puzzle solved!"` and **Next Puzzle** appear.
4. Tap **Next Puzzle** → next puzzle loads.
5. Play an incorrect move → `"Incorrect — try again"` shown; board reverts.

**Scenario D — Openings Trainer (Study):**
1. Tap **Openings** → list of openings displayed.
2. Select an opening → board shown at starting position.
3. Tap **Next →** repeatedly → moves played sequentially.
4. Tap **← Previous** → move undone.

**Scenario E — Openings Trainer (Training):**
1. Select an opening → switch to **Train** mode.
2. Play the correct first move → `"Correct!"` shown.
3. Play an incorrect move → `"Incorrect"` shown; board reverts.

**Scenario F — Offline Fallback:**
1. Stop the Flask server.
2. Launch the app and navigate to Puzzles and Openings.
3. Confirm data loads from local assets; no crash occurs.

**Verification Gate:**
- All six scenarios pass without any crash or unhandled exception.
- No illegal chess moves occur in any scenario.
- Feedback messages appear correctly in all relevant scenarios.
- The app handles the offline scenario gracefully.

---

### 9.3 — Final Polish & Demo Build

**Inputs:** Phase 9.2 complete.

**Tasks:**
- Review all screens for layout consistency:
  - Buttons are not clipped or overlapping.
  - Text is readable on both light and dark emulator themes.
  - The chessboard fills the intended area and is never stretched non-uniformly.
- Add a simple app icon (can be a standard chess piece vector).
- Confirm the app name displayed in the launcher is `"Chess Trainer"`.
- Build a release-mode APK: `./gradlew assembleRelease`.
- Write a concise `README.md` at the project root covering:
  - How to start the Flask server.
  - How to install the APK on a device or emulator.
  - How to point the app at the server (URL configuration).
  - A feature summary.

**Verification Gate:**
- The release APK installs and runs on a clean emulator with no prior installation.
- The README is accurate: following it from scratch produces a working demo.
- All screens are visually consistent with no layout regressions.

---

## ✅ Phase Dependency Summary

```
Phase 0.1  ──────────────────────────── Phase 3.1 → Phase 3.2
Phase 0.2  → Phase 1.1 → Phase 1.2 → Phase 1.3
                                           │
                              ┌────────────┴──────────────┐
                              ▼                           ▼
                         Phase 2.1 → Phase 2.2       Phase 4.1 → Phase 4.2
                              │                           │
                    ┌─────────┘                           │
                    ▼                                     │
               Phase 5.1 → Phase 5.2                     │
                    │            │                        │
          ┌─────────┘            └──────────────────┐    │
          ▼                                         ▼    │
     Phase 6.1 → Phase 6.2                    Phase 7.1 ←┘
                                                    │
                                               Phase 7.2
                                                    │
                                          Phase 8.1 → Phase 8.2
                                                    │
                                          Phase 9.1 → 9.2 → 9.3
```

---

## 🚫 Known Constraints & Explicit Non-Goals

- No Stockfish or external engine integration.
- No online multiplayer.
- No user authentication.
- No persistent statistics (out of scope for this version).
- HTTP (not HTTPS) is acceptable for local demo; do not deploy the Flask server publicly without adding HTTPS.
- The AI search depth is capped at 1 ply (single-move lookahead). Do not implement minimax recursion in this version.

---

## 🔮 Future Work (Do Not Implement Now)

- Minimax with alpha-beta pruning (depth 3–5).
- Online multiplayer via WebSockets.
- Puzzle difficulty progression and unlock system.
- User profiles and game history persistence.
- Integration with Lichess public API for live puzzle feeds.
