# Implementation Prompt: Convert to Routine-Based Workout System

## Context
I have a workout app that currently has a broken "Custom Programs" feature that only allows creating program names/descriptions without any actual workout functionality. I want to completely remove the program concept and implement a simpler routine-based system that matches how Hevy and Strong apps work.

## Current State
- App has a "Custom Programs" screen that doesn't work properly
- Can only add program name and description
- No way to add exercises or start workouts
- Main workout tab exists but doesn't show custom content
- Exercise database with 800+ exercises is available
- Basic workout logging functionality exists

## Target Implementation

### Core Concept Change
**Remove entirely:** Programs → Workouts → Exercises hierarchy
**Implement instead:** Standalone Routines with exercises that can be started directly

### 1. Data Model Requirements

Create these entities:
```kotlin
data class WorkoutRoutine(
    val id: String,
    val name: String,
    val exercises: List<RoutineExercise>,
    val lastPerformed: Long? = null,
    val createdDate: Long,
    val folder: String? = null // optional organization
)

data class RoutineExercise(
    val exerciseId: String,
    val exerciseName: String,
    val primaryMuscle: String,
    val sets: List<ExerciseSet>,
    val orderIndex: Int,
    val restSeconds: Int = 120
)

data class ExerciseSet(
    val setNumber: Int,
    val targetReps: String, // "8-10", "12", "5"
    val previousWeight: Float? = null,
    val previousReps: Int? = null
)
```

### 2. Screen Modifications

#### A. Replace "Custom Programs" with "Routines" Screen
Navigation: Workout tab → Routines button

Layout:
```
Routines                           [+]

Quick Start
[+ New Routine]    [Explore]

My Routines (3)
┌─────────────────────────────────┐
│ Push Day A                      │
│ Chest, Shoulders, Triceps       │
│ 6 exercises • Last: 2 days ago  │
│ [Start Routine]                 │
└─────────────────────────────────┘
```

#### B. New Routine Creation Flow

**Step 1: Empty Routine Screen**
```
Create Routine            [Cancel] [Save]

Routine Name: [_______________]

[Icon: Dumbbell]
Get started by adding exercises

[+ Add Exercises]
```

**Step 2: Exercise Selection (Multi-select)**
```
Select Exercises              [Add (3)]

[Search...]
[All Equipment ▼] [All Muscles ▼]

☑ Bench Press (Barbell) • Chest
☑ Overhead Press • Shoulders  
□ Squat (Barbell) • Legs
☑ Triceps Extension • Arms
□ Deadlift • Back
```

**Step 3: Exercise Configuration**
```
Create Routine                    [Save]

Routine Name: [Push Day A]

Bench Press (Barbell)        [↕] [🗑]
3 sets × 5 reps • 3 min rest

Overhead Press               [↕] [🗑]
3 sets × 8-10 reps • 2 min rest

Triceps Extension            [↕] [🗑]
3 sets × 12-15 reps • 90s rest

[+ Add More Exercises]
```

#### C. Home Screen Modifications

Current: Shows programs (broken)
New: Shows quick workout access

```
Home Screen
━━━━━━━━━━━━━━━━━━

Today's Workout
[Start Empty Workout]

Recent Routines
┌─────────────────────────┐
│ Push Day A              │
│ Last: 2 days ago        │
│ [Quick Start]           │
└─────────────────────────┘

Quick Actions
• Create New Routine
• Browse All Routines
• Workout History
```

#### D. Workout Tab Redesign

```
Workout Tab
━━━━━━━━━━━━

[Start Empty Workout]

My Routines                    [See All]
┌─────────────────────────┐
│ Push Day A              │
│ 6 exercises             │
│ [Start]                 │
└─────────────────────────┘
┌─────────────────────────┐
│ Pull Day A              │
│ 5 exercises             │
│ [Start]                 │
└─────────────────────────┘

[+ Create New Routine]
```

### 3. User Flows to Implement

#### Flow A: Create Routine
1. Workout tab → "+ Create New Routine"
2. Empty routine screen → Add exercises
3. Multi-select exercises from list
4. Auto-generate default sets/reps based on exercise type
5. Allow reordering via drag handles
6. Save routine

#### Flow B: Start Routine
1. Workout tab → Tap "Start" on routine
2. Immediately begin workout timer
3. Show all exercises with previous weights
4. Log sets as completed
5. Finish workout → Update routine's lastPerformed

#### Flow C: Edit Routine
1. Long press routine → Edit option
2. Add/remove/reorder exercises
3. Modify sets/reps/rest
4. Save changes

### 4. Implementation Priority

**Phase 1: Core Functionality**
1. Remove all "Program" related code and UI
2. Create Routine data model and repository
3. Build routine creation screen with exercise selection
4. Implement routine list display in Workout tab

**Phase 2: Workout Execution**
1. Start routine → active workout session
2. Display exercises with set tracking
3. Save workout history
4. Update routine's last performed date

**Phase 3: Polish**
1. Drag to reorder exercises
2. Edit existing routines
3. Duplicate routines
4. Search/filter routines

### 5. Specific Code Changes

#### Remove these files/components:
- CustomProgramScreen
- ProgramRepository
- Program data models
- Any program-related navigation

#### Add these new components:
```kotlin
// Screens
@Composable fun RoutineListScreen()
@Composable fun CreateRoutineScreen()
@Composable fun ExerciseSelectionScreen()
@Composable fun ActiveWorkoutScreen()

// ViewModels
class RoutineViewModel
class ExerciseSelectionViewModel
class ActiveWorkoutViewModel

// Repository
class RoutineRepository {
    fun createRoutine(routine: WorkoutRoutine)
    fun getRoutines(): Flow<List<WorkoutRoutine>>
    fun updateRoutine(routine: WorkoutRoutine)
    fun deleteRoutine(id: String)
}
```

### 6. Navigation Graph Updates

```kotlin
// Replace program navigation with:
composable("routines") { RoutineListScreen() }
composable("create_routine") { CreateRoutineScreen() }
composable("select_exercises") { ExerciseSelectionScreen() }
composable("active_workout/{routineId}") { ActiveWorkoutScreen() }
```

### 7. Testing Checklist

- [ ] Can create routine with multiple exercises
- [ ] Routine appears in Workout tab
- [ ] Can start routine immediately
- [ ] Exercises show with sets/reps
- [ ] Can complete workout and save
- [ ] Last performed date updates
- [ ] Can edit existing routine
- [ ] Can delete routine
- [ ] Empty workout still works

## Deliverables

1. Complete removal of program-related code
2. Working routine creation with exercise selection
3. Routine management (create, edit, delete)
4. Integration with Workout tab
5. Ability to start and complete routines
6. Updated home screen with routine quick access

## Success Criteria

- User can create a routine in under 60 seconds
- Starting a routine takes one tap
- No configuration required before workout starts
- All routine data persists correctly
- Smooth transition from program to routine system