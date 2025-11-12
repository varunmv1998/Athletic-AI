# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Athletic AI - Intelligent Fitness Tracker

Android fitness app with AI coaching, built in 4 phases using Jetpack Compose + Material3.

## Key Project Information

- **Package Name**: `com.athleticai.app`
- **Language**: Kotlin with Java 11 compatibility
- **Build System**: Gradle with Kotlin DSL (.gradle.kts files)
- **Min SDK**: 35, Target SDK: 36, Compile SDK: 36
- **Test Framework**: JUnit 4 for unit tests, AndroidX Test with Espresso for instrumentation tests

## Build Commands

### Basic Build Tasks
- **Build the project**: `./gradlew build`
- **Clean build**: `./gradlew clean`
- **Assemble debug APK**: `./gradlew assembleDebug`
- **Assemble release APK**: `./gradlew assembleRelease`

### Testing
- **Run unit tests**: `./gradlew test`
- **Run instrumented tests**: `./gradlew connectedAndroidTest`
- **Run all tests**: `./gradlew check`

### Development
- **Install debug APK on device**: `./gradlew installDebug`
- **Uninstall from device**: `./gradlew uninstallDebug`

## Architecture Overview

### MVVM + Repository Pattern
The app follows a strict layered architecture:

- **UI Layer**: Jetpack Compose screens with ViewModels
- **Repository Layer**: Data access abstraction with business logic
- **Database Layer**: Room database with DAOs and entities
- **Data Layer**: JSON asset loading, API services, and data models

### Dependency Injection
Uses manual dependency injection with:
- `AthleticAIApplication`: Application class that initializes repositories
- `AppContainer`: Manages ViewModels and their dependencies
- Lazy initialization pattern for all components

### Database Architecture
Room database (`AppDatabase`) with versioned migrations:
- **Current version**: 5
- **Migration strategy**: Defined migrations from v1-v5 with fallback to destructive migration
- **Entities organized by phase**:
  - Phase 1: Exercise, WorkoutSession, WorkoutSet
  - Phase 2: Program entities (ProgramEnrollment, ProgramTemplate, etc.)
  - Phase 3: Analytics entities (BodyMeasurement, PersonalRecord, Goal)
  - Achievements: AchievementEntity, UserAchievementEntity, stats entities

### Key Components

#### Application Layer
- `AthleticAIApplication.kt`: Main application class with repository initialization
- `AppContainer.kt`: ViewModel factory and dependency container
- `MainActivity.kt`: Single activity with Compose navigation

#### Data Layer Repositories
- `WorkoutRepository`: Workout sessions and sets management
- `ExerciseRepository`: Exercise database with asset loading
- `ProgramRepository`: 90-day program management with progression
- `AnalyticsRepository`: Progress tracking and statistics
- `MeasurementsRepository`: Body measurements and goals
- `SettingsRepository`: App preferences and API key management
- `AchievementRepository`: Achievement system integration
- `AIService`: OpenAI API integration

#### ViewModels
- `WorkoutViewModel`: Active workout sessions and exercise management
- `ProgramViewModel`: Program progression and day management
- `ProgressViewModel`: Analytics, measurements, and progress tracking
- `SettingsViewModel`: App configuration and preferences
- `AICoachViewModel`: AI coaching integration
- `AchievementViewModel`: Achievement system

#### UI Architecture
- **Navigation**: Bottom tab navigation with 4 main screens
- **Screens**: HomeScreen, WorkoutScreen, ProgressScreen, AICoachScreen
- **Theme**: Material3 with light/dark theme support
- **Components**: Reusable UI components with Material Symbols icons

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose + Material3
- **Architecture**: MVVM + Repository Pattern
- **Database**: Room (SQLite) with type converters
- **AI Integration**: OpenAI API with Retrofit/OkHttp
- **Navigation**: Compose Navigation with bottom tabs
- **Icons**: Material Symbols (outlined style, 24dp standard)
- **Async**: Kotlin Coroutines
- **JSON**: Gson for data parsing
- **Security**: AndroidX Security Crypto for API key storage

## Phased Development Approach

The project follows a strict phased development strategy defined in `phases.md`:

### Phase 1: Foundation (COMPLETED)
- Basic workout logging with sets, reps, weight, RPE
- Exercise database integration from assets/exercises.json
- 4-tab navigation with Material3 UI
- Settings screen with theme and API key management

### Phase 2: Program Management (COMPLETED)
- 90-day Push/Pull/Legs program from assets/program-json.json
- Linear progression based on RPE feedback
- Exercise substitution within muscle groups
- Program day tracking and advancement

### Phase 3: Analytics & Body Tracking (COMPLETED)
- Progress charts and workout analytics
- Body measurements with goal tracking
- Monthly calendar with workout status
- Personal records and achievement system

