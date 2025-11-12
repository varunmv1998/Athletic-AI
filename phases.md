# Athletic AI - Phased Development Plan

## Development Strategy

Each phase builds a complete, working feature set while progressing toward the full AI-powered fitness tracker. Use these exact prompts with Claude Code for token-efficient development.

---

## PHASE 1: FOUNDATION (4-6 weeks)
**Goal:** Working fitness tracker with basic logging

### Claude Code Command:
```
Implement Athletic AI Phase 1 - Core workout tracking foundation.

PROJECT SETUP:
- Target: Android app with Jetpack Compose + Material3
- Architecture: MVVM + Repository + Room database
- Navigation: 4-tab bottom navigation + Settings FAB
- Theme: Light/Dark theme support, Metric units default

REQUIRED FEATURES:
1. Room database: Exercise, WorkoutSession, WorkoutSet entities
2. Exercise data: Load from assets/exercises.json (download from GitHub)
3. Workout sessions: Start, log sets (weight/reps/RPE 6-10), finish
4. Rest timer: Simple countdown timer (foreground only)
5. Session history: List previous workouts with basic details
6. Home screen: Dashboard with motivational quotes (pull-to-refresh)
7. Settings: OpenAI API key storage, theme toggle, unit preferences

NAVIGATION STRUCTURE:
- Bottom tabs: Home, Workout, Progress, AI Coach
- Settings: Floating Action Button on all screens
- Material3 styling with proper elevation and typography

MATERIAL DESIGN 3:
- Use Material3 components throughout
- Material Symbols for icons (outlined, 24dp)
- Proper color tokens and accessibility
- Basic animations: ripples, transitions

DATA FILES NEEDED:
- Download https://raw.githubusercontent.com/yuhonas/free-exercise-db/main/exercises.json to assets/
- Use provided motivational-quotes.json
- Placeholder content for AI Coach tab

SKIP FOR PHASE 1:
- 90-day program logic
- Body measurements/profile
- Progress analytics/charts
- Real AI integration
- Background notifications

SUCCESS CRITERIA:
- Complete workout sessions with set logging
- Data persistence through app restart
- All navigation tabs functional
- Settings allow theme/API configuration
- Consistent Material3 styling
```

---

## PHASE 2: PROGRAM STRUCTURE (3-4 weeks)
**Goal:** Add 90-day PPL program with progression

### Claude Code Command:
```
Extend Athletic AI Phase 2 - Add program management and progression.

BUILDING ON: Phase 1 (basic workout logging system)

NEW FEATURES:
1. 90-day PPL program: Load from assets/program-json.json
2. Program tracking: Current day (1-90), template progression
3. Linear progression: Add 2.5kg when RPE ≤ 7.5 across sets
4. Exercise substitution: Within same muscle group
5. Program navigation: Skip workout, advance program day
6. Today's workout: Home screen shows current program day

INTEGRATION POINTS:
- Home screen: "Start Today's Workout" with program day display
- Workout screen: Program template vs custom workout options
- Exercise database: Add muscle group substitution logic
- Progress tracking: Store program day and progression data

DATABASE ADDITIONS:
- ProgramDay entity: day number, template, completion status
- UserProgress entity: current program day, start date
- Exercise substitution mappings

UI ENHANCEMENTS:
- Program progress indicator on Home
- Workout type display (Push A, Pull B, etc.)
- Exercise substitution UI during sessions
- Program overview in Workout tab

PROGRESSION LOGIC:
- Simple linear progression per exercise
- Weight increases based on RPE feedback only
- No complex periodization or advanced algorithms

Keep implementation focused on core program functionality.
```

---

## PHASE 3: ANALYTICS & BODY TRACKING (3-4 weeks)
**Goal:** Add progress visualization and body measurements

