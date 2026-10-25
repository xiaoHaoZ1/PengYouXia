package com.pengyouxia.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pengyouxia.app.ui.exchanges.ExchangesScreen
import com.pengyouxia.app.ui.frienddetail.FriendDetailScreen
import com.pengyouxia.app.ui.friends.FriendsScreen
import com.pengyouxia.app.ui.friends.edit.FriendEditScreen
import com.pengyouxia.app.ui.mine.MineScreen

/** 底部三个一级 tab。 */
private enum class TopTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
) {
    FRIENDS("friends", "朋友", Icons.Filled.People, Icons.Outlined.People),
    EXCHANGES("exchanges", "人情", Icons.Filled.Redeem, Icons.Outlined.Redeem),
    MINE("mine", "我的", Icons.Filled.Person, Icons.Outlined.Person),
}

/** 二级路由（无底栏）。 */
object Routes {
    const val FRIEND_NEW = "friend/new"
    const val FRIEND_DETAIL = "friend/{id}"
    const val FRIEND_EDIT = "friend/{id}/edit"

    fun friendDetail(id: String) = "friend/$id"
    fun friendEdit(id: String) = "friend/$id/edit"
}

/** 应用导航容器：底部三 tab（仅一级路由显示），二级页隐藏底栏。 */
@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevel = TopTab.entries.any { it.route == currentRoute }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (topLevel) BottomBar(navController, currentRoute)
        }
    ) { innerPadding ->
        // 顶部留空由 PageHeader 的 statusBarsPadding 处理，底栏占位只消费 bottom
        NavHost(
            navController = navController,
            startDestination = TopTab.FRIENDS.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
        ) {
            composable(TopTab.FRIENDS.route) {
                FriendsScreen(
                    onAdd = { navController.navigate(Routes.FRIEND_NEW) },
                    onOpenFriend = { navController.navigate(Routes.friendDetail(it)) },
                )
            }
            composable(TopTab.EXCHANGES.route) { ExchangesScreen() }
            composable(TopTab.MINE.route) { MineScreen() }
            // 精确路由须在 friend/{id} 之前声明，否则 "friend/new" 被当作 id
            composable(Routes.FRIEND_NEW) {
                FriendEditScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { id ->
                        navController.navigate(Routes.friendDetail(id)) {
                            popUpTo(Routes.FRIEND_NEW) { inclusive = true }
                        }
                    },
                    onDeleted = { navController.popBackStack() },
                )
            }
            composable(Routes.FRIEND_DETAIL) {
                FriendDetailScreen(
                    onBack = { navController.popBackStack() },
                    onEdit = { id -> navController.navigate(Routes.friendEdit(id)) },
                )
            }
            composable(Routes.FRIEND_EDIT) {
                FriendEditScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() },
                    onDeleted = {
                        navController.popBackStack(TopTab.FRIENDS.route, inclusive = false)
                    },
                )
            }
        }
    }
}

@Composable
private fun BottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer) {
        TopTab.entries.forEach { tab ->
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
        }
    }
}
