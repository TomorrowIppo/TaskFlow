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
    const val FIRST_ROUTE = "first" // 첫 실행 (FirstScreen)
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"

    // 2. 메인 앱
    const val HOME_ROUTE = "home" // MainScreen (DailyTask)
    const val PROFILE_ROUTE = "profile" // ProfileScreen (View)
    const val SETTINGS_ROUTE = "settings" // SettingScreen (Global Settings)
    const val PROFILE_SETTING_ROUTE = "profileSetting" // ProfileSettingScreen (Edit)
    const val GROUPS_ROUTE = "groups" // GroupTaskScreen (Group List)
    const val GROUP_DETAIL_ROUTE = "groupDetail/{groupId}" // GroupDetailScreen (Detail/Task List)
    const val ADD_GROUP_ROUTE = "addGroup" // AddGroupScreen
    const val ADD_TASK_ROUTE = "addTask" // AddTaskScreen

    const val TASK_DETAIL_ROUTE = "taskDetail/{taskId}"

    // 인자 전달 함수
    fun groupDetailRoute(groupId: String) = "groupDetail/$groupId"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TaskFlowTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // AuthViewModel과 TaskViewModel은 표준 생성으로 가정
                    val authViewModel: AuthViewModel = viewModel()
                    val taskViewModel: TaskViewModel = viewModel()

                    // 🚨 GroupViewModel은 Custom Factory를 통해 생성 (DI 해결)
                    // 이 코드가 작동하려면 ViewModelFactory가 GroupVM의 인자를 처리해야 함.
                    val groupViewModelFactory = remember {
                        ViewModelFactory(
                            authViewModel,
                            taskViewModel
                        )
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
    groupViewModel: GroupViewModel // GroupViewModel 추가
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val navController = rememberNavController()

    if (isLoading) {
        LoadingScreen()
        return
    }

    // 인증 상태에 따라 루트 NavHost를 교차하여 로드
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
    groupViewModel: GroupViewModel // GroupViewModel 추가
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME_ROUTE
    ) {
        // 1. HOME (MainScreen - Daily Task View)
        composable(Destinations.HOME_ROUTE) {
            MainScreen(
                authViewModel = authViewModel,
                taskViewModel = taskViewModel,
                onNavigateToSettings = { navController.navigate(Destinations.SETTINGS_ROUTE) },
                onNavigateToProfile = { navController.navigate(Destinations.PROFILE_ROUTE) },
                onNavigateToGroups = { navController.navigate(Destinations.GROUPS_ROUTE) }
            )
        }

        // 2. PROFILE (ProfileScreen - View)
        composable(Destinations.PROFILE_ROUTE) {
            ProfileScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSettings = { navController.navigate(Destinations.PROFILE_SETTING_ROUTE) }
            )
        }

        // 3. PROFILE SETTINGS (ProfileSettingScreen - Edit)
        composable(Destinations.PROFILE_SETTING_ROUTE) {
            ProfileSettingScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSignedOut = { authViewModel.signOut() }
            )
        }

        // 4. SETTINGS (Global Settings)
        composable(Destinations.SETTINGS_ROUTE) {
            SettingScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = { authViewModel.signOut() }, // 로그아웃 시 AuthNavHost로 전환됨
                onNavigateToProfileSetting = { navController.navigate(Destinations.PROFILE_SETTING_ROUTE) },
                onNavigateToSecurity = { /* TODO */ },
                onNavigateToTheme = { /* TODO */ },
                onNavigateToAbout = { /* TODO */ },
                onNavigateToEtc = { /* TODO */ }
            )
        }

        // 5. GROUPS (GroupTaskScreen - List View)
        composable(Destinations.GROUPS_ROUTE) {
            GroupTaskScreen(
                groupViewModel = groupViewModel,
                // Task 통계 계산 및 Task 생성 로직은 GroupTaskScreen 내부에서 TaskViewModel을 호출
                onNavigateToMain = { navController.navigate(Destinations.HOME_ROUTE) },
                onNavigateToProfile = { navController.navigate(Destinations.PROFILE_ROUTE) },
                onNavigateToAddGroup = { navController.navigate(Destinations.ADD_GROUP_ROUTE) },
                onNavigateToGroupDetail = { groupId ->
                    navController.navigate(Destinations.groupDetailRoute(groupId))
                }
            )
        }

        // 6. ADD GROUP (AddGroupScreen)
        composable(Destinations.ADD_GROUP_ROUTE) {
            // GroupViewModel, AuthViewModel 필요 (이메일 검색)
            AddGroupScreen(
                groupViewModel = groupViewModel,
                authViewModel = authViewModel,
                onTaskCreated = { navController.popBackStack() }, // 생성 완료 후 이전 화면(Group List)으로 복귀
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // 7. GROUP DETAIL / TASK LIST (GroupDetailScreen)
        composable(Destinations.GROUP_DETAIL_ROUTE) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")

            if (groupId != null) {
                GroupDetailScreen(
                    groupId = groupId, // 🚨 인자 전달
                    groupViewModel = groupViewModel,
                    taskViewModel = taskViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAddTask = {
                        // Task 생성 화면으로 이동 시, 현재 GroupId를 함께 전달해야 함
                        navController.navigate("${Destinations.ADD_TASK_ROUTE}/$groupId")
                    },

                    // ⭐️ 추가된 콜백: Task ID를 받아 Task 상세 화면으로 이동합니다.
                    onNavigateToTaskDetail = { taskId ->
                        // Task 상세 화면 경로로 네비게이션합니다.
                        navController.navigate("${Destinations.TASK_DETAIL_ROUTE}/$taskId")
                    }
                )
            } else {
                Text("Error: Group ID Missing")
            }
        }

        // 8. ADD TASK (AddTaskScreen)
        composable("${Destinations.ADD_TASK_ROUTE}/{groupId}") { backStackEntry -> // ⭐️ 인자 경로 명시
            val groupIdFromArgs = backStackEntry.arguments?.getString("groupId") // ⭐️ groupId 인자 추출

            if (groupIdFromArgs != null) {
                AddTaskScreen(
                    // 추출한 groupId를 AddTaskScreen에 전달
                    initialGroupId = groupIdFromArgs,
                    taskViewModel = taskViewModel,
                    groupViewModel = groupViewModel,
                    onTaskCreated = { navController.popBackStack() },
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                // GroupId가 없으면 오류 텍스트를 표시하거나 뒤로가기 처리
                Text("Error: Group ID Missing for Add Task")
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