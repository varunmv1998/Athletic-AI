package com.athleticai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.athleticai.app.ui.components.MaterialSymbol
import com.athleticai.app.ui.components.MaterialSymbols
import com.athleticai.app.ui.components.IconSizes
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.athleticai.app.navigation.AthleticAIDestination
import com.athleticai.app.ui.screens.AICoachScreen
import com.athleticai.app.ui.screens.HomeScreen
import com.athleticai.app.ui.screens.ProgressScreen
import com.athleticai.app.ui.screens.ProfileScreen
import com.athleticai.app.ui.screens.SettingsScreen
import com.athleticai.app.ui.screens.AchievementsScreen
import com.athleticai.app.ui.screens.AchievementGuideScreen
import com.athleticai.app.ui.screens.WorkoutScreen
import com.athleticai.app.ui.screens.WorkoutDashboardScreen
import com.athleticai.app.ui.screens.WorkoutBuilderFlow
import com.athleticai.app.ui.screens.RoutineListScreen
import com.athleticai.app.ui.screens.CreateRoutineScreen
import com.athleticai.app.ui.screens.EditRoutineScreen
import com.athleticai.app.ui.screens.WorkoutDetailsScreen
import com.athleticai.app.ui.screens.ExerciseDictionaryScreen
import com.athleticai.app.ui.screens.ProgramDetailScreen
import com.athleticai.app.ui.theme.AthleticAITheme
import com.athleticai.app.ui.theme.ThemePreset
import com.athleticai.app.ui.theme.CustomThemeColors
import com.athleticai.app.ui.components.parseCustomThemeColors
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val application = application as AthleticAIApplication
        val appContainer = AppContainer(application)
        
        setContent {
            val settingsVm = appContainer.settingsViewModel
            val settingsState by settingsVm.state.collectAsState()
            
            // Determine dark theme
            val dark = when (settingsState.theme) {
                "dark" -> true
                "light" -> false
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }
            
            // Parse theme preset
            val themePreset = try {
                ThemePreset.valueOf(settingsState.themePreset)
            } catch (e: Exception) {
                ThemePreset.DYNAMIC
            }
            
            // Parse custom colors if available
            val customColors = settingsState.customThemeColors?.let { json ->
                parseCustomThemeColors(json)
            }
            
            AthleticAITheme(
                darkTheme = dark,
                themePreset = themePreset,
                customColors = customColors
            ) {
                AthleticAIApp(appContainer = appContainer)
            }
        }
    }
}

@Composable
fun AthleticAIApp(appContainer: AppContainer) {
    val navController = rememberNavController()
    var selectedDestination by remember { mutableStateOf(AthleticAIDestination.HOME) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AthleticAIBottomNavigation(
                navController = navController,
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = AthleticAIDestination.HOME.route
            ) {
                composable(AthleticAIDestination.HOME.route) {
                    HomeScreen(
                        homeViewModel = appContainer.homeViewModel,
                        progressViewModel = appContainer.progressViewModel,
                        routineViewModel = appContainer.routineViewModel,
                        onNavigateToWorkout = {
                            navController.navigate(AthleticAIDestination.WORKOUT.route)
                        }
                    )
                }
                composable(AthleticAIDestination.WORKOUT.route) {
                    WorkoutDashboardScreen(
                        routineViewModel = appContainer.routineViewModel,
                        workoutViewModel = appContainer.workoutViewModel,
                        programViewModel = appContainer.programManagementViewModel,
                        onStartRoutine = { routineId ->
                            navController.navigate("workout/session/$routineId")
                        },
                        onViewAllRoutines = {
                            navController.navigate(AthleticAIDestination.ROUTINE_LIST.route)
                        },
                        onCreateRoutine = {
                            navController.navigate(AthleticAIDestination.CREATE_ROUTINE.route)
                        },
                        onEditRoutine = { routineId ->
                            navController.navigate("routine/edit/$routineId")
                        },
                        onEnrollInProgram = { programId ->
                            // Enrollment is handled in the ViewModel
                        },
                        onViewProgramDetails = { programId ->
                            navController.navigate("program/$programId")
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable("workout/session/{routineId}") { backStackEntry ->
                    val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
                    val units = appContainer.settingsViewModel.getUnits()
                    
                    // Start the routine workout when this screen loads
                    LaunchedEffect(routineId) {
                        if (routineId.isNotEmpty()) {
                            appContainer.workoutViewModel.startWorkoutSessionFromRoutine(routineId)
                        }
                    }
                    
                    WorkoutScreen(
                        workoutViewModel = appContainer.workoutViewModel,
                        units = units,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(AthleticAIDestination.ROUTINE_LIST.route) {
                    RoutineListScreen(
                        routineViewModel = appContainer.routineViewModel,
                        onStartRoutine = { routineId ->
                            navController.navigate("workout/session/$routineId")
                        },
                        onEditRoutine = { routineId ->
                            navController.navigate("routine/edit/$routineId")
                        },
                        onCreateRoutine = {
                            navController.navigate(AthleticAIDestination.CREATE_ROUTINE.route)
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable(AthleticAIDestination.CREATE_ROUTINE.route) {
                    CreateRoutineScreen(
                        routineViewModel = appContainer.routineViewModel,
                        exerciseSelectionViewModel = appContainer.exerciseSelectionViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onRoutineCreated = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable("routine/edit/{routineId}") { backStackEntry ->
                    val routineId = backStackEntry.arguments?.getString("routineId") ?: ""
                    EditRoutineScreen(
                        routineId = routineId,
                        routineViewModel = appContainer.routineViewModel,
                        exerciseSelectionViewModel = appContainer.exerciseSelectionViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onRoutineSaved = {
                            navController.popBackStack()
                        }
                    )
                }
                
                composable("program/{programId}") { backStackEntry ->
                    val programId = backStackEntry.arguments?.getString("programId") ?: ""
                    ProgramDetailScreen(
                        programId = programId,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onStartWorkout = { programDayId ->
                            navController.navigate("program/workout/$programDayId")
                        },
                        viewModel = appContainer.programDetailViewModel
                    )
                }
                
                // Keep for backward compatibility during transition
                composable(AthleticAIDestination.CUSTOM_WORKOUT_BUILDER.route) {
                    WorkoutBuilderFlow(
                        customWorkoutViewModel = appContainer.customWorkoutViewModel,
                        exerciseSelectionViewModel = appContainer.exerciseSelectionViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onWorkoutCreated = {
                            navController.popBackStack()
                        }
                    )
                }
                composable(AthleticAIDestination.PROGRESS.route) {
                    ProgressScreen(
                        viewModel = appContainer.progressViewModel,
                        onOpenSession = { id ->
                            navController.navigate("progress/session/" + id)
                        }
                    )
                }
                composable("progress/session/{sessionId}") { backStackEntry ->
                    val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                    val progressViewModel = appContainer.progressViewModel
                    
                    var workoutDetails by remember { mutableStateOf<Triple<com.athleticai.app.data.database.entities.WorkoutSession?, List<com.athleticai.app.data.database.entities.WorkoutSet>, Map<String, com.athleticai.app.data.database.entities.Exercise>>?>(null) }
                    
                    // Load workout details when sessionId changes
                    LaunchedEffect(sessionId) {
                        if (sessionId.isNotEmpty()) {
                            workoutDetails = progressViewModel.getWorkoutDetails(sessionId)
                        }
                    }
                    
                    workoutDetails?.let { (session, sets, exercises) ->
                        if (session != null) {
                            WorkoutDetailsScreen(
                                session = session,
                                workoutSets = sets,
                                exerciseDetails = exercises,
                                onNavigateBack = {
                                    navController.popBackStack()
                                }
                            )
                        } else {
                            // Show error state
                            Text(
                                text = "Workout session not found",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }
                    } ?: run {
                        // Loading state
                        Text(
                            text = "Loading...",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }
                composable(AthleticAIDestination.AI_COACH.route) {
                    AICoachScreen(
                        viewModel = appContainer.aiCoachViewModel
                    )
                }
                composable(AthleticAIDestination.PROFILE.route) {
                    ProfileScreen(
                        progressViewModel = appContainer.progressViewModel,
                        onNavigateToSettings = {
                            navController.navigate(AthleticAIDestination.SETTINGS.route)
                        }
                    )
                }
                composable(AthleticAIDestination.SETTINGS.route) {
                    SettingsScreen(
                        viewModel = appContainer.settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAchievements = {
                            navController.navigate("achievements")
                        },
                        onNavigateToAchievementGuide = {
                            navController.navigate("achievement-guide")
                        },
                        onNavigateToExerciseDictionary = {
                            navController.navigate("exercise-dictionary")
                        }
                    )
                }
                
                composable("exercise-dictionary") {
                    ExerciseDictionaryScreen(
                        viewModel = appContainer.exerciseDictionaryViewModel,
                        routineViewModel = appContainer.routineViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCreateRoutine = {
                            navController.navigate(AthleticAIDestination.CREATE_ROUTINE.route)
                        }
                    )
                }
                
                composable("achievements") {
                    AchievementsScreen(
                        viewModel = appContainer.achievementViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                
                composable("achievement-guide") {
                    AchievementGuideScreen(
                        viewModel = appContainer.achievementViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun AthleticAIBottomNavigation(
    navController: NavController,
    selectedDestination: AthleticAIDestination,
    onDestinationSelected: (AthleticAIDestination) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val bottomNavDestinations = listOf(
        AthleticAIDestination.HOME to MaterialSymbols.HOME,
        AthleticAIDestination.WORKOUT to MaterialSymbols.FITNESS_CENTER,
        AthleticAIDestination.PROGRESS to MaterialSymbols.ANALYTICS,
        AthleticAIDestination.AI_COACH to MaterialSymbols.PSYCHOLOGY,
        AthleticAIDestination.PROFILE to MaterialSymbols.PERSON
    )

    NavigationBar(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        bottomNavDestinations.forEach { (destination, icon) ->
            NavigationBarItem(
                icon = {
                    MaterialSymbol(
                        symbol = icon,
                        size = IconSizes.STANDARD, // Consistent 24dp sizing
                        contentDescription = destination.title
                    )
                },
                label = { Text(destination.title) },
                selected = currentRoute == destination.route,
                onClick = {
                    onDestinationSelected(destination)
                    if (currentRoute != destination.route) {
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
