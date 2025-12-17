package com.ippo.taskflow.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ippo.taskflow.activity.ui.theme.TaskFlowTheme
import com.ippo.taskflow.mvvm.view.group_view.AddGroupScreen
import com.ippo.taskflow.mvvm.view.group_view.AddTaskScreen
import com.ippo.taskflow.mvvm.view.group_view.EditTaskScreen // ✅ 추가
import com.ippo.taskflow.mvvm.view.group_view.GroupDetailScreen
import com.ippo.taskflow.mvvm.view.group_view.GroupTaskScreen
import com.ippo.taskflow.mvvm.view.init_view.FirstScreen
import com.ippo.taskflow.mvvm.view.init_view.LoginScreen
import com.ippo.taskflow.mvvm.view.init_view.RegisterScreen
import com.ippo.taskflow.mvvm.view.main_view.MainScreen
import com.ippo.taskflow.mvvm.view.main_view.ProfileScreen
import com.ippo.taskflow.mvvm.view.main_view.ProfileSettingScreen
import com.ippo.taskflow.mvvm.view.main_view.SettingScreen
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import com.ippo.taskflow.mvvm.view_model.utils.ViewModelFactory

/**
 * 🧭 네비게이션 경로 정의
 */
object Destinations {
    // 1. 초기/인증
    const val FIRST_ROUTE = "first"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"

    // 2. 메인 앱
    const val HOME_ROUTE = "home"
    const val PROFILE_ROUTE = "profile"
    const val SETTINGS_ROUTE = "settings"
    const val PROFILE_SETTING_ROUTE = "profileSetting"
    const val GROUPS_ROUTE = "groups"
    const val GROUP_DETAIL_ROUTE = "groupDetail/{groupId}"
    const val ADD_GROUP_ROUTE = "addGroup"
    const val ADD_TASK_ROUTE = "addTask" // 실제 라우트는 addTask/{groupId}로 사용 중

    const val TASK_DETAIL_ROUTE = "taskDetail/{taskId}"

    // ✅ 추가: EditTask 라우트
    const val EDIT_TASK_ROUTE = "editTask/{taskId}"

    // 인자 전달 함수
    fun groupDetailRoute(groupId: String) = "groupDetail/$groupId"

    // ✅ 추가: EditTask 인자 전달 함수
    fun editTaskRoute(taskId: String) = "editTask/$taskId"
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

    Crossfade(targetState = isAuthenticated, label = "Authentication Crossfade") { authenticated ->
        if (authenticated) {
            MainAppNavHost(
                navController = navController,
                authViewModel = authViewModel,
                taskViewModel = taskViewModel,
                groupViewModel = groupViewModel
            )
        } else {
            AuthNavHost(
                navController = navController,
                authViewModel = authViewModel
            )
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
    NavHost(
        navController = navController,
        startDestination = Destinations.FIRST_ROUTE
    ) {
        composable(Destinations.FIRST_ROUTE) {
            FirstScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = { navController.navigate(Destinations.LOGIN_ROUTE) },
                onNavigateToMain = { /* State change handles MainAppNavHost switch */ }
            )
        }
        composable(Destinations.LOGIN_ROUTE) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToMain = { /* State change handles MainAppNavHost switch */ },
                onNavigateToRegister = { navController.navigate(Destinations.REGISTER_ROUTE) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Destinations.REGISTER_ROUTE) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { navController.popBackStack(Destinations.LOGIN_ROUTE, inclusive = false) }
            )
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
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME_ROUTE
    ) {
        // 1. HOME
        composable(Destinations.HOME_ROUTE) {
            MainScreen(
                authViewModel = authViewModel,
                taskViewModel = taskViewModel,
                onNavigateToSettings = { navController.navigate(Destinations.SETTINGS_ROUTE) },
                onNavigateToProfile = { navController.navigate(Destinations.PROFILE_ROUTE) },
                onNavigateToGroups = { navController.navigate(Destinations.GROUPS_ROUTE) },

                // ✅ 추가: Task 클릭 시 EditTaskScreen으로
                onNavigateToEditTask = { taskId ->
                    navController.navigate(Destinations.editTaskRoute(taskId))
                }
            )
        }

        // 2. PROFILE
        composable(Destinations.PROFILE_ROUTE) {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Destinations.PROFILE_SETTING_ROUTE) }
            )
        }

        // 3. PROFILE SETTINGS
        composable(Destinations.PROFILE_SETTING_ROUTE) {
            ProfileSettingScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSignedOut = { authViewModel.signOut() }
            )
        }

        // 4. SETTINGS
        composable(Destinations.SETTINGS_ROUTE) {
            SettingScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { authViewModel.signOut() },
                onNavigateToProfileSetting = { navController.navigate(Destinations.PROFILE_SETTING_ROUTE) },
                onNavigateToSecurity = { /* TODO */ },
                onNavigateToTheme = { /* TODO */ },
                onNavigateToAbout = { /* TODO */ },
                onNavigateToEtc = { /* TODO */ }
            )
        }

        // 5. GROUPS
        composable(Destinations.GROUPS_ROUTE) {
            GroupTaskScreen(
                groupViewModel = groupViewModel,
                taskViewModel = taskViewModel,
                onNavigateToMain = { navController.navigate(Destinations.HOME_ROUTE) },
                onNavigateToProfile = { navController.navigate(Destinations.PROFILE_ROUTE) },
                onNavigateToAddGroup = { navController.navigate(Destinations.ADD_GROUP_ROUTE) },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Destinations.groupDetailRoute(groupId))
                }
            )
        }

        // 6. ADD GROUP
        composable(Destinations.ADD_GROUP_ROUTE) {
            AddGroupScreen(
                groupViewModel = groupViewModel,
                authViewModel = authViewModel,
                onTaskCreated = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. GROUP DETAIL / TASK LIST
        composable(Destinations.GROUP_DETAIL_ROUTE) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")

            if (groupId != null) {
                GroupDetailScreen(
                    groupId = groupId,
                    groupViewModel = groupViewModel,
                    taskViewModel = taskViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddTask = {
                        navController.navigate("${Destinations.ADD_TASK_ROUTE}/$groupId")
                    },
                    onNavigateToTaskDetail = { taskId ->
                        navController.navigate("${Destinations.TASK_DETAIL_ROUTE}/$taskId")
                    }
                )
            } else {
                Text("Error: Group ID Missing")
            }
        }

        // 8. ADD TASK (addTask/{groupId})
        composable("${Destinations.ADD_TASK_ROUTE}/{groupId}") { backStackEntry ->
            val groupIdFromArgs = backStackEntry.arguments?.getString("groupId")

            if (groupIdFromArgs != null) {
                AddTaskScreen(
                    initialGroupId = groupIdFromArgs,
                    taskViewModel = taskViewModel,
                    groupViewModel = groupViewModel,
                    onTaskCreated = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                Text("Error: Group ID Missing for Add Task")
            }
        }

        // ✅ 9. EDIT TASK (editTask/{taskId})
        composable(Destinations.EDIT_TASK_ROUTE) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")

            if (taskId != null) {
                EditTaskScreen(
                    taskId = taskId,
                    taskViewModel = taskViewModel,
                    onTaskUpdated = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                Text("Error: Task ID Missing for Edit Task")
            }
        }

        // --- (기타 경로 생략) ---
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}
