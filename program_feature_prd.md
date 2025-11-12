# Program Feature - Product Requirements Document (PRD)

## 1. Executive Summary

The Program feature introduces structured, time-bound workout programs to the workout app, complementing the existing Routines feature. Unlike Routines which are flexible, standalone workouts, Programs provide goal-oriented, progressive training plans with specific durations (4-26 weeks) and day-by-day structure. Users can enroll in pre-built programs (Fat Loss, Muscle Building, etc.) or create custom programs using existing routines.

## 2. Background & Problem Statement

### Current State
- Users have access to individual routines but lack structured, progressive training plans
- No guidance for achieving specific fitness goals over extended periods
- Users struggle with workout consistency and progression planning

### Problem Statement
Users need structured, time-bound training programs that provide clear progression toward specific fitness goals, with the flexibility to customize and adapt to their schedules.

## 3. Goals & Success Metrics

### Primary Goals
- Increase user engagement and retention through structured programs
- Provide clear progression paths for different fitness goals
- Maintain flexibility while offering structure

### Success Metrics
- Program completion rate (target: >60%)
- User retention improvement (target: +25% for program users)
- Daily active users increase (target: +15%)
- Custom program creation rate (target: 30% of active users create custom programs)

## 4. User Personas & Use Cases

### Primary Personas
1. **Goal-Oriented User**: Wants structured approach to fat loss/muscle gain
2. **Beginner**: Needs guided progression and clear structure
3. **Advanced User**: Wants to create custom programs for specific goals

### Key Use Cases
- Enroll in a 12-week muscle building program
- Create custom program mixing strength and cardio routines
- Skip workout days while maintaining program structure
- Track progress through a structured program
- Switch between different programs

## 5. Feature Requirements

### 5.1 Program Discovery & Navigation

#### Navigation Integration
- **Location**: Toggle switch between "Routines" and "Programs" in workout screen
- **Bottom Navigation**: Change "Workout" to "Program" when user has active program enrollment
- **Flexibility**: Users can switch between program view and routine view anytime
- **Default View**: Programs view shows available programs in categories
- **Search**: Filter by goal, experience level, duration, equipment

#### Program Categories (Phase 1)
Based on Athlean-X structure:

**By Goal:**
- Fat Loss Programs
- Muscle Building Programs  
- General Fitness Programs

**By Experience Level:**
- Beginner (0-6 months experience)
- Intermediate (6 months - 2 years experience)
- Advanced (2+ years experience)

### 5.2 Pre-Built Programs (Phase 1)

#### Program Templates
**1. Fat Loss Programs**
- Beginner: 8-week bodyweight + light equipment program
- Intermediate: 12-week mixed strength + cardio program  
- Advanced: 16-week high-intensity program

**2. Muscle Building Programs**
- Beginner: 12-week full-body program (3x/week)
- Intermediate: 12-week push/pull/legs split (4-5x/week)
- Advanced: 16-week specialized muscle-building program

**3. General Fitness Programs**
- Beginner: 8-week foundational fitness program
- Intermediate: 12-week athletic performance program
- Advanced: 20-week comprehensive fitness program

#### Program Structure (Based on Athlean-X Research)
- **Duration**: 4-26 weeks
- **Frequency**: 3-6 workouts per week
- **Workout Types**: Strength training, conditioning, rest days, active recovery
- **Progression**: Built-in progressive overload and exercise progression

### 5.3 Program Enrollment & Management

#### Enrollment Process
1. User browses available programs
2. Views program details (description, duration, equipment needed, experience level)
3. Clicks "Enroll" button
4. System checks if user is already enrolled in another program
5. If enrolled elsewhere, prompt to unenroll from current program
6. Confirmation of enrollment
7. Program starts on first workout completion (Day 1)

#### Single Program Limitation
- Users can only be enrolled in ONE program at a time
- To switch programs: Must unenroll from current program → lose all progress → enroll in new program → start from Day 1
- Clear warning message about progress loss when switching

### 5.4 Program Execution & Progress Tracking

#### Daily Structure
- Each program day has:
  - Assigned routine (pre-defined for template programs)
  - Rest day designation
  - Active recovery designation
  - Program day number (Day 1, Day 2, etc.)

#### Progress Tracking
- **Program Timeline**: Visual progress bar showing current day vs. total days
- **Completion Status**: Track completed vs. skipped vs. upcoming days
- **Workout History Integration**: Program workouts appear in standard workout history with program context

