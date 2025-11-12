package com.athleticai.app.navigation

enum class AthleticAIDestination(val route: String, val title: String) {
    HOME("home", "Home"),
    WORKOUT("workout", "Workout"),
    PROGRESS("progress", "Progress"),
    AI_COACH("ai_coach", "AI Coach"),
    PROFILE("profile", "Profile"),
    SETTINGS("settings", "Settings"),
    ROUTINE_LIST("routine_list", "Routines"),
    CREATE_ROUTINE("create_routine", "Create Routine"),
    CUSTOM_WORKOUT_BUILDER("custom_workout_builder", "Workout Builder") // Keep for backward compatibility
}