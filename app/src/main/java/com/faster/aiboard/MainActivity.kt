package com.faster.aiboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.faster.aiboard.navigation.AppNavGraph
import com.faster.aiboard.navigation.Screen
import com.faster.aiboard.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                val bottomBarVisible = currentDestination?.route in Screen.bottomNavRoutes

                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = bottomBarVisible,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it }
                        ) {
                            NavigationBar {
                                data class NavItem(
                                    val route: String,
                                    val label: String
                                )
                                val navItems = listOf(
                                    NavItem(Screen.BoardList.route, "白板"),
                                    NavItem(Screen.Settings.route, "设置")
                                )
                                navItems.forEach { item ->
                                    val selected = currentDestination?.hierarchy?.any {
                                        it.route == item.route
                                    } == true
                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            when (item.route) {
                                                Screen.BoardList.route -> {
                                                    if (selected) Icon(Icons.Filled.Dashboard, contentDescription = item.label)
                                                    else Icon(Icons.Outlined.Dashboard, contentDescription = item.label)
                                                }
                                                Screen.Settings.route -> {
                                                    if (selected) Icon(Icons.Filled.Settings, contentDescription = item.label)
                                                    else Icon(Icons.Outlined.Settings, contentDescription = item.label)
                                                }
                                            }
                                        },
                                        label = { Text(item.label) }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavGraph(
                        navController = navController,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    )
                }
            }
        }
    }
}
