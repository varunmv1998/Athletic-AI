# Custom Workout Builder - Complete Feature Document

## 1. Feature Overview

**Purpose:** Enable users to create personalized training programs using the complete free-exercise-db dataset (800+ exercises) as an alternative to the fixed 90-day PPL program.

**Core Architecture:** Program → Workout → Exercises (three-tier hierarchy)

**Integration:** Replaces existing 90-day program when activated, maintains compatibility with current workout logging system.

---

## 2. Data Architecture

### 2.1 Database Schema

**CustomProgram Entity:**
- id: String (UUID)
- name: String (user-defined program name)
- description: String (optional program notes)
- isActive: Boolean (only one active program allowed)
- createdDate: Long (timestamp)
- totalWorkouts: Int (calculated field)

**CustomWorkout Entity:**
- id: String (UUID)
- programId: String (foreign key to CustomProgram)
- name: String (e.g., "Push Day 1", "Upper Body")
- dayNumber: Int (ordering within program)
- estimatedDuration: Int (minutes, calculated from exercises and rest)
- exerciseCount: Int (calculated field)

**WorkoutExercise Entity:**
- id: String (UUID)
- workoutId: String (foreign key to CustomWorkout)
- exerciseId: String (reference to exercise from free-exercise-db)
- orderIndex: Int (exercise position within workout)
- targetSets: Int (1-10 range)
- targetReps: String (e.g., "8-10", "12", "AMRAP")
- rpeTarget: Float (6.0-10.0 range, 0.5 increments)
- restSeconds: Int (30-300 seconds range)

### 2.2 Data Migration

**Exercise Database Import:**
- Import complete free-exercise-db JSON structure
- Create search indexes on: name, primaryMuscles, secondaryMuscles, equipment
- Add frequently used exercises tracking table
- Maintain exercise instruction text without images/media

---

## 3. User Interface Specifications

### 3.1 Entry Point

**Location:** Existing Workout tab
**Access Method:** "Create Custom Program" button alongside current program options
**Visual Treatment:** Distinct call-to-action button with custom workout icon

### 3.2 Exercise Selection Screen

**Layout Structure:**
- Search bar at top with real-time filtering
- Filter chips below search: Muscle Group, Equipment, Exercise Type
- Recently used exercises section (collapsible)
- Infinite scroll exercise list with multi-select checkboxes
- Floating action button showing selected count

**Exercise List Item:**
- Exercise name (primary text)
- Primary muscle groups (secondary text)
- Equipment required (tertiary text)
- Selection checkbox on the right
- Tap to select, long press for exercise details modal

**Filtering Options:**
- **Muscle Groups:** Chest, Back, Shoulders, Arms, Legs, Core, Full Body
- **Equipment:** Barbell, Dumbbell, Cable, Machine, Bodyweight, None
- **Exercise Type:** Compound, Isolation, Cardio

**Performance Features:**
- Debounced search (300ms delay)
- Virtual scrolling for 800+ exercises
- Recently used exercises cached and prioritized
- Search result highlighting

### 3.3 Exercise Details Modal

**Content:**
- Exercise name as header
- Primary and secondary muscle groups
- Equipment requirements
- Complete exercise instructions
- Movement pattern classification
- Close button and "Add to Workout" action button

### 3.4 Exercise Review & Configuration Screen

**Layout:**
- Selected exercises list with drag handles for reordering
- Each exercise shows: name, muscle group, current configuration
- Tap any exercise to open inline configuration panel
- "Estimate Duration" display at top
- "Next Step" button when all exercises configured

**Configuration Panel (per exercise):**
- Sets input: Number stepper (1-10 range)
- Reps input: Text field with validation (1-100 range or "AMRAP")
- RPE target: Slider with 0.5 increments (6.0-10.0)
- Rest time: Preset chips (30s, 60s, 90s, 120s, 180s, 300s) plus custom
- Remove exercise option with confirmation

**Duration Calculation:**
- Base time per set: 60 seconds
- Rest time between sets: as configured
- Total = (sets × 60s + (sets-1) × rest) × number of exercises
- Display format: "Estimated: 45-60 minutes"

### 3.5 Workout Naming & Saving Screen

**Components:**
- Workout name input field with character limit (50 chars)
- Program name input (if first workout in new program)
- Exercise summary list (read-only)
- Duration estimate display
- Save button with validation