#### Skip Day Functionality
- **Skip Options**: Users can mark a day as "Skipped"
- **Automatic Adjustment**: When day is skipped:
  - All remaining workouts shift forward by 1 day
  - Program end date extends by 1 day
  - Skipped day logged in workout history as "Program Day X - Skipped"
- **No Limits**: Unlimited skips allowed (user responsibility)
- **Visual Indication**: Skipped days clearly marked in program timeline

### 5.5 Custom Program Creation

#### Creation Process
1. **Program Setup**
   - Program Name (required)
   - Description (required)
   - Goal selection (Fat Loss, Muscle Building, General Fitness, Other)
   - Experience level (Beginner, Intermediate, Advanced)
   - Equipment needed (multi-select from predefined list)
   - Duration in weeks (4-26 weeks)

2. **Day-by-Day Assignment**
   - Weekly view with 7 days
   - For each day, assign:
     - Existing routine from user's routine library
     - Rest day
     - Active recovery day
   - Copy week functionality for repeated patterns
   - Drag-and-drop interface for routine assignment

3. **Program Review & Save**
   - Preview entire program structure
   - Edit individual days
   - Save as personal program (not shareable in Phase 1)

#### Custom Program Constraints
- Must use existing routines from user's library
- Cannot create new routines within program creation flow
- Must assign something to each day (routine, rest, or active recovery)
- At least 1 workout day per week required

### 5.6 Program Details & Metadata

#### Program Information Display
**Pre-built Programs:**
- Program name and goal
- Duration (X weeks, Y total workout days)
- Experience level indicator
- Equipment requirements list
- Frequency (X workouts per week)
- Program description and benefits
- Sample week view

**Custom Programs:**
- All above fields (user-defined)
- "Created by You" indicator
- Creation/last modified date

#### Equipment Requirements
Standardized equipment categories based on research:
- Bodyweight Only
- Dumbbells
- Barbell + Weights
- Pull-up Bar
- Resistance Bands
- Bench
- Squat Rack
- Cable Machine
- Cardio Equipment
- Other/Specialized

### 5.7 User Interface Requirements

#### Program List View
- Card-based layout showing programs
- Filter/sort by goal, experience level, duration
- Visual indicators for enrolled program
- Quick info: duration, frequency, equipment level

#### Program Detail View
- Hero section with program overview
- Detailed description and goals
- Week-by-week breakdown (collapsible)
- Equipment requirements
- Enroll/Unenroll button
- Reviews/ratings placeholder (future feature)

#### Active Program Dashboard
- Current day information
- Progress bar (Day X of Y)
- Today's workout quick access
- Skip day option
- Program timeline/calendar view
- Quick stats: days completed, days skipped, estimated completion date

#### Program Creation Flow
- Multi-step wizard interface
- Clear progress indication
- Save draft functionality
- Preview mode before final save

## 6. Technical Requirements

### 6.1 Data Model

#### Program Entity
```
Program {
  id: string
  name: string
  description: string
  goal: enum (fat_loss, muscle_building, general_fitness, other)
  experience_level: enum (beginner, intermediate, advanced)
  equipment_required: array[string]
  duration_weeks: number (4-26)
  is_custom: boolean
  created_by: string (user_id for custom programs)
  created_at: datetime
  updated_at: datetime
  program_days: array[ProgramDay]
}

ProgramDay {
  day_number: number
  routine_id: string (nullable)
  day_type: enum (workout, rest, active_recovery)
  week_number: number
  day_of_week: number (1-7)
}
```

#### User Program Enrollment
```
UserProgramEnrollment {
  id: string
  user_id: string
  program_id: string
  enrolled_at: datetime
  started_at: datetime (nullable, set on first workout)
  current_day: number
  status: enum (enrolled, active, completed, cancelled)
  estimated_completion_date: datetime
  actual_completion_date: datetime (nullable)
  days_skipped: array[number]
}
```

#### Program Progress Tracking
```
ProgramDayCompletion {
  id: string
  user_enrollment_id: string
  program_day_number: number
  completion_date: datetime
  status: enum (completed, skipped)
  workout_session_id: string (nullable, if completed)
}
```

### 6.2 API Endpoints

