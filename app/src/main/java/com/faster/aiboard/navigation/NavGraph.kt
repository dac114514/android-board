package com.faster.aiboard.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.faster.aiboard.ui.screens.BoardListScreen
import com.faster.aiboard.ui.screens.CanvasScreen
import com.faster.aiboard.ui.screens.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.BoardList.route,
        modifier = modifier
    ) {
        composable(Screen.BoardList.route) {
            BoardListScreen(
                onBoardClick = { boardId ->
                    navController.navigate(Screen.Canvas.createRoute(boardId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        composable(
            route = Screen.Canvas.route,
            arguments = listOf(navArgument("boardId") { type = NavType.StringType })
        ) { backStackEntry ->
            val boardId = backStackEntry.arguments?.getString("boardId") ?: return@composable
            CanvasScreen(
                boardId = boardId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