### Phase 4: AI Coaching (COMPLETED)
- OpenAI API integration with natural language coaching
- Post-workout analysis and recommendations
- Chat interface for training questions
- Weekly progress reviews with insights

## Data Files & Asset Management

### Required Assets (in app/src/main/assets/)
- **exercises.json**: 800+ exercises from free-exercise-db format
- **motivational-quotes.json**: Home screen motivational content
- **program-json.json**: 90-day PPL program structure

### Asset Loading Architecture
- `ExerciseDataLoader`: Loads and processes exercise database
- `ProgramDataLoader`: Loads 90-day program templates
- Background loading with Room database caching
- Error handling for missing or corrupted asset files

## Material3 UI System

### Design System
- **Icons**: Material Symbols Outlined, standardized at 24dp
- **Motion**: Spring physics with consistent animation timing
- **Interactive Components**: Enhanced with scale transforms and elevation changes
- **Color Scheme**: Dynamic Material3 colors with theme support
- **Typography**: Material3 typography scale throughout

### Interactive Components (in InteractiveComponents.kt)
- `InteractiveCard`: Enhanced card with press animations
- `InteractiveButton`: Button with spring physics feedback
- `InteractiveIconButton`: 48dp touch targets with scale animations
- `AnimatedProgressIndicator`: Smooth progress transitions

### Accessibility
- 48dp minimum touch targets
- Proper content descriptions for all icons
- Material3 color contrast compliance
- Screen reader support

## Database Schema & Migrations

### Entity Relationships
- **Workouts**: WorkoutSession → WorkoutSet (one-to-many)
- **Programs**: ProgramEnrollment → ProgramTemplate → ProgramExercise
- **User Progress**: UserProgression tracks program advancement
- **Substitutions**: ExerciseSubstitution and DaySubstitution for customization
- **Measurements**: BodyMeasurement with Goal tracking
- **Achievements**: AchievementEntity with UserAchievementEntity progress

### Migration Strategy
- Version 1→2: Initial program management tables
- Version 2→3: Added exercise substitution system
- Version 3→4: Body measurements and goals
- Version 4→5: Achievement system integration
- Fallback to destructive migration for development builds

## API Integration

### OpenAI Integration (AIService.kt)
- Retrofit with OkHttp for HTTP client
- API key stored in Android Keystore via SettingsRepository
- Request/response models in OpenAIModels.kt
- Error handling for network failures and API limits
- Offline fallbacks for core functionality

### Security
- API keys encrypted using AndroidX Security Crypto
- No sensitive data in version control
- Proper ProGuard rules for production builds

## Testing Strategy

### Unit Tests
- Repository layer testing with mock DAOs
- ViewModel testing with test coroutines
- Data loader testing for asset parsing
- Business logic validation

### Integration Tests
- Database migrations testing
- End-to-end workout flow testing
- API integration testing with mock responses
- UI navigation testing

## Performance Considerations

### Database Performance
- Proper indexing on frequently queried columns
- Lazy loading for large datasets
- Efficient queries with Room-generated SQL
- Background threading for all database operations

### UI Performance
- Lazy composition for scrollable lists
- Efficient recomposition with proper state management
- Hardware-accelerated animations
- Memory-efficient image and icon loading

### Memory Management
- Proper lifecycle-aware components
- Repository singleton pattern
- Cleanup of animation resources
- Efficient coroutine scoping

## Development Workflow

### Adding New Features
1. Define database entities and migrations if needed
2. Create or update repository with business logic
3. Update ViewModel with UI state management
4. Implement UI components following Material3 guidelines
5. Add proper error handling and loading states
6. Write unit tests for business logic
7. Test accessibility and performance

### Modifying Existing Features
1. Check migration strategy if database changes are needed
2. Update repository and ViewModel layers
3. Maintain existing UI patterns and component consistency
4. Verify backward compatibility and data persistence
5. Update tests to cover changes

## Code Quality Standards

### Kotlin Style
- Use data classes for models and entities
- Prefer sealed classes for state management
- Implement proper null safety
- Use coroutines for async operations
- Follow standard Kotlin naming conventions

### Architecture Patterns
- Maintain strict separation of concerns
- Use Repository pattern for data access
- Implement proper error handling at each layer
- Follow MVVM pattern with ViewModels
- Use Compose state management best practices

### Material3 Compliance
- Use only official Material3 components
- Follow Material Design guidelines
- Implement proper theme support
- Maintain accessibility standards
- Use Material Symbols icon system

The codebase is mature and production-ready, with all four phases implemented and a comprehensive achievement system, AI coaching, and polished UI interactions.
- always run build command after every sucessful integration to verify compile issues.