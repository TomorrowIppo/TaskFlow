package com.ippo.taskflow.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.ippo.taskflow.activity.ui.theme.TaskFlowTheme
import com.ippo.taskflow.mvvm.view.group_view.*
import com.ippo.taskflow.mvvm.view.init_view.*
import com.ippo.taskflow.mvvm.view.main_view.*
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import com.ippo.taskflow.mvvm.view_model.utils.ViewModelFactory

/**
 * 🧭 네비게이션 경로 정의
 */
object Destinations {
    const val FIRST_ROUTE = "first"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"

    const val HOME_ROUTE = "home"
    const val PROFILE_ROUTE = "profile"
    const val SETTINGS_ROUTE = "settings"
    const val PROFILE_SETTING_ROUTE = "profileSetting"
    const val GROUPS_ROUTE = "groups"
    const val GROUP_DETAIL_ROUTE = "groupDetail/{groupId}"
    const val ADD_GROUP_ROUTE = "addGroup"
    const val ADD_TASK_ROUTE = "addTask/{groupId}"
    const val TASK_DETAIL_ROUTE = "taskDetail/{taskId}"

    const val EDIT_TASK_ROUTE = "editTask/{taskId}"

    fun groupDetailRoute(groupId: String) = "groupDetail/$groupId"
    fun editTaskRoute(taskId: String) = "taskDetail/$taskId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskFlowTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel()
                    val taskViewModel: TaskViewModel = viewModel()
                    val groupViewModelFactory = remember {
                        ViewModelFactory(authViewModel, taskViewModel)
                    }
                    val groupViewModel: GroupViewModel = viewModel(factory = groupViewModelFactory)

                    TaskFlowApp(
                        authViewModel = authViewModel,
                        taskViewModel = taskViewModel,
                        groupViewModel = groupViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun TaskFlowApp(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    groupViewModel: GroupViewModel
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val navController = rememberNavController()

    if (isLoading) {
        LoadingScreen()
        return
    }

    Crossfade(targetState = isAuthenticated, label = "AuthSwitch") { authenticated ->
        if (authenticated) {
            MainAppNavHost(navController, authViewModel, taskViewModel, groupViewModel)
        } else {
            AuthNavHost(navController, authViewModel)
        }
    }
}

// -------------------------------------------------------------
// 인증 및 초기 진입 네비게이션 호스트
// -------------------------------------------------------------
@Composable
fun AuthNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(navController = navController, startDestination = Destinations.FIRST_ROUTE) {
        composable(Destinations.FIRST_ROUTE) {
            FirstScreen(authViewModel, { navController.navigate(Destinations.LOGIN_ROUTE) }, {})
        }
        composable(Destinations.LOGIN_ROUTE) {
            LoginScreen(authViewModel, {}, { navController.navigate(Destinations.REGISTER_ROUTE) }, { navController.popBackStack() })
        }
        composable(Destinations.REGISTER_ROUTE) {
            RegisterScreen(authViewModel, { navController.popBackStack() }, { navController.popBackStack(Destinations.LOGIN_ROUTE, false) })
        }
    }
}

// -------------------------------------------------------------
// 메인 앱 네비게이션 호스트 (인증 후)
// -------------------------------------------------------------
@Composable
fun MainAppNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    groupViewModel: GroupViewModel
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route.orEmpty()

    Scaffold(
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                TaskFlowBottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Destinations.HOME_ROUTE,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            composable(Destinations.HOME_ROUTE) {
                MainScreen(authViewModel, taskViewModel,
                    { navController.navigate(Destinations.SETTINGS_ROUTE) },
                    { navController.navigate(Destinations.PROFILE_ROUTE) },
                    { navController.navigate(Destinations.GROUPS_ROUTE) },
                    { taskId -> navController.navigate(Destinations.editTaskRoute(taskId)) }
                )
            }

            composable(Destinations.PROFILE_ROUTE) {
                ProfileScreen(authViewModel, taskViewModel, { navController.popBackStack() }, { navController.navigate(Destinations.PROFILE_SETTING_ROUTE) })
            }

            composable(Destinations.PROFILE_SETTING_ROUTE) {
                ProfileSettingScreen(authViewModel, { navController.popBackStack() }, { authViewModel.signOut() })
            }

            composable(Destinations.SETTINGS_ROUTE) {
                SettingScreen(authViewModel, { navController.popBackStack() }, { authViewModel.signOut() }, { navController.navigate(Destinations.PROFILE_SETTING_ROUTE) }, {}, {}, {}, {})
            }

            composable(Destinations.GROUPS_ROUTE) {
                GroupTaskScreen(groupViewModel, taskViewModel, { navController.navigate(Destinations.HOME_ROUTE) }, { navController.navigate(Destinations.PROFILE_ROUTE) }, { navController.navigate(Destinations.ADD_GROUP_ROUTE) }, { gid -> navController.navigate(Destinations.groupDetailRoute(gid)) })
            }

            composable(Destinations.ADD_GROUP_ROUTE) {
                AddGroupScreen(groupViewModel, authViewModel, { navController.popBackStack() }, { navController.popBackStack() })
            }

            composable(Destinations.GROUP_DETAIL_ROUTE) { entry ->
                val gid = entry.arguments?.getString("groupId")
                if (gid != null) {
                    GroupDetailScreen(
                        groupId = gid,
                        groupViewModel = groupViewModel,
                        taskViewModel = taskViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToAddTask = { navController.navigate("addTask/$gid") },
                        onNavigateToTaskDetail = { tid -> navController.navigate("taskDetail/$tid") }
                    )
                }
            }

            composable(Destinations.ADD_TASK_ROUTE) { entry ->
                val gid = entry.arguments?.getString("groupId")
                if (gid != null) {
                    AddTaskScreen(gid, taskViewModel, groupViewModel, { navController.popBackStack() }, { navController.popBackStack() })
                }
            }

            // ⭐️ [에러 수정] EditTaskScreen 호출 인자 4개 일치화 및 람다 문법 수정
            composable(Destinations.TASK_DETAIL_ROUTE) { entry ->
                val tid = entry.arguments?.getString("taskId")
                if (tid != null) {
                    EditTaskScreen(
                        taskId = tid,
                        taskViewModel = taskViewModel,
                        onTaskUpdated = {
                            navController.popBackStack()
                        },
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
}

private fun shouldShowBottomBar(route: String): Boolean {
    if (route.isBlank()) return false
    return when {
        route == Destinations.FIRST_ROUTE || route == Destinations.LOGIN_ROUTE || route == Destinations.REGISTER_ROUTE -> false
        route.startsWith("groupDetail") || route.startsWith("addGroup") || route.startsWith("addTask") || route.startsWith("taskDetail") || route == Destinations.PROFILE_SETTING_ROUTE -> false
        else -> true
    }
}