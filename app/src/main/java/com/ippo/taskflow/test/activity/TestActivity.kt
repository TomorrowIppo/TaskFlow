package com.ippo.taskflow.test.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ippo.taskflow.ui.theme.TaskFlowTheme
import com.ippo.taskflow.view_model.auth.AuthViewModel
import com.ippo.taskflow.view_model.group.GroupViewModel
import com.ippo.taskflow.screen.auth.LoginScreen
import com.ippo.taskflow.screen.auth.RegisterScreen
import com.ippo.taskflow.view_model.task.TaskViewModel
import com.ippo.taskflow.test.screen.ProfileScreen
import com.ippo.taskflow.test.screen.ProfileSettingScreen
import com.ippo.taskflow.test.screen.TestCRUDScreen
import com.ippo.taskflow.view_model.utils.ViewModelFactory

// =========================================================================
// 0. NavHost Route 상수 정의 (가독성 향상)
// =========================================================================

private object TestRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CRUD_TEST = "crud_test"
    const val PROFILE = "profile"
    const val PROFILE_SETTING = "profile_setting"
}

// 1. 🧪 TestActivity: Single Activity Host
class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaskFlowTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    TestAppHost()
                }
            }
        }
    }
}

// 2. 🧭 TestAppHost: ViewModel 생성 및 NavHost 관리 (핵심 컴포넌트)
@Composable
fun TestAppHost() {
    val navController = rememberNavController()

    // 🚨 1. ViewModel 생성 영역 집중화
    val authViewModel: AuthViewModel = viewModel()
    val taskViewModel: TaskViewModel = viewModel()

    // Custom Factory를 사용해 GroupViewModel 생성 (의존성 주입)
    // ViewModelFactory는 이 파일에 포함되어 있지 않으므로 import에 의존
    val groupViewModelFactory = remember { ViewModelFactory(authViewModel, taskViewModel) }
    val groupViewModel: GroupViewModel = viewModel(factory = groupViewModelFactory)

    // 2. NavHost 설정
    NavHost(
        navController = navController,
        startDestination = TestRoutes.LOGIN
    ) {

        // --- 1. Authentication Screens ---

        composable(TestRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToMain = {
                    navController.navigate(TestRoutes.CRUD_TEST) {
                        popUpTo(TestRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(TestRoutes.REGISTER) }
            )
        }

        composable(TestRoutes.REGISTER) {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    // 회원가입 성공 시 LoginScreen으로 복귀하며, RegisterScreen을 백스택에서 제거
                    navController.navigate(TestRoutes.LOGIN) {
                        popUpTo(TestRoutes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // --- 2. Test & Functional Screens ---

        composable(TestRoutes.CRUD_TEST) {
            TestCRUDScreen(
                groupViewModel = groupViewModel,
                taskViewModel = taskViewModel,
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(TestRoutes.LOGIN) {
                        popUpTo(TestRoutes.LOGIN) { inclusive = true }
                    }
                },
                // ⭐️ 수정: Profile 화면으로 이동하는 콜백으로 변경 ⭐️
                onNavigateToProfile = {
                    navController.navigate(TestRoutes.PROFILE) // PROFILE 경로로 이동
                }
            )
        }

        // ⭐️ 3. Profile Screen ⭐️
        composable(TestRoutes.PROFILE) {
            ProfileScreen(
                authViewModel = authViewModel,
                // ProfileScreen 내부에서 Settings 버튼 클릭 시 Setting 화면으로 이동
                onNavigateToSettings = { navController.navigate(TestRoutes.PROFILE_SETTING) },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(TestRoutes.LOGIN) {
                        popUpTo(TestRoutes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateUp = { navController.popBackStack() }
            )
        }

        // ⭐️ 4. Profile Setting Screen ⭐️
        composable(TestRoutes.PROFILE_SETTING) {
            ProfileSettingScreen(
                authViewModel = authViewModel,
                onNavigateUp = { navController.popBackStack() }
            )
        }
    }
}