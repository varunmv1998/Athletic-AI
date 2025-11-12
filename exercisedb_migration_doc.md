# ExerciseDB Integration & Migration Requirements Document

## 1. Executive Summary

Migrate from local 800+ exercise database to ExerciseDB API (1400+ exercises) while maintaining all existing functionality and adding enhanced exercise data with GIF demonstrations.

## 2. ExerciseDB API Overview

### API Endpoints (No Authentication Required)
- **Base URL**: `https://exercisedb.p.rapidapi.com/v1/exercises`
- **No API Key needed** for v1.exercisedb.dev
- **Rate Limits**: None documented for self-hosted version

### Available Endpoints
1. `GET /exercises` - All 1400+ exercises
2. `GET /exercises/bodyPartList` - List of body parts
3. `GET /exercises/targetList` - List of target muscles  
4. `GET /exercises/equipmentList` - List of equipment types
5. `GET /exercises/exercise/{id}` - Single exercise by ID
6. `GET /exercises/name/{name}` - Search by name
7. `GET /exercises/bodyPart/{bodyPart}` - Filter by body part
8. `GET /exercises/equipment/{equipment}` - Filter by equipment
9. `GET /exercises/target/{target}` - Filter by target muscle

### Data Structure
```json
{
  "bodyPart": "chest",
  "equipment": "barbell",
  "gifUrl": "https://v2.exercisedb.io/image/[id]",
  "id": "0025",
  "name": "barbell bench press",
  "target": "pectorals",
  "secondaryMuscles": ["triceps", "shoulders"],
  "instructions": [
    "Lie flat on a bench...",
    "Lower the barbell slowly...",
    "Push the barbell back up..."
  ]
}
```

## 3. Migration Strategy

### Phase 1: Data Synchronization
**Objective**: Download and cache entire ExerciseDB locally

**Implementation Requirements**:
1. One-time sync on first app launch after update
2. Store all 1400+ exercises in local Room database
3. Map ExerciseDB structure to existing app structure
4. Maintain backward compatibility with existing user routines

**Data Mapping**:
| ExerciseDB Field | App Database Field | Notes |
|-----------------|-------------------|-------|
| id | exerciseId | Prefix with "edb_" to avoid conflicts |
| name | exerciseName | Format: capitalize properly |
| bodyPart | primaryMuscle | Map categories (see mapping table) |
| target | targetMuscle | New field to add |
| equipment | equipment | Direct mapping |
| gifUrl | mediaUrl | New field for animations |
| instructions | instructions | Array to formatted text |
| secondaryMuscles | secondaryMuscles | New field as JSON array |

### Phase 2: Offline Strategy

**Approach**: Progressive Enhancement
1. **Core Data** (Required): Exercise metadata cached in Room DB
2. **Media Assets** (Optional): GIF downloads for offline viewing

**Offline Modes**:
- **Basic Mode**: All exercise data except GIFs (automatic)
- **Enhanced Mode**: User-triggered GIF download per routine
- **Full Offline**: Download all GIFs (optional, ~500MB)

**Implementation**:
- Add "Download for Offline" button in routine details
- Show download progress indicator
- Cache GIFs by routine to minimize storage
- Automatic cleanup of unused GIFs after 30 days

## 4. Affected Screens & Components

### 4.1 Exercise Selection Screen
**Changes Required**:
- Update search to use new exercise names
- Add filter chips for new categories (body part, target, equipment)
- Display exercise GIF thumbnails in list
- Add "View Demo" option for each exercise

### 4.2 Routine Builder
**Changes Required**:
- Ensure exercise IDs migration for existing routines
- Display richer exercise information
- Add exercise preview capability

### 4.3 Active Workout Screen
**Changes Required**:
- Add "Show Demo" button per exercise
- Display target muscle information
- Show exercise instructions on tap

### 4.4 Exercise Detail Modal (New)
**New Component**:
- Full-screen GIF player
- Step-by-step instructions
- Target & secondary muscles diagram
- Equipment requirements

### 4.5 Settings Screen
**New Options**:
- Offline data management
- Clear exercise cache
- Re-sync exercise database
- GIF quality settings (data saver mode)

## 5. Database Schema Updates

