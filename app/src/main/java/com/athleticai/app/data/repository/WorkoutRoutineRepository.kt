package com.athleticai.app.data.repository

import com.athleticai.app.data.database.dao.FolderDao
import com.athleticai.app.data.database.dao.WorkoutRoutineDao
import com.athleticai.app.data.database.dao.RoutineExerciseDao
import com.athleticai.app.data.database.entities.Folder
import com.athleticai.app.data.database.entities.WorkoutRoutine
import com.athleticai.app.data.database.entities.RoutineExercise
import com.athleticai.app.data.database.entities.WorkoutRoutineWithExercises
import com.athleticai.app.data.database.entities.ExerciseSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class WorkoutRoutineRepository(
    private val folderDao: FolderDao,
    private val routineDao: WorkoutRoutineDao,
    private val routineExerciseDao: RoutineExerciseDao
) {
    
    // Folder operations
    fun getAllFolders(): Flow<List<Folder>> = folderDao.getAllFolders()
    
    suspend fun createFolder(name: String): String {
        val folderId = UUID.randomUUID().toString()
        val folder = Folder(
            id = folderId,
            name = name,
            createdDate = System.currentTimeMillis()
        )
        folderDao.insertFolder(folder)
        return folderId
    }
    
    suspend fun deleteFolder(folderId: String) {
        val folder = folderDao.getFolderById(folderId)
        folder?.let { folderDao.deleteFolder(it) }
    }
    
    suspend fun renameFolder(folderId: String, newName: String) {
        val folder = folderDao.getFolderById(folderId)
        folder?.let {
            folderDao.updateFolder(it.copy(name = newName))
        }
    }
    
    // Routine operations
    fun getAllRoutines(): Flow<List<WorkoutRoutine>> = routineDao.getAllRoutines()
    
    fun getRoutinesByFolder(folderId: String): Flow<List<WorkoutRoutine>> = 
        routineDao.getRoutinesByFolder(folderId)
    
    fun getUncategorizedRoutines(): Flow<List<WorkoutRoutine>> = 
        routineDao.getUncategorizedRoutines()
    
    suspend fun getRoutineWithExercises(routineId: String): WorkoutRoutineWithExercises? =
        routineDao.getRoutineWithExercises(routineId)
    
    suspend fun createRoutine(
        name: String,
        folderId: String? = null,
        exercises: List<Pair<String, List<ExerciseSet>>> = emptyList(),
        notes: String = ""
    ): String {
        val routineId = UUID.randomUUID().toString()
        val routine = WorkoutRoutine(
            id = routineId,
            name = name,
            folderId = folderId,
            notes = notes,
            createdDate = System.currentTimeMillis()
        )
        
        routineDao.insertRoutine(routine)
        
        // Add exercises to the routine
        exercises.forEachIndexed { index, (exerciseId, sets) ->
            val routineExerciseId = UUID.randomUUID().toString()
            val routineExercise = RoutineExercise(
                id = routineExerciseId,
                routineId = routineId,
                exerciseId = exerciseId,
                orderIndex = index,
                defaultSets = sets,
                restTimerEnabled = true,
                restSeconds = 90 // default rest time
            )
            routineExerciseDao.insertRoutineExercise(routineExercise)
        }
        
        return routineId
    }
    
    suspend fun updateRoutine(routine: WorkoutRoutine) {
        routineDao.updateRoutine(routine)
    }
    
    suspend fun deleteRoutine(routineId: String) {
        val routine = routineDao.getRoutineById(routineId)
        routine?.let { routineDao.deleteRoutine(it) }
    }
    
    suspend fun duplicateRoutine(routineId: String, newName: String): String {
        val originalRoutine = routineDao.getRoutineWithExercises(routineId) ?: return ""
        
        val newRoutineId = UUID.randomUUID().toString()
        val newRoutine = originalRoutine.routine.copy(
            id = newRoutineId,
            name = newName,
            createdDate = System.currentTimeMillis(),
            lastPerformed = null
        )
        
        routineDao.insertRoutine(newRoutine)
        
        // Duplicate all exercises
        originalRoutine.exercises.forEach { exercise ->
            val newExerciseId = UUID.randomUUID().toString()
            val newExercise = exercise.copy(
                id = newExerciseId,
                routineId = newRoutineId
            )
            routineExerciseDao.insertRoutineExercise(newExercise)
        }
        
        return newRoutineId
    }
    
    suspend fun updateLastPerformed(routineId: String) {
        routineDao.updateLastPerformed(routineId, System.currentTimeMillis())
    }
    
    // Routine exercise operations
    suspend fun addExerciseToRoutine(
        routineId: String,
        exerciseId: String,
        sets: List<ExerciseSet> = emptyList(),
        restSeconds: Int = 90
    ) {
        val existingExercises = routineExerciseDao.getExercisesByRoutine(routineId)
        val nextOrderIndex = existingExercises.size
        
        val routineExerciseId = UUID.randomUUID().toString()
        val routineExercise = RoutineExercise(
            id = routineExerciseId,
            routineId = routineId,
            exerciseId = exerciseId,
            orderIndex = nextOrderIndex,
            defaultSets = sets,
            restTimerEnabled = true,
            restSeconds = restSeconds
        )
        
        routineExerciseDao.insertRoutineExercise(routineExercise)
    }
    
    suspend fun removeExerciseFromRoutine(routineExerciseId: String) {
        val routineExercise = routineExerciseDao.getRoutineExerciseById(routineExerciseId)
        routineExercise?.let { 
            routineExerciseDao.deleteRoutineExercise(it)
        }
    }
    
    suspend fun updateExerciseOrder(routineId: String, exerciseIds: List<String>) {
        exerciseIds.forEachIndexed { index, exerciseId ->
            val routineExercise = routineExerciseDao.getRoutineExerciseById(exerciseId)
            routineExercise?.let {
                routineExerciseDao.updateRoutineExercise(it.copy(orderIndex = index))
            }
        }
    }
    
    suspend fun updateExerciseSets(routineExerciseId: String, sets: List<ExerciseSet>) {
        val routineExercise = routineExerciseDao.getRoutineExerciseById(routineExerciseId)
        routineExercise?.let {
            routineExerciseDao.updateRoutineExercise(it.copy(defaultSets = sets))
        }
    }
    
    suspend fun updateExerciseRestTime(routineExerciseId: String, restSeconds: Int) {
        val routineExercise = routineExerciseDao.getRoutineExerciseById(routineExerciseId)
        routineExercise?.let {
            routineExerciseDao.updateRoutineExercise(it.copy(restSeconds = restSeconds))
        }
    }
    
    // Statistics and utility functions
    suspend fun getRoutineCount(): Int = routineDao.getRoutineCount()
    
    fun getRecentRoutines(limit: Int = 5): Flow<List<WorkoutRoutine>> = 
        routineDao.getAllRoutines().map { routines ->
            routines.filter { it.lastPerformed != null }
                  .sortedByDescending { it.lastPerformed }
                  .take(limit)
        }
    
    fun getFavoriteRoutines(): Flow<List<WorkoutRoutine>> = 
        routineDao.getAllRoutines().map { routines ->
            routines.sortedWith(compareByDescending<WorkoutRoutine> { it.lastPerformed != null }
                  .thenByDescending { it.lastPerformed })
        }
}