**Default Naming:**
- Auto-suggest based on primary muscle groups
- Examples: "Chest & Triceps", "Back & Biceps", "Full Body"

---

## 4. User Flow Details

### 4.1 Complete User Journey

**Step 1: Program Creation Entry**
1. User taps "Create Custom Program" in Workout tab
2. System checks if active custom program exists
3. If exists: offer to "Add Workout" or "Create New Program"
4. If none: proceed to exercise selection

**Step 2: Exercise Selection**
1. Display exercise selection screen with 800+ exercises
2. User searches/filters to find desired exercises
3. User selects multiple exercises (minimum 1 required)
4. Recently used exercises appear prominently at top
5. Selected count shown in floating action button
6. User proceeds to review screen

**Step 3: Exercise Configuration**
1. Display selected exercises in list format
2. User can drag to reorder exercises
3. User taps each exercise to configure sets/reps/RPE/rest
4. Real-time duration estimation updates
5. All exercises must be configured to proceed

**Step 4: Workout Finalization**
1. User enters workout name
2. If first workout: user enters program name
3. System validates all inputs
4. User saves workout and program
5. System activates custom program (replaces 90-day program)

### 4.2 Program Management Flow

**Active Program Switching:**
1. User creates custom program
2. System prompts: "Switch to custom program? This will pause your current program."
3. User confirms switch
4. Custom program becomes active
5. Current program progress saved for potential resumption

**Program Overview Access:**
1. From Workout tab, show active custom program
2. Display: program name, total workouts, current workout
3. Options: View all workouts, Add workout, Edit program, Delete program

---

## 5. Validation Rules

### 5.1 Exercise Level Validation
- Minimum 1 exercise per workout
- Maximum 20 exercises per workout
- Sets: 1-10 range only
- Reps: 1-100 numeric range or "AMRAP" text
- RPE: 6.0-10.0 with 0.5 increments
- Rest time: 30-300 seconds range

### 5.2 Workout Level Validation
- Workout name: 1-50 characters, no special characters
- Estimated duration: maximum 180 minutes warning
- Duplicate exercise warning (allow but warn user)

### 5.3 Program Level Validation
- Program name: 1-50 characters
- Minimum 1 workout per program
- Maximum 50 workouts per program
- Unique workout names within program

---

## 6. Integration Requirements

### 6.1 Existing System Compatibility
- Custom workouts integrate with existing WorkoutSession logging
- Progression tracking applies to custom exercises
- AI Coach can analyze custom workout performance
- Progress charts include custom workout data
- Export functionality includes custom program data

### 6.2 Data Persistence
- Custom programs survive app updates
- Local storage only (no cloud sync initially)
- Export/import capability for backup
- Program sharing via JSON export (future feature)

---

## 7. Performance Requirements

### 7.1 Response Time Targets
- Exercise search results: < 500ms
- Screen transitions: < 300ms
- Exercise reordering: < 100ms per move
- Duration calculation: < 50ms (real-time)

### 7.2 Memory Management
- Virtualized scrolling for exercise lists
- Lazy loading of exercise details
- Efficient caching of recently used exercises
- Background cleanup of unused exercise data

---

## 8. Error Handling

### 8.1 User Error Scenarios
- Invalid input values: inline validation with clear messaging
- Network unavailable: fully offline functionality
- Corrupted program data: recovery options with backup
- Exceeding limits: clear feedback on constraints

### 8.2 System Error Recovery
- Database errors: graceful degradation with retry options
- Memory issues: progressive data loading
- Performance issues: loading indicators and optimization

---

## 9. Future Enhancements (Out of Scope)

### 9.1 Advanced Features
- Superset and circuit support
- Community program sharing
- AI-generated workout suggestions
- Video exercise demonstrations
- Advanced periodization schemes

### 9.2 Social Features
- Program rating and reviews
- Community template library
- Workout sharing with friends
- Leaderboards and challenges

---

## 10. Success Criteria

### 10.1 Functional Requirements
- Users can create custom programs with multiple workouts
- Exercise selection is responsive with 800+ exercise database
- Custom programs integrate seamlessly with existing workout logging
- All validation prevents invalid configurations
- Duration estimation accuracy within 10-15% of actual workout time

### 10.2 User Experience Goals
- Exercise selection feels as smooth as Hevy/Strong apps
- Configuration process is intuitive and efficient
- No performance degradation compared to current app functionality
- Users can successfully create and complete custom workouts