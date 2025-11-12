package com.athleticai.app.data.repository

import com.athleticai.app.data.database.dao.WorkoutSessionDao
import com.athleticai.app.data.database.dao.WorkoutSetDao
import com.athleticai.app.data.database.entities.WorkoutSession
import com.athleticai.app.data.database.entities.WorkoutSet
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val sessionDao: WorkoutSessionDao,
    private val setDao: WorkoutSetDao
) {
    
    // Session operations
    fun getAllSessions(): Flow<List<WorkoutSession>> = sessionDao.getAllSessions()
    
    suspend fun getSessionById(sessionId: String): WorkoutSession? = 
        sessionDao.getSessionById(sessionId)
    
    suspend fun getActiveSession(): WorkoutSession? = sessionDao.getActiveSession()
    
    fun getCompletedSessions(limit: Int): Flow<List<WorkoutSession>> = 
        sessionDao.getCompletedSessions(limit)
    
    suspend fun getCompletedSessionCount(): Int = sessionDao.getCompletedSessionCount()
    
    suspend fun insertSession(session: WorkoutSession) = sessionDao.insertSession(session)
    
    suspend fun updateSession(session: WorkoutSession) = sessionDao.updateSession(session)
    
    suspend fun deleteSession(sessionId: String) = sessionDao.deleteSession(sessionId)
    
    // Set operations
    fun getSetsForSession(sessionId: String): Flow<List<WorkoutSet>> = 
        setDao.getSetsForSession(sessionId)
    
    fun getSetsForExerciseInSession(sessionId: String, exerciseId: String): Flow<List<WorkoutSet>> = 
        setDao.getSetsForExerciseInSession(sessionId, exerciseId)
    
    fun getRecentSetsForExercise(exerciseId: String, limit: Int): Flow<List<WorkoutSet>> = 
        setDao.getRecentSetsForExercise(exerciseId, limit)
    
    suspend fun getSetCountForSession(sessionId: String): Int = 
        setDao.getSetCountForSession(sessionId)
    
    suspend fun getPersonalRecord(exerciseId: String): Double? = 
        setDao.getPersonalRecord(exerciseId)
    
    suspend fun insertSet(workoutSet: WorkoutSet) = setDao.insertSet(workoutSet)
    
    suspend fun deleteSet(setId: String) = setDao.deleteSet(setId)
    
    suspend fun deleteSetsForSession(sessionId: String) = setDao.deleteSetsForSession(sessionId)

    // Sync helpers
    suspend fun getSetsForSessionSync(sessionId: String): List<WorkoutSet> =
        setDao.getSetsForSessionSync(sessionId)
    
    // Test data methods
    suspend fun addSession(session: WorkoutSession) = sessionDao.insertSession(session)
    
    suspend fun addSet(workoutSet: WorkoutSet) = setDao.insertSet(workoutSet)
    
    suspend fun clearAllSessions() {
        setDao.deleteAll() // Clear sets first due to foreign key constraint
        sessionDao.deleteAll()
    }
}