### Claude Code Command:
```
Implement Athletic AI Phase 3 - Progress analytics and body measurements.

BUILDING ON: Phase 1 + 2 (workout logging + program management)

ANALYTICS FEATURES:
1. Progress charts: Volume over time, Personal Records tracking
2. Monthly calendar: Workout status indicators with streak counter
3. Session details: Set-by-set breakdown accessible from history
4. Achievement badges: Consistency, volume PRs, exercise PRs
5. Filtering: By timeframe, muscle group, workout type

BODY MEASUREMENTS:
1. Measurement tracking: Weight, height, body fat %, waist
2. Target setting: User-defined goals with deadlines
3. Progress visualization: Progress bars toward targets
4. BMI calculation: Auto-computed from weight + height
5. Measurement history: Charts and trend analysis

HOME SCREEN INTEGRATION:
- Body measurements carousel: Weight progress, Body fat progress, Add measurement
- Quick stats integration with progress data
- Target progress indicators

CHART IMPLEMENTATION:
- Use Compose Canvas or simple charting library
- Time ranges: 4 weeks, 3 months, 6 months, 1 year
- Handle sparse data gracefully (not all users log daily)
- Interactive data points with value display

DATABASE ADDITIONS:
- BodyMetrics entity: timestamp, weight, height, body fat, waist
- PersonalRecord entity: exercise, best weight, date achieved
- Targets entity: metric type, target value, deadline

UI SCREENS:
- Progress screen: Calendar, charts, history list
- Session detail: Accessible from progress history
- Body measurement entry: Modal from Home carousel

Focus on functional analytics over visual polish.
```

---

## PHASE 4: AI INTEGRATION & POLISH (4-5 weeks)
**Goal:** Complete AI coaching and production readiness

### Claude Code Command:
```
Complete Athletic AI Phase 4 - AI Coach integration and production polish.

BUILDING ON: Phase 1-3 (complete workout tracking with analytics)

AI COACH IMPLEMENTATION:
1. OpenAI API integration: Use stored API key from Settings
2. Event queue system: Capture workout events, batch process after session
3. Post-workout analysis: Session summaries and recommendations
4. Weekly reviews: Progress trend analysis with insights
5. Chat interface: Natural language Q&A about training

AI ANALYSIS TYPES:
- Progression analysis: Weight increases, volume trends, plateau detection
- Recovery recommendations: RPE patterns, overreaching signs
- Program adjustments: Exercise substitutions, load modifications
- Goal alignment: Progress toward body measurement targets

CHAT FEATURES:
- Context-aware responses using user's workout history
- Common questions with quick-tap options
- Fallback to static responses when API unavailable
- Conversation history management

PRODUCTION POLISH:
1. Background services: Rest timer notifications during workouts
2. Advanced animations: Shared elements, container transforms
3. Error handling: Network failures, API limits, offline states
4. Performance: Smooth scrolling, memory optimization
5. App polish: Icons, splash screen, proper loading states

TECHNICAL IMPROVEMENTS:
- Foreground service for active workout sessions
- Notification channels and background processing
- Comprehensive error handling with user-friendly messages
- Data validation and edge case management
- API rate limiting and cost management

FINAL TESTING:
- Complete user flows from onboarding to AI coaching
- Performance testing on various devices
- Offline functionality verification
- AI coaching quality assessment

DELIVERABLE:
Production-ready APK with full AI coaching capabilities.
```

---

## Development Guidelines

### Between Phases:
- Test each phase thoroughly before proceeding
- Ensure all features work as expected
- Verify data persistence and app stability
- Address any critical bugs before next phase

### Data File Requirements:
- **Phase 1:** exercises.json, motivational-quotes.json
- **Phase 2:** program-json.json
- **Phase 3:** Analytics from existing workout data
- **Phase 4:** OpenAI API integration

### Success Validation:
Each phase should produce a working app that users could realistically use, even if not feature-complete.

### Token Optimization:
These prompts are designed for maximum development efficiency while maintaining quality output from Claude Code.