### New Tables Required
```sql
-- Enhanced exercise table
CREATE TABLE exercises_v2 (
    id TEXT PRIMARY KEY,
    legacy_id TEXT, -- maps to old exercise IDs
    name TEXT NOT NULL,
    body_part TEXT,
    target_muscle TEXT,
    equipment TEXT,
    gif_url TEXT,
    gif_local_path TEXT,
    instructions TEXT,
    secondary_muscles TEXT, -- JSON array
    search_text TEXT, -- FTS optimization
    is_custom BOOLEAN DEFAULT 0
);

-- Download tracking
CREATE TABLE offline_downloads (
    exercise_id TEXT PRIMARY KEY,
    downloaded_at INTEGER,
    file_size INTEGER,
    last_accessed INTEGER
);
```

### Migration Table
```sql
-- Maps old exercise IDs to new ExerciseDB IDs
CREATE TABLE exercise_migration (
    old_id TEXT PRIMARY KEY,
    new_id TEXT NOT NULL,
    confidence_score REAL -- matching confidence
);
```

## 6. API Integration Architecture

### Sync Service Requirements
1. **Initial Sync**:
   - Download all exercises list
   - Download category lists (body parts, targets, equipment)
   - Store in Room database
   - Update UI to show new exercises

2. **Incremental Updates** (Future):
   - Check for new exercises monthly
   - Version tracking for API changes

3. **Error Handling**:
   - Retry logic for failed requests
   - Fallback to existing database if sync fails
   - User notification for sync status

### API Call Optimization
- Batch all initial requests in single session
- Cache responses for 30 days
- Use ETags for conditional requests (if API supports)

## 7. User Data Preservation

### Critical Requirements
1. **Existing Routines**: Must continue working without modification
2. **Workout History**: Maintain all historical data with exercise references
3. **Personal Records**: Link to new exercise IDs where matches exist
4. **Statistics**: Ensure continuity in progress tracking

### Migration Logic
1. Fuzzy match existing exercises to ExerciseDB entries
2. Create mapping table for confirmed matches
3. Keep custom exercises that don't match
4. Allow manual exercise linking by user

## 8. Performance Optimizations

### Search Performance
- Create FTS5 virtual table for full-text search
- Index on: name, body_part, target, equipment
- Pre-compute search tokens for faster queries

### Image Loading
- Lazy load GIFs only when visible
- Thumbnail generation for list views
- Progressive loading with placeholder
- Memory cache for recently viewed GIFs

### Database Optimizations
- Composite indexes for common queries
- Denormalized view for exercise selection
- Batch inserts during sync

## 9. Fallback & Recovery

### Scenarios to Handle
1. **No Internet on First Launch**: Use bundled basic dataset
2. **Partial Sync Failure**: Resume from last successful point
3. **Corrupted Cache**: Clear and re-sync option
4. **API Changes**: Version detection and compatibility mode

## 10. Testing Requirements

### Test Scenarios
1. Fresh install with sync
2. Update from existing app version
3. Offline mode functionality
4. Routine creation with new exercises
5. Workout completion with mixed exercises
6. Search performance with 1400+ exercises
7. Memory usage with GIF loading

### Data Validation
- Ensure all exercises have required fields
- Verify exercise ID uniqueness
- Validate routine integrity after migration
- Check workout history consistency

## 11. Implementation Priority

### Phase 1 (Core Migration) - Week 1
1. Add ExerciseDB API client
2. Implement sync service
3. Update database schema
4. Create migration logic

### Phase 2 (UI Updates) - Week 2
1. Update exercise selection screen
2. Add exercise detail view
3. Integrate GIF display
4. Update workout screen

### Phase 3 (Offline & Optimization) - Week 3
1. Implement offline download
2. Add search optimization
3. Performance tuning
4. User settings for sync

### Phase 4 (Polish & Testing) - Week 4
1. Error handling improvements
2. User feedback during sync
3. Comprehensive testing
4. Migration validation

## 12. Success Metrics

- All existing features remain functional
- Exercise database expanded from 800 to 1400+
- Search performance <100ms for any query
- Successful migration of 95%+ existing exercises
- GIF loading time <2 seconds on average connection
- App size increase <50MB (without offline GIFs)

## 13. Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| API service downtime | High | Local cache, bundled fallback data |
| Large app size with GIFs | Medium | Progressive download, compression |
| Breaking existing routines | High | Thorough testing, gradual rollout |
| Slow initial sync | Low | Background sync, progress indicator |
| Memory issues with GIFs | Medium | Proper image recycling, cache limits |