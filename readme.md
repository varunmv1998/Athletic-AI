# Athletic AI - Intelligent Fitness Tracker

Android fitness tracking app with AI-powered coaching, progressive workout programs, and comprehensive progress analytics.

## Development Approach

This project uses phased development with Claude Code for efficient, token-optimized development cycles.

## Phase Overview

- **Phase 1:** Core workout logging with Material3 foundation (4-6 weeks)
- **Phase 2:** 90-day PPL program with progression logic (3-4 weeks)  
- **Phase 3:** Analytics, body measurements, progress tracking (3-4 weeks)
- **Phase 4:** AI coaching integration and production polish (4-5 weeks)

## Data Dependencies

### Required Files
Download and place in `app/src/main/assets/`:

- **Exercise Database:** [exercises.json](https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises.json)
- **Motivational Quotes:** See `motivational-quotes.json` in project files
- **Program Templates:** See `program-json.json` in project files

### Quick Setup
```bash
# Download exercise database
curl -o app/src/main/assets/exercises.json https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises.json

# Create assets directory if it doesn't exist
mkdir -p app/src/main/assets/
```

## Technical Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material3
- **Architecture:** MVVM + Repository Pattern
- **Database:** Room
- **AI Integration:** OpenAI API
- **Navigation:** Compose Navigation with bottom tabs

## Development Commands

Start Phase 1 development:
```
claude code: Implement Phase 1 as specified in PHASES.md
```

## Project Structure

```
AthleticAI/
├── app/src/main/assets/          # JSON data files
├── PHASES.md                     # Phased development plan
├── .claude.md                    # Claude Code instructions
└── README.md                     # This file
```

## Features by Phase

### Phase 1: Foundation
- Workout session logging (sets, reps, weight, RPE)
- Exercise database integration
- Basic Material3 UI with 4-tab navigation
- Settings screen with theme and API key management
- Motivational quotes with pull-to-refresh

### Phase 2: Program Management
- 90-day Push/Pull/Legs program
- Linear progression based on RPE feedback
- Exercise substitution within muscle groups
- Program tracking and advancement

### Phase 3: Analytics & Body Tracking
- Progress charts and workout analytics
- Body measurements with target tracking
- Monthly calendar with workout status
- Personal records and achievement badges

### Phase 4: AI Coaching
- Post-workout analysis and recommendations
- Natural language coaching chat interface
- Weekly progress reviews with insights
- Plateau detection and program adjustments

## Design Philosophy

- **Offline-first:** Core functionality works without internet
- **Material3:** Modern Android design language with theming
- **Progressive enhancement:** Each phase builds working functionality
- **Token-efficient development:** Phased approach optimizes Claude Code usage