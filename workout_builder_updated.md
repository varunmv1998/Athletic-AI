# Custom Workout Builder - Updated Feature Document (Hevy/Strong Flow)

## 1. Core Architecture Change

**Key Difference:** Hevy/Strong use a **Template/Routine** system, not a Program hierarchy.

**New Structure:**
- **Routines/Templates** (standalone workout templates)
- **Folders** (optional organization for routines)
- **Active Workout Session** (instance of a routine being performed)

**No longer using:** Program → Workout → Exercises hierarchy

---

## 2. Data Architecture (Revised)

### 2.1 Database Schema

**Folder Entity:**
- id: String (UUID)
- name: String (e.g., "AX90", "My Routines")
- createdDate: Long
- isExpanded: Boolean (UI state)

**WorkoutRoutine Entity:**
- id: String (UUID)
- name: String (e.g., "Strong 5x5 - Workout B", "Legs")
- folderId: String (nullable, for organization)
- lastPerformed: Long (timestamp, nullable)
- exercises: List<RoutineExercise>
- createdDate: Long
- notes: String (optional)

**RoutineExercise Entity:**
- id: String (UUID)
- exerciseId: String (from exercise database)
- orderIndex: Int
- defaultSets: List<ExerciseSet>
- restTimerEnabled: Boolean
- restSeconds: Int (default from user settings)
- notes: String (optional)

**ExerciseSet Entity:**
- setNumber: Int
- targetReps: String (e.g., "5", "8-10", "12", "AMRAP")
- previousWeight: Float (nullable, from last workout)
- previousReps: Int (nullable, from last workout)

**ActiveWorkoutSession Entity:**
- id: String (UUID)
- routineId: String (nullable, if from routine)
- routineName: String
- startTime: Long
- exercises: List<SessionExercise>
- isActive: Boolean

**SessionExercise Entity:**
- exerciseId: String
- sets: List<PerformedSet>
- restTimerActive: Boolean
- restSeconds: Int
- notes: String

**PerformedSet Entity:**
- weight: Float
- reps: Int
- rpe: Float (optional)
- completed: Boolean
- restTimerTriggered: Boolean

---

## 3. User Interface Specifications (Updated)

### 3.1 Main Workout Tab

**Layout:**
- "Start Empty Workout" button at top (quick start)
- "Routines" section with:
  - "New Routine" button
  - "Explore" button (community templates)
  - Expandable folders (e.g., "AX90", "My Routines")
  - Routine cards showing:
    - Routine name
    - Exercise preview (first 3-4 exercises)
    - Last performed date
    - "Start Routine" button (prominent)
    - Three-dot menu for edit/delete

**Key Behaviors:**
- Tapping routine card shows full routine details
- "Start Routine" immediately begins workout session
- Folders are collapsible for organization

### 3.2 Creating a New Routine

**Step 1: Routine Creation Screen**
- "Create Routine" header
- Routine name field (auto-focused)
- Empty state with dumbbell icon
- "Add exercise" button (prominent blue)
- Save button (top right, disabled until exercises added)

**Step 2: Exercise Selection**
- Search bar at top
- Two filter buttons: "All Equipment" | "All Muscles"
- "Popular Exercises" section
- Exercise list with:
  - Exercise image/icon
  - Exercise name
  - Primary muscle group
  - Checkbox selection (right side)
- Multi-select capability
- Floating counter: "Add X exercises" button
- Recently used exercises (if available)

**Step 3: Back to Routine Builder**
- Selected exercises appear in order
- Each exercise shows:
  - Exercise icon/image
  - Name and muscle group
  - Drag handle for reordering
  - Default sets (auto-generated based on exercise type)
- "Add exercise" button remains at bottom
- Save when ready

### 3.3 Starting a Workout

**Workout Initiation:**
- Tapping "Start Routine" immediately starts
- Creates active workout session
- Shows timer at top
- No configuration step before starting

**Active Workout Screen:**
- Timer display (prominent)
- "Finish" button (top right)
- Current routine/workout name
- Exercise list with:
  - Exercise name and icon
  - "Previous" column showing last workout's performance
  - Weight input field
  - Reps input field
  - Checkbox to complete set
  - Rest timer between sets (automatic)
- "Add Set" button after each exercise
- "Add Exercise" button at bottom
- Settings and Discard buttons at bottom

**Rest Timer Behavior:**
- First-time prompt: "Add rest timers to this workout?"
  - "Add Rest Timers"
  - "Cancel"  
  - "Don't Ask Again"
- Timer appears between sets (e.g., "2:00")
- Auto-starts when set is completed
- Can be skipped/modified

### 3.4 During Workout Features

**Adding Exercises Mid-Workout:**
- "Add Exercise" button opens exercise picker
- Same interface as routine builder
- Exercises added to current session only
- Option to update routine after workout

**Set Management:**
- Tap checkbox to mark set complete
- Auto-advances to next set
- Previous workout data shown for reference
- RPE button (optional, shown after set completion)

**Bottom Controls:**
- Settings (rest timer defaults, plates calculator)
- Discard Workout (with confirmation)

---

## 4. Key Flow Differences from Original Design

### 4.1 No Program Concept
- Routines are standalone templates
- Users organize with folders, not programs
- Each routine is independently startable

### 4.2 Immediate Workout Start
- No configuration before starting
- Routine defines default structure
- Modifications happen during workout

### 4.3 Flexible Routine System
- Can start empty workout (no routine)
- Can modify routine during workout
- Changes can be saved back to routine

### 4.4 Template Library
- "Explore" section for community/preset routines
- Example templates provided (Strong 5x5, etc.)
- Users can duplicate and modify

---

## 5. User Flow (Revised)

### 5.1 Creating First Routine
1. Tap "New Routine" from main screen
2. See empty routine builder
3. Tap "Add exercise"
4. Search/filter/select multiple exercises
5. Tap "Add X exercises"
6. Return to routine with exercises listed
7. Adjust order if needed
8. Name routine
9. Save routine

### 5.2 Starting Workout from Routine
1. View routines in main tab
2. Tap "Start Routine" on desired routine
3. Workout session begins immediately
4. Log sets as you complete them
5. Rest timer prompts on first use
6. Add/modify exercises as needed
7. Finish workout when complete

### 5.3 Empty Workout Flow
1. Tap "Start Empty Workout"
2. Blank workout session starts
3. Add exercises on the fly
4. Log sets
5. Optionally save as new routine when finished

---

## 6. Settings & Preferences

### 6.1 Global Settings
- Default rest timer duration
- Auto-start rest timer (on/off)
- Weight increment preferences
- Plate calculator settings

### 6.2 Per-Routine Settings
- Override rest timer for specific routine
- Notes for routine
- Target workout duration

---

## 7. Critical Implementation Details

### 7.1 State Management
- Active workout persists if app closes
- Draft routines saved automatically
- Last workout data cached for "Previous" column

### 7.2 Exercise Database
- Must support instant search (< 100ms)
- Popular exercises pre-indexed
- Recent exercises tracked per user
- Images/icons for visual recognition

### 7.3 Performance Requirements
- Routine list loads instantly (cached)
- Exercise search returns results immediately
- Smooth drag-to-reorder (60fps)
- No lag when checking off sets

---

## 8. Key Differences from Original Spec

**Removed:**
- Program hierarchy concept
- Pre-workout configuration screens
- Complex validation rules
- RPE as required field
- Exercise details modal (uses inline selection)

**Added:**
- Folder organization
- Immediate workout start
- Rest timer prompts
- Previous workout display
- Mid-workout exercise addition
- Template exploration
- Routine duplication

**Changed:**
- Simplified data model (routines not programs)
- Streamlined exercise selection (multi-select)
- Dynamic set addition during workout
- Optional configuration (happens in-workout)

---

## 9. Success Metrics

### 9.1 User Experience Goals
- Time to start workout: < 5 seconds from app open
- Time to create routine: < 2 minutes for 5 exercises
- Zero configuration required before working out
- Seamless mid-workout modifications

### 9.2 Technical Performance
- Instant routine loading
- Sub-100ms exercise search
- Smooth animations throughout
- Reliable offline functionality

---

## 10. Migration Path

For existing users with programs:
1. Convert each workout to a routine
2. Create folder with program name
3. Place converted routines in folder
4. Maintain workout history

---

## Notes for Implementation

The key insight from Hevy/Strong is that users want to **start working out immediately**. The routine/template system provides structure without friction. Configuration happens during the workout, not before. This approach prioritizes action over planning, which aligns with how people actually work out.