package com.ippo.taskflow.mvvm.view.main_view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ippo.taskflow.R
import com.ippo.taskflow.activity.Destinations

private data class BottomNavItem(
    val route: String,
    val label: String,
    val iconRes: Int
)

@Composable
fun TaskFlowBottomNavBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem(
            route = Destinations.HOME_ROUTE,
            label = "홈",
            iconRes = R.drawable.ic_home
        ),
        BottomNavItem(
            route = Destinations.GROUPS_ROUTE,
            label = "그룹",
            iconRes = R.drawable.ic_taskflow
        ),
        BottomNavItem(
            route = Destinations.PROFILE_ROUTE,
            label = "프로필",
            iconRes = R.drawable.ic_profile
        )
    )

    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route.orEmpty()

    val selectedRoute = when {
        currentRoute.startsWith("groupDetail") -> Destinations.GROUPS_ROUTE
        currentRoute.startsWith("addGroup") -> Destinations.GROUPS_ROUTE
        currentRoute.startsWith("addTask") -> Destinations.GROUPS_ROUTE
        currentRoute.startsWith("taskDetail") -> Destinations.GROUPS_ROUTE
        currentRoute.startsWith("profileSetting") -> Destinations.PROFILE_ROUTE
        else -> currentRoute
    }

    val barColor = Color(0xFFACFFC1)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(barColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = selectedRoute == item.route
            val tint = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Black
            }

            IconButton(
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Image(
                    painter = painterResource(id = item.iconRes),
                    contentDescription = item.label,
                    modifier = Modifier.size(40.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(tint)
                )
            }
        }
    }
}