package com.faster.aiboard.navigation

sealed class Screen(val route: String) {
    data object BoardList : Screen("board_list")
    data object Canvas : Screen("canvas/{boardId}") {
        fun createRoute(boardId: String) = "canvas/$boardId"
    }
    data object Settings : Screen("settings")

    companion object {
        val bottomNavRoutes = listOf(BoardList.route, Settings.route)
    }
}
