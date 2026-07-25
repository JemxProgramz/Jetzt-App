package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.Indigo500
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun AppNavHost(viewModel: AppViewModel, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = { BottomNav(navController = navController) },
        containerColor = Slate900,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = modifier.padding(innerPadding)
        ) {
            composable("home") { HomeScreen(viewModel) }
            composable("tasks") { TasksScreen(viewModel) }
            composable("focus") { FocusScreen(viewModel) }
            composable("notes") { NotesScreen(viewModel) }
            composable("schedule") { ScheduleScreen(viewModel) }
        }
    }
}

@Composable
fun BottomNav(navController: NavHostController) {
    val items = listOf(
        Triple("home", Icons.Filled.Home, Icons.Outlined.Home),
        Triple("tasks", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
        Triple("focus", Icons.Filled.Timer, Icons.Outlined.Timer),
        Triple("notes", Icons.Filled.EditNote, Icons.Outlined.EditNote),
        Triple("schedule", Icons.Filled.CalendarToday, Icons.Outlined.CalendarToday)
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    NavigationBar(
        containerColor = Slate800.copy(alpha = 0.95f),
        contentColor = Color.White,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        items.forEach { (route, selectedIcon, unselectedIcon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (currentRoute == route) selectedIcon else unselectedIcon,
                        contentDescription = route
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Indigo500,
                    unselectedIconColor = Slate400,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
