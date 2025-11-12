package com.athleticai.app.data

import android.content.Context
import android.util.Log
import com.athleticai.app.data.models.MotivationalQuote
import com.athleticai.app.data.models.QuotesResponse
import com.google.gson.Gson
import java.time.LocalTime
import java.time.LocalDate
import java.time.DayOfWeek
import kotlin.random.Random

class QuoteLoader(private val context: Context) {
    
    private val TAG = "QuoteLoader"
    private var cachedQuotes: List<MotivationalQuote>? = null
    
    /**
     * Load all quotes from the JSON file
     */
    suspend fun loadQuotes(): List<MotivationalQuote> {
        if (cachedQuotes != null) {
            return cachedQuotes!!
        }
        
        return try {
            Log.d(TAG, "Loading quotes from assets...")
            context.assets.open("motivational-quotes.json").use { inputStream ->
                val json = inputStream.bufferedReader().use { it.readText() }
                val quotesResponse = Gson().fromJson(json, QuotesResponse::class.java)
                cachedQuotes = quotesResponse.quotes
                Log.d(TAG, "Loaded ${quotesResponse.quotes.size} quotes")
                quotesResponse.quotes
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading quotes", e)
            // Return default quotes if loading fails
            listOf(
                MotivationalQuote(
                    id = 1,
                    text = "The iron never lies. You are what you lift.",
                    context = "general",
                    timeOfDay = "any"
                ),
                MotivationalQuote(
                    id = 2,
                    text = "Your workout is the highlight of your day. Make it count.",
                    context = "general",
                    timeOfDay = "any"
                )
            )
        }
    }
    
    /**
     * Get a random quote, optionally filtered by context and time of day
     */
    suspend fun getRandomQuote(
        context: String? = null,
        timeOfDay: String? = null
    ): MotivationalQuote {
        val quotes = loadQuotes()
        
        val filteredQuotes = quotes.filter { quote ->
            val contextMatches = context == null || quote.context == context || quote.context == "any"
            val timeMatches = timeOfDay == null || quote.timeOfDay == timeOfDay || quote.timeOfDay == "any"
            contextMatches && timeMatches
        }
        
        return if (filteredQuotes.isNotEmpty()) {
            filteredQuotes[Random.nextInt(filteredQuotes.size)]
        } else {
            // Fallback to any quote if no matches
            quotes[Random.nextInt(quotes.size)]
        }
    }
    
    /**
     * Get a contextually appropriate quote based on current time, day, and user data
     */
    suspend fun getQuoteForCurrentTime(): MotivationalQuote {
        val currentHour = LocalTime.now().hour
        val currentDay = LocalDate.now().dayOfWeek
        
        val timeOfDay = when (currentHour) {
            in 5..7 -> "early_morning"
            in 8..11 -> "morning"
            in 12..17 -> "afternoon"
            in 18..21 -> "evening"
            else -> "night"
        }
        
        // Determine day-specific context
        val dayContext = when (currentDay) {
            DayOfWeek.MONDAY -> "monday"
            DayOfWeek.FRIDAY -> "friday"
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY -> "weekend"
            else -> null
        }
        
        // Check for Sunday-specific context
        val specificDayContext = if (currentDay == DayOfWeek.SUNDAY) "sunday" else dayContext
        
        Log.d(TAG, "Getting quote for time: $timeOfDay, day: $currentDay, context: $specificDayContext")
        
        // Try to get a day-specific quote first, then fall back to time-based
        return if (specificDayContext != null) {
            val dayQuote = tryGetSpecificQuote(specificDayContext)
            if (dayQuote != null) {
                Log.d(TAG, "Found day-specific quote: ${dayQuote.text}")
                dayQuote
            } else {
                getRandomQuote(timeOfDay = timeOfDay)
            }
        } else {
            getRandomQuote(timeOfDay = timeOfDay)
        }
    }
    
    /**
     * Get a pre-workout motivational quote
     */
    suspend fun getPreWorkoutQuote(): MotivationalQuote {
        return getRandomQuote(context = "pre_workout")
    }
    
    /**
     * Get a post-workout motivational quote
     */
    suspend fun getPostWorkoutQuote(): MotivationalQuote {
        return getRandomQuote(context = "post_workout")
    }
    
    /**
     * Try to get a quote for a specific context, return null if none found
     */
    private suspend fun tryGetSpecificQuote(context: String): MotivationalQuote? {
        val quotes = loadQuotes()
        val specificQuotes = quotes.filter { it.context == context }
        
        return if (specificQuotes.isNotEmpty()) {
            specificQuotes[Random.nextInt(specificQuotes.size)]
        } else {
            null
        }
    }
    
    /**
     * Get a context-aware quote based on user's current workout state
     */
    suspend fun getContextAwareQuote(
        workoutStreak: Int = 0,
        lastWorkoutDays: Int = 0,
        hasRecentPR: Boolean = false,
        totalWorkouts: Int = 0
    ): MotivationalQuote {
        val currentHour = LocalTime.now().hour
        val currentDay = LocalDate.now().dayOfWeek
        
        // Determine contextual priority
        val context = when {
            workoutStreak >= 20 -> "very_long_streak"
            workoutStreak >= 10 -> "long_streak"
            lastWorkoutDays > 3 -> "after_skip"
            lastWorkoutDays == 1 -> "consecutive_days"
            hasRecentPR -> "after_pr"
            currentDay == DayOfWeek.SUNDAY -> "sunday"
            currentDay == DayOfWeek.MONDAY -> "monday"
            currentDay == DayOfWeek.FRIDAY -> "friday"
            currentDay in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> "weekend"
            currentHour in 5..7 -> "early_morning"
            totalWorkouts == 0 -> "general"
            else -> null
        }
        
        Log.d(TAG, "Getting context-aware quote. Context: $context, Streak: $workoutStreak, Last workout: $lastWorkoutDays days ago")
        
        return if (context != null) {
            val contextQuote = tryGetSpecificQuote(context)
            contextQuote ?: getQuoteForCurrentTime()
        } else {
            getQuoteForCurrentTime()
        }
    }
    
    /**
     * Clear cached quotes to force reload
     */
    fun clearCache() {
        Log.d(TAG, "Clearing quote cache")
        cachedQuotes = null
    }
}