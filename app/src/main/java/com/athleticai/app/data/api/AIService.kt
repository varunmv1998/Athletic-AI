package com.athleticai.app.data.api

import android.util.Log
import com.athleticai.app.data.database.entities.BodyMeasurement
import com.athleticai.app.data.database.entities.WorkoutSession
import com.athleticai.app.data.database.entities.WorkoutSet
import com.athleticai.app.data.database.entities.Goal
import com.athleticai.app.data.database.entities.PersonalRecord
import com.athleticai.app.data.repository.SettingsRepository
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import java.security.KeyStore

class AIService(
    private val settingsRepository: SettingsRepository
) {
    private val api: OpenAIApi by lazy {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // Only add logging in debug builds to prevent API key exposure
            .apply {
                if (android.util.Log.isLoggable("AIService", android.util.Log.DEBUG)) {
                    val logging = HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.HEADERS // Only log headers, not body
                    }
                    addInterceptor(logging)
                }
            }
            .build()

        Retrofit.Builder()
            .baseUrl("https://api.openai.com/")
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenAIApi::class.java)
    }

    // Event Queue for batching workout events
    private val eventQueue = mutableListOf<WorkoutEvent>()

    sealed class WorkoutEvent {
        data class SessionCompleted(
            val session: WorkoutSession,
            val sets: List<WorkoutSet>
        ) : WorkoutEvent()
        
        data class PersonalRecordAchieved(val pr: PersonalRecord) : WorkoutEvent()
        data class BodyMeasurementAdded(val measurement: BodyMeasurement) : WorkoutEvent()
        data class GoalUpdated(val goal: Goal) : WorkoutEvent()
    }

    // Add events to queue for batch processing
    fun queueEvent(event: WorkoutEvent) {
        eventQueue.add(event)
        Log.d("AIService", "Event queued: ${event::class.simpleName}")
    }

    // Process queued events and get AI analysis
    suspend fun processQueuedEvents(): Result<String> {
        if (eventQueue.isEmpty()) {
            return Result.failure(Exception("No events to process"))
        }

        return withContext(Dispatchers.IO) {
            try {
                val apiKey = settingsRepository.getApiKey()
                if (apiKey.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("No OpenAI API key configured"))
                }

                val prompt = generateEventAnalysisPrompt(eventQueue.toList())
                val response = makeApiCall(apiKey, prompt)

                if (response.isSuccessful) {
                    val aiResponse = response.body()?.choices?.firstOrNull()?.message?.content
                        ?: "No response from AI"
                    
                    // Clear processed events
                    eventQueue.clear()
                    
                    Result.success(aiResponse)
                } else {
                    val error = parseError(response)
                    Result.failure(Exception("API Error: $error"))
                }
            } catch (e: Exception) {
                Log.e("AIService", "Error processing events", e)
                Result.failure(e)
            }
        }
    }

    // Chat interface for direct Q&A
    suspend fun askQuestion(
        question: String,
        workoutHistory: List<WorkoutSession> = emptyList(),
        measurements: List<BodyMeasurement> = emptyList(),
        goals: List<Goal> = emptyList()
    ): Result<String> {
        Log.d("AIService", "=== ASK QUESTION START ===")
        Log.d("AIService", "Question: '$question'")
        Log.d("AIService", "Workout history size: ${workoutHistory.size}")
        Log.d("AIService", "Measurements size: ${measurements.size}")
        Log.d("AIService", "Goals size: ${goals.size}")
        
        return withContext(Dispatchers.IO) {
            try {
                Log.d("AIService", "Getting API key from settings...")
                val apiKey = settingsRepository.getApiKey()
                Log.d("AIService", "API key status: ${if (apiKey.isNullOrBlank()) "NULL/EMPTY" else "EXISTS (length: ${apiKey.length})"}")
                
                if (apiKey.isNullOrBlank()) {
                    Log.w("AIService", "No API key configured - returning failure")
                    return@withContext Result.failure(Exception("No OpenAI API key configured"))
                }

                Log.d("AIService", "Generating chat prompt...")
                val prompt = generateChatPrompt(question, workoutHistory, measurements, goals)
                Log.d("AIService", "Generated prompt length: ${prompt.length}")
                Log.v("AIService", "Full prompt: $prompt")
                
                Log.d("AIService", "Making API call...")
                val response = makeApiCall(apiKey, prompt)
                Log.d("AIService", "API call completed - Response code: ${response.code()}")
                Log.d("AIService", "Response successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    Log.d("AIService", "Response body: $responseBody")
                    
                    val aiResponse = responseBody?.choices?.firstOrNull()?.message?.content
                        ?: "I'm sorry, I couldn't generate a response. Please try again."
                    Log.d("AIService", "Extracted AI response: '$aiResponse'")
                    Result.success(aiResponse)
                } else {
                    Log.e("AIService", "API call failed - Response code: ${response.code()}")
                    Log.e("AIService", "Error body: ${response.errorBody()?.string()}")
                    val error = parseError(response)
                    Log.e("AIService", "Parsed error: $error")
                    Result.failure(Exception("API Error: $error"))
                }
            } catch (e: Exception) {
                Log.e("AIService", "Exception in askQuestion: ${e.message}")
                Log.e("AIService", "Exception type: ${e.javaClass.simpleName}")
                Log.e("AIService", "Stack trace:", e)
                Result.failure(e)
            }
        }
    }

    // Weekly review analysis
    suspend fun generateWeeklyReview(
        weekSessions: List<WorkoutSession>,
        weekSets: List<WorkoutSet>,
        measurements: List<BodyMeasurement>,
        goals: List<Goal>
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val apiKey = settingsRepository.getApiKey()
                if (apiKey.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("No OpenAI API key configured"))
                }

                val prompt = generateWeeklyReviewPrompt(weekSessions, weekSets, measurements, goals)
                val response = makeApiCall(apiKey, prompt)

                if (response.isSuccessful) {
                    val aiResponse = response.body()?.choices?.firstOrNull()?.message?.content
                        ?: "Unable to generate weekly review"
                    Result.success(aiResponse)
                } else {
                    val error = parseError(response)
                    Result.failure(Exception("API Error: $error"))
                }
            } catch (e: Exception) {
                Log.e("AIService", "Error generating weekly review", e)
                Result.failure(e)
            }
        }
    }

    private suspend fun makeApiCall(apiKey: String, userPrompt: String): Response<ChatCompletionResponse> {
        Log.d("AIService", "=== MAKE API CALL START ===")
        Log.d("AIService", "API key length: ${apiKey.length}")
        Log.d("AIService", "User prompt length: ${userPrompt.length}")
        
        val messages = listOf(
            ChatMessage(
                role = "system",
                content = """You are an expert AI fitness coach specializing in strength training and body composition. 
                |You help users analyze their workout data, track progress, and provide personalized recommendations.
                |
                |Key principles:
                |- Focus on progressive overload and consistent training
                |- Use RPE (Rate of Perceived Exertion) scale for intensity guidance
                |- Provide actionable, specific advice
                |- Consider both workout performance and body measurements
                |- Be encouraging but realistic about expectations
                |
                |Keep responses concise, practical, and motivational.""".trimMargin()
            ),
            ChatMessage(role = "user", content = userPrompt)
        )
        Log.d("AIService", "Messages created - count: ${messages.size}")

        val request = ChatCompletionRequest(
            model = "gpt-3.5-turbo",
            messages = messages,
            maxTokens = 1000,
            temperature = 0.7
        )
        Log.d("AIService", "Chat completion request created")
        
        Log.d("AIService", "Making network call to OpenAI API...")
        try {
            val response = api.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )
            Log.d("AIService", "Network call completed - Response code: ${response.code()}")
            Log.d("AIService", "Response message: ${response.message()}")
            Log.d("AIService", "Response successful: ${response.isSuccessful}")
            return response
        } catch (e: java.net.UnknownHostException) {
            Log.e("AIService", "DNS resolution failed for api.openai.com", e)
            throw e
        } catch (e: java.net.ConnectException) {
            Log.e("AIService", "Connection failed to OpenAI API", e)
            throw e
        } catch (e: java.net.SocketTimeoutException) {
            Log.e("AIService", "Socket timeout connecting to OpenAI API", e)
            throw e
        } catch (e: javax.net.ssl.SSLException) {
            Log.e("AIService", "SSL handshake failed with OpenAI API", e)
            throw e
        } catch (e: Exception) {
            Log.e("AIService", "Unexpected network error: ${e.javaClass.simpleName}: ${e.message}", e)
            throw e
        }
    }

    private fun generateEventAnalysisPrompt(events: List<WorkoutEvent>): String {
        val summary = StringBuilder()
        summary.append("Analyze these recent fitness events and provide insights:\n\n")

        events.forEach { event ->
            when (event) {
                is WorkoutEvent.SessionCompleted -> {
                    summary.append("WORKOUT COMPLETED:\n")
                    summary.append("- Session: ${event.session.sessionName}\n")
                    summary.append("- Date: ${event.session.endTime?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}\n")
                    summary.append("- Sets completed: ${event.sets.size}\n")
                    
                    val avgRPE = event.sets.map { it.rpe }.average()
                    summary.append("- Average RPE: ${"%.1f".format(avgRPE)}\n")
                    
                    val totalVolume = event.sets.sumOf { it.weight * it.reps }
                    summary.append("- Total Volume: ${"%.0f".format(totalVolume)} kg·reps\n\n")
                }
                is WorkoutEvent.PersonalRecordAchieved -> {
                    summary.append("PERSONAL RECORD:\n")
                    summary.append("- Exercise: ${event.pr.exerciseId}\n")
                    summary.append("- New ${event.pr.type}: ${event.pr.value}\n")
                    summary.append("- Date: ${event.pr.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}\n\n")
                }
                is WorkoutEvent.BodyMeasurementAdded -> {
                    summary.append("BODY MEASUREMENT UPDATE:\n")
                    event.measurement.weightKg?.let { summary.append("- Weight: ${it}kg\n") }
                    event.measurement.bodyFatPct?.let { summary.append("- Body Fat: ${it}%\n") }
                    event.measurement.waistCm?.let { summary.append("- Waist: ${it}cm\n") }
                    summary.append("- Date: ${event.measurement.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}\n\n")
                }
                is WorkoutEvent.GoalUpdated -> {
                    summary.append("GOAL UPDATE:\n")
                    summary.append("- Metric: ${event.goal.metricType}\n")
                    summary.append("- Target: ${event.goal.targetValue}\n")
                    summary.append("- Deadline: ${event.goal.targetDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}\n\n")
                }
            }
        }

        summary.append("Please provide:\n")
        summary.append("1. Performance analysis of recent workouts\n")
        summary.append("2. Progress assessment and trends\n")
        summary.append("3. Specific recommendations for next workouts\n")
        summary.append("4. Any concerns or adjustments needed\n")

        return summary.toString()
    }

    private fun generateChatPrompt(
        question: String,
        workoutHistory: List<WorkoutSession>,
        measurements: List<BodyMeasurement>,
        goals: List<Goal>
    ): String {
        val context = StringBuilder()
        context.append("USER QUESTION: $question\n\n")
        
        // Add recent context if available
        if (workoutHistory.isNotEmpty()) {
            context.append("RECENT WORKOUTS:\n")
            workoutHistory.take(5).forEach { session ->
                context.append("- ${session.sessionName} (${session.endTime?.format(DateTimeFormatter.ofPattern("MMM dd")) ?: "In Progress"})\n")
            }
            context.append("\n")
        }
        
        if (measurements.isNotEmpty()) {
            val latest = measurements.maxByOrNull { it.date }
            context.append("LATEST MEASUREMENTS:\n")
            latest?.weightKg?.let { context.append("- Weight: ${it}kg\n") }
            latest?.bodyFatPct?.let { context.append("- Body Fat: ${it}%\n") }
            context.append("\n")
        }
        
        if (goals.isNotEmpty()) {
            context.append("ACTIVE GOALS:\n")
            goals.take(3).forEach { goal ->
                context.append("- ${goal.metricType}: ${goal.targetValue} by ${goal.targetDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}\n")
            }
            context.append("\n")
        }
        
        context.append("Please provide a helpful, personalized response based on this context.")
        
        return context.toString()
    }

    private fun generateWeeklyReviewPrompt(
        sessions: List<WorkoutSession>,
        sets: List<WorkoutSet>,
        measurements: List<BodyMeasurement>,
        goals: List<Goal>
    ): String {
        val summary = StringBuilder()
        summary.append("WEEKLY TRAINING REVIEW:\n\n")
        
        summary.append("WORKOUTS THIS WEEK:\n")
        summary.append("- Sessions completed: ${sessions.size}\n")
        summary.append("- Total sets: ${sets.size}\n")
        
        if (sets.isNotEmpty()) {
            val avgRPE = sets.map { it.rpe }.average()
            val totalVolume = sets.sumOf { it.weight * it.reps }
            summary.append("- Average RPE: ${"%.1f".format(avgRPE)}\n")
            summary.append("- Total Volume: ${"%.0f".format(totalVolume)} kg·reps\n")
        }
        
        summary.append("\nWORKOUT DETAILS:\n")
        sessions.forEach { session ->
            summary.append("- ${session.sessionName}: ${session.endTime?.format(DateTimeFormatter.ofPattern("EEE, MMM dd"))}\n")
        }
        
        if (measurements.isNotEmpty()) {
            summary.append("\nBODY MEASUREMENTS:\n")
            measurements.forEach { m ->
                m.weightKg?.let { summary.append("- Weight: ${it}kg (${m.date.format(DateTimeFormatter.ofPattern("MMM dd"))})\n") }
            }
        }
        
        if (goals.isNotEmpty()) {
            summary.append("\nACTIVE GOALS:\n")
            goals.forEach { goal ->
                summary.append("- ${goal.metricType}: ${goal.targetValue}\n")
            }
        }
        
        summary.append("\nPlease provide:\n")
        summary.append("1. Week performance summary\n")
        summary.append("2. Progress toward goals\n")
        summary.append("3. Areas for improvement\n")
        summary.append("4. Recommendations for next week\n")
        
        return summary.toString()
    }

    private fun parseError(response: Response<ChatCompletionResponse>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                val error = Gson().fromJson(errorBody, OpenAIError::class.java)
                error.error.message
            } else {
                "HTTP ${response.code()}: ${response.message()}"
            }
        } catch (e: Exception) {
            "HTTP ${response.code()}: ${response.message()}"
        }
    }

    // Fallback responses for when API is unavailable
    fun getFallbackResponse(question: String): String {
        Log.d("AIService", "=== GET FALLBACK RESPONSE ===")
        Log.d("AIService", "Question for fallback: '$question'")
        
        val response = when {
            question.contains("rest", ignoreCase = true) -> {
                Log.d("AIService", "Matched 'rest' keyword")
                "For strength training, rest 2-3 minutes between sets for compound movements and 1-2 minutes for isolation exercises."
            }
            question.contains("rpe", ignoreCase = true) -> {
                Log.d("AIService", "Matched 'rpe' keyword")
                "RPE (Rate of Perceived Exertion): 6-7 = Easy, 8 = Hard, 9 = Very Hard, 10 = Maximum effort. Aim for RPE 7-8 for most training sets."
            }
            question.contains("progression", ignoreCase = true) -> {
                Log.d("AIService", "Matched 'progression' keyword")
                "Progressive overload is key. Increase weight by 2.5-5kg when you can complete all sets at RPE 7 or below."
            }
            question.contains("program", ignoreCase = true) -> {
                Log.d("AIService", "Matched 'program' keyword")
                "The Push/Pull/Legs split is excellent for intermediate trainees. Focus on compound movements and consistent progression."
            }
            else -> {
                Log.d("AIService", "No keywords matched - using default response")
                "I'm currently offline. For general training advice, focus on consistency, progressive overload, and adequate recovery between sessions."
            }
        }
        
        Log.d("AIService", "Selected fallback response: '$response'")
        return response
    }
}