# Chess Trainer Client

A comprehensive Android chess training application built in Kotlin with MVVM architecture. The client provides an interactive platform for chess learning, including two-player games, AI opponent gameplay, puzzle solving, and openings training.

## Overview

The Chess Trainer Client is a feature-rich Android application designed to help users improve their chess skills through various training modes and interactive gameplay. The app integrates with a backend server for authentication, AI moves, and content management while providing offline fallback for puzzles and openings.

## Architecture and Technology Stack

### Architecture Pattern
- MVVM (Model-View-ViewModel) with LiveData for reactive UI updates
- Repository pattern for data access abstraction
- Dependency injection via singleton pattern

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Android Fragments with ConstraintLayout
- **Database**: SQLite via Android Room ORM
- **Networking**: Retrofit 2 with Gson for JSON deserialization
- **Async Operations**: Kotlin Coroutines with Flow
- **Navigation**: Android Navigation Component with Safe Args
- **Local Storage**: SharedPreferences for session management

### Directory Structure
```
app/src/main/
  java/com/example/chess_trainer_client/
    engine/          - Pure Kotlin chess engine with move validation
    data/
      local/         - Room database, SharedPreferences, local storage
      network/       - Retrofit API service and client
      repository/    - Repository layer for data access
    ui/              - Fragments for app screens
    viewmodel/       - MVVM ViewModels for business logic
  res/
    layout/          - XML layout files for all screens
    values/          - Colors, strings, dimensions, theme definitions
    drawable/        - Button shapes and UI elements
    navigation/      - Navigation graph for screen transitions
```

## Core Features

### 1. Authentication and Session Management

The app implements a complete authentication system with persistent session handling:

- **Registration**: Users can create accounts with email and password
- **Login**: Secure authentication using JWT tokens
- **Session Persistence**: Authentication tokens stored in SharedPreferences
- **Automatic Token Refresh**: Token included in API requests for session verification
- **Logout**: Clear session and return to login screen

Implementation Details:
- AuthFragment handles registration and login UI
- AuthRepository manages authentication API calls
- SessionManager handles token persistence
- AuthViewModel manages authentication state

### 2. Game Screens and Navigation

The application provides multiple interconnected screens with smooth navigation:

**Screens:**
- AuthFragment - Login and registration interface
- MenuFragment - Main menu with game mode selection
- GameFragment - Chess board and game controls
- PuzzleFragment - Chess puzzle training
- OpeningsFragment - Openings study and training
- SavedGamesFragment - Game history and resume functionality

**Navigation Features:**
- Type-safe navigation using Safe Args
- Proper back stack management
- Deep linking support
- Argument passing between screens for context preservation
- Navigation graph centrally defined in nav_graph.xml

### 3. Local Database Integration

The app uses SQLite with Android Room to provide persistent local storage:

**Database Schema:**
- SavedGame table - Stores game FEN notation, active color, game mode, and timestamp
- Automatic schema versioning with migration support
- Thread-safe database access via Room

**Features:**
- Save games during or after play
- Load previously saved games with full state restoration
- Delete saved games
- Display saved games in scrollable RecyclerView
- Persistence across app restarts

**Implementation:**
- UserSessionDatabase - Room database definition
- SavedGameStore - Data access object for game storage
- SavedGameEntity - Data class for game representation

### 4. HTTP API Integration

The app communicates with the backend server for authentication, AI moves, and content:

**API Endpoints:**
- POST /auth/register - User registration
- POST /auth/login - User authentication
- GET /auth/me - Session verification
- GET /puzzles - Fetch all chess puzzles
- GET /puzzles/{id} - Fetch specific puzzle
- GET /openings - Fetch all chess openings
- GET /openings/{id} - Fetch specific opening
- POST /ai-move - Request AI move response

**Network Implementation:**
- Retrofit HTTP client with automatic base URL selection
- Emulator detection: uses 10.0.2.2:5000 for emulator, 127.0.0.1:5000 for physical device with adb reverse
- Automatic JSON deserialization via Gson
- Error handling with Result<T> wrapper pattern
- Coroutine integration for async operations

**Device Detection:**
```
Emulator: http://10.0.2.2:5000/
Physical Device (adb reverse): http://127.0.0.1:5000/
Physical Device (Wi-Fi): http://<host-ip>:5000/
```

### 5. Data Persistence and Offline Support

The app combines online and offline data sources:

**SharedPreferences Usage:**
- User authentication token storage
- Session state persistence
- Token automatically included in API headers

**Local Assets:**
- puzzles.json - Chess puzzles cached locally
- openings.json - Chess openings cached locally
- Automatic fallback when server is unavailable
- JSON deserialization via Gson with TypeToken

**Data Loading Strategy:**
- Attempt network request first
- Fall back to local assets on network failure
- Cache remote data for offline access
- Users can train even without server connection

### 6. User Interface and Design

The application implements a modern, cohesive dark theme throughout:

**Color Scheme:**
- Background Dark: #121826 (primary background)
- Card Surface: #1E293B (panels, cards, default states)
- Button Primary: #3B82F6 (interactive elements)
- Button Hover: #2563EB (pressed/focused state)
- Text Primary: #F8FAFC (main text)
- Text Secondary: #A1A5B0 (secondary text, hints)

**UI Components:**
- All buttons feature rounded corners with consistent styling
- Proper padding and spacing (24-32dp top, 36-40dp bottom for navigation bar)
- No overlapping elements using ConstraintLayout
- Theme applied system-wide via themes.xml and styles.xml
- Navigation bar themed to match app background

**Screens:**
- Login screen with gradient background and centered form fields
- Menu screen with game mode selection buttons
- Game board with move history and control buttons
- Puzzle trainer with feedback and navigation
- Openings trainer with move counter and study/training modes
- Saved games list with load and delete functionality

### 7. Chess Engine

The app includes a pure Kotlin chess engine:

**Features:**
- Complete move validation including en passant and castling
- Checkmate and stalemate detection
- FEN notation support for game state serialization
- Two-player gameplay with turn-based system

**Implementation:**
- Board representation and piece logic
- Legal move generation with rule validation
- Game state evaluation
- Located in app/src/main/java/com/example/chess_trainer_client/engine

### 8. AI Opponent

The AI system provides intelligent gameplay through backend integration:

**Features:**
- Server-side AI move calculation
- Undo prevention in AI mode (prevents undoing opponent moves)
- Response time guard (approximately 800ms)
- Difficulty controlled by server implementation

**Gameplay:**
- Player makes move as White
- App sends board state to /ai-move endpoint
- AI response automatically applied to board
- Turn-based gameplay with proper state management

### 9. Stability and Error Handling

The application implements comprehensive crash prevention:

**Stability Features:**
- Fixed AlertDialog theme compatibility issues
- Proper ViewModel constructor patterns
- Correct exception handling in coroutines
- Input prevention after game completion
- Try-catch blocks in all HTTP requests
- Null safety checks throughout codebase

**Tested Scenarios:**
- All screen transitions complete without crashes
- Game over dialog displays correctly on all modes
- Puzzle and openings navigation stable
- Save/load operations error-free
- App remains stable across multiple sessions
- Proper handling of network timeouts and errors

## Building and Running

### Prerequisites
- Android Studio (latest version)
- Android SDK 21 or higher
- Kotlin support in IDE
- Connected Android device or running emulator

### Build Commands

Debug build:
```bash
./gradlew assembleDebug
```

Release build:
```bash
./gradlew assembleRelease
```

Run tests:
```bash
./gradlew test
```

### Running on Emulator

1. Open project in Android Studio
2. Create or select an Android Virtual Device
3. Click Run or press Shift+F10
4. Server automatically uses 10.0.2.2:5000

### Running on Physical Device

1. Connect device via USB
2. Enable USB debugging in device settings
3. Set up port forwarding:
   ```bash
   adb reverse tcp:5000 tcp:5000
   ```
4. Run app via Android Studio or:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

### Running on Physical Device via Wi-Fi

Update RetrofitClient with your server IP:
```kotlin
private val BASE_URL = "http://<your-server-ip>:5000/"
```

## Server Configuration

The client expects a running Flask server:

**Default Configuration:**
- Local: http://localhost:5000/
- Emulator: http://10.0.2.2:5000/
- Physical device: http://127.0.0.1:5000/ (with adb reverse)

**To Change Server Address:**

Edit `app/src/main/java/com/example/chess_trainer_client/data/network/RetrofitClient.kt` and update the BASE_URL constant.

## Testing Checklist

### Manual Testing

Two-Player Game:
- Launch app, complete login
- Select "Play vs Player" from menu
- Make moves and verify turn changes
- Verify checkmate dialog appears

Play vs AI:
- Select "Play vs AI" from menu
- Make a move as White
- Verify AI responds within 2-3 seconds
- Verify board updates with AI move

Puzzle Mode:
- Select "Puzzles" from menu
- Complete puzzle with correct move
- Verify "Correct!" feedback appears
- Try incorrect move and verify board resets

Openings Study:
- Select "Openings" from menu
- Use Next/Previous buttons
- Verify move counter updates
- Try training mode with correct/incorrect moves

Save and Load:
- During game, tap "Save Game"
- Return to menu
- Select "Saved Games"
- Tap load on saved game
- Verify full game state is restored

Offline Functionality:
- Stop the server
- Verify puzzles and openings still load from local assets
- Verify saved games still accessible

### Automated Testing

Run unit tests:
```bash
./gradlew test
```

## Performance Considerations

- Coroutines used for all blocking operations
- LiveData prevents unnecessary UI redraws
- RecyclerView with ViewHolder pattern for efficient list display
- Lazy loading of assets to reduce startup time
- Database queries optimized with indices

## Dependencies

Key dependencies (see build.gradle.kts for complete list):
- androidx.appcompat - Android compatibility library
- androidx.lifecycle - ViewModel and LiveData
- androidx.room - Local database ORM
- retrofit2 - HTTP client
- com.squareup.okhttp3 - HTTP interceptors
- com.google.code.gson - JSON parsing
- androidx.navigation - Navigation framework
- androidx.recyclerview - List display

## Known Limitations

- AI strength determined by server implementation
- Puzzles and openings cached locally (updates require app reinstall)
- Single-player training modes only
- No cloud save/synchronization

## Future Enhancements

- Online multiplayer games
- User progression tracking and statistics
- Enhanced puzzle rating system
- Offline database sync when connection restored
- Push notifications for friend challenges
- Dark mode variants

## Troubleshooting

### Connection Refused
- Verify server is running and accessible
- Check if using correct IP address for device type (10.0.2.2 for emulator, 127.0.0.1 for device with adb reverse)
- Run `adb reverse tcp:5000 tcp:5000` on physical device

### Crashes on Game Over
- Ensure using latest build
- Clear app cache: Settings > Apps > Chess Trainer > Storage > Clear Cache
- Reinstall app

### Puzzles/Openings Not Loading
- Verify assets are present in app/src/main/assets/
- Check network connection
- Verify server is running for online content

### Save/Load Not Working
- Check app storage permissions: Settings > Apps > Chess Trainer > Permissions
- Verify sufficient device storage space
- Clear app data and reinstall if persistent issues occur

## License

Chess Trainer Client is part of the Chess Trainer project.

## Support

For issues, questions, or feature requests, contact the development team or consult API_DOCS.md for backend documentation.
