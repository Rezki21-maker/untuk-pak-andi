package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.AssignmentListScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.ClassManagementScreen
import com.example.ui.screens.GradeEntryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RecapExportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TeacherViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TeacherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val fontScale by viewModel.fontScale.collectAsState()
            val userMessage by viewModel.userMessage.collectAsState()

            val effectiveDarkTheme = isDarkTheme ?: isSystemInDarkTheme()

            MyApplicationTheme(darkTheme = effectiveDarkTheme) {
                // Apply custom senior teacher font scale
                val currentDensity = LocalDensity.current
                val customDensity = remember(currentDensity, fontScale) {
                    Density(
                        density = currentDensity.density,
                        fontScale = currentDensity.fontScale * fontScale
                    )
                }

                CompositionLocalProvider(LocalDensity provides customDensity) {
                    GuruKitaApp(
                        viewModel = viewModel,
                        userMessage = userMessage,
                        onClearMessage = { viewModel.clearMessage() }
                    )
                }
            }
        }
    }
}

@Composable
fun GuruKitaApp(
    viewModel: TeacherViewModel,
    userMessage: String?,
    onClearMessage: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearMessage()
        }
    }

    // Tampilkan BottomBar hanya pada top-level destinations
    val showBottomBar = currentRoute in listOf("home", "attendance", "assignments", "recap")

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val items = listOf(
                        Triple("home", "Beranda", Pair(Icons.Default.Home, Icons.Outlined.Home)),
                        Triple("attendance", "Presensi", Pair(Icons.Default.HowToReg, Icons.Outlined.HowToReg)),
                        Triple("assignments", "Tugas & Nilai", Pair(Icons.Default.Assignment, Icons.Outlined.Assignment)),
                        Triple("recap", "Rekap", Pair(Icons.Default.Assessment, Icons.Outlined.Assessment))
                    )

                    items.forEach { (route, label, icons) ->
                        val isSelected = currentRoute == route
                        val icon = if (isSelected) icons.first else icons.second

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != route) {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(imageVector = icon, contentDescription = label) },
                            label = { Text(label) },
                            modifier = Modifier.testTag("nav_item_$route")
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToAttendance = { navController.navigate("attendance") },
                    onNavigateToAssignments = { navController.navigate("assignments") },
                    onNavigateToRecap = { navController.navigate("recap") },
                    onNavigateToClasses = { navController.navigate("classes") },
                    onNavigateToSettings = { navController.navigate("settings") }
                )
            }

            composable("attendance") {
                AttendanceScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("assignments") {
                AssignmentListScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToGradeEntry = { assignmentId ->
                        navController.navigate("grade_entry/$assignmentId")
                    }
                )
            }

            composable(
                route = "grade_entry/{assignmentId}",
                arguments = listOf(navArgument("assignmentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val assignmentId = backStackEntry.arguments?.getLong("assignmentId") ?: 0L
                GradeEntryScreen(
                    assignmentId = assignmentId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("recap") {
                RecapExportScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("classes") {
                ClassManagementScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("settings") {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
