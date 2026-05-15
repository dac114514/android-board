package com.faster.aiboard.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.faster.aiboard.data.repository.FileRepository
import com.faster.aiboard.ui.screens.BoardListScreen
import com.faster.aiboard.ui.screens.BoardViewModel
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
            val context = LocalContext.current
            val repository = remember { FileRepository(context) }
            val boardViewModel = remember { BoardViewModel(repository) }
            BoardListScreen(
                viewModel = boardViewModel,
                onBoardClick = { boardId ->
                    navController.navigate(Screen.Canvas.createRoute(boardId))
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