#### Program Management
- `GET /programs` - List available programs with filters
- `GET /programs/{id}` - Get program details
- `POST /programs` - Create custom program
- `PUT /programs/{id}` - Update custom program
- `DELETE /programs/{id}` - Delete custom program

#### Program Enrollment
- `POST /programs/{id}/enroll` - Enroll in program
- `DELETE /programs/{id}/unenroll` - Unenroll from program
- `GET /users/{id}/current-program` - Get user's current program
- `POST /programs/{id}/skip-day` - Skip current day
- `GET /programs/{id}/progress` - Get program progress

### 6.3 Integration Points

#### Integration Points

#### Existing Systems Integration
- **Routines System**: Program creation references existing routines
- **Workout History**: Program workouts logged with program context
- **User Profile**: Program enrollment status and preferences
- **Analytics**: Track program engagement and completion metrics
- **Dashboard Integration**: Program progress widgets and stats on main dashboard
- **Progress Screen**: Program-specific progress tracking, completion rates, and timeline
- **Bottom Navigation**: Dynamic navigation label based on program enrollment status

#### Data Migration
- No migration needed (new feature)
- Existing routines remain unchanged
- Workout history structure may need extension for program context

## 7. UX/UI Specifications

### 7.1 Visual Design Requirements
- Consistent with existing app design system
- Progress visualizations (progress bars, calendars)
- Clear program status indicators
- Responsive design for all screen sizes

### 7.2 User Flow Diagrams
1. Program Discovery → Program Details → Enrollment → Program Execution
2. Custom Program Creation → Day Assignment → Review → Save
3. Program Execution → Skip Day → Progress Update → Timeline Adjustment

### 7.3 Accessibility Requirements
- Screen reader compatibility
- High contrast mode support
- Touch target size compliance
- Clear visual hierarchy

## 8. Implementation Plan

### Phase 1: Core Program Feature (MVP)
**Timeline: 8-10 weeks**

**Week 1-2: Backend Development**
- Database schema implementation
- Core API endpoints
- Program templates data seeding

**Week 3-4: Frontend - Program Discovery**
- Programs list and detail views
- Navigation integration
- Program enrollment flow

**Week 5-6: Frontend - Program Execution**
- Active program dashboard
- Progress tracking
- Skip day functionality

**Week 7-8: Custom Program Creation**
- Program creation wizard
- Day assignment interface
- Program management

**Week 9-10: Testing & Polish**
- Integration testing
- User acceptance testing
- Bug fixes and polish

### Phase 2: Enhanced Features (Future)
- Program sharing and community features
- Advanced analytics and insights
- Program recommendations based on user data
- Integration with wearable devices
- Social features (leaderboards, challenges)

## 9. Success Criteria & KPIs

### Launch Success Metrics (30 days post-launch)
- 40% of active users explore Programs feature
- 25% of active users enroll in at least one program
- 15% of users create custom programs
- Average program adherence rate >50%

### Long-term Success Metrics (90 days post-launch)
- Program completion rate >60%
- User retention increase of 25% for program participants
- Daily active users increase of 15%
- Average session duration increase of 20%

### Quality Metrics
- Program feature crashes <0.1%
- Data synchronization accuracy >99.9%
- User satisfaction rating >4.5/5 for Programs feature

## 10. Risk Assessment & Mitigation

### Technical Risks
**Risk**: Complex state management for program progression
**Mitigation**: Implement robust testing, clear data models, and fallback mechanisms

**Risk**: Performance impact from additional data queries
**Mitigation**: Implement caching strategies, optimize database queries

### User Experience Risks
**Risk**: Feature complexity overwhelming users
**Mitigation**: Progressive disclosure, excellent onboarding, clear documentation

**Risk**: User confusion between Routines and Programs
**Mitigation**: Clear visual distinction, tooltips, help documentation

### Business Risks
**Risk**: Low adoption rate
**Mitigation**: Strong onboarding experience, compelling pre-built programs, user education

## 11. Future Considerations

### Potential Enhancements
- AI-powered program recommendations
- Community-created program marketplace
- Integration with nutrition tracking
- Wearable device synchronization
- Video workout integration
- Real-time coaching features

### Scalability Considerations
- Program template versioning system
- Multi-language support for international expansion
- Enterprise/trainer accounts for program creation
- Integration with fitness equipment APIs

---

*This PRD serves as the foundational document for implementing the Program feature. Regular reviews and updates will be conducted based on user feedback and technical discoveries during implementation.*