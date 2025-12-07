package com.ippo.taskflow.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel 인스턴스 생성을 위한 Import
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ippo.taskflow.activity.ui.theme.TaskFlowTheme
// 🚨 필수 MVVM/Screen Imports
import com.ippo.taskflow.mvvm.view.init_view.FirstScreen
import com.ippo.taskflow.mvvm.view.init_view.LoginScreen
import com.ippo.taskflow.mvvm.view.main_view.MainScreen
import com.ippo.taskflow.mvvm.view.init_view.RegisterScreen
import com.ippo.taskflow.mvvm.view.main_view.SettingScreen
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel // TaskViewModel Import

// 🚨 NavHost 경로 상수 정의 (수정 없이 기존 유지)
const val ROUTE_MAIN = "main_screen"
const val ROUTE_LOGIN = "login_screen"
const val ROUTE_ONBOARDING = "onboarding_screen" // FirstScreen 경로
const val ROUTE_REGISTER = "register_screen" // 회원가입 경로
const val ROUTE_SETTINGS = "settings_screen" // 설정 경로 (추가)
const val ROUTE_CRUD_TEST = "crud_test_screen" // 테스트 화면 경로 (필요시)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaskFlowTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 💡 ViewModel 생성
    val authViewModel: com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel = viewModel()
    val taskViewModel: com.ippo.taskflow.mvvm.view_model.task.TaskViewModel = viewModel() // TaskViewModel 추가

    // AuthViewModel의 상태를 이용해 FirstScreen에서 바로 Main으로 분기하도록 연결
    NavHost(
        navController = navController,
        startDestination = ROUTE_ONBOARDING // FirstScreen을 시작점으로 설정
    ) {

        // 1. FirstScreen (Onboarding/Splash) 경로
        composable(ROUTE_ONBOARDING) {
            _root_ide_package_.com.ippo.taskflow.mvvm.view.init_view.FirstScreen(
                authViewModel = authViewModel,
                // NavHost로 화면 전환 액션 정의
                onNavigateToLogin = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_ONBOARDING) { inclusive = true } // 백스택 정리
                    }
                },
                onNavigateToMain = {
                    navController.navigate(ROUTE_MAIN) {
                        popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // 2. LoginScreen 경로
        composable(ROUTE_LOGIN) {
            _root_ide_package_.com.ippo.taskflow.mvvm.view.init_view.LoginScreen(
                authViewModel = authViewModel,
                onNavigateToMain = {
                    navController.navigate(ROUTE_MAIN) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(ROUTE_REGISTER)
                },
                onNavigateBack = {
                    navController.popBackStack() // 뒤로가기
                }
            )
        }

        // 3. RegisterScreen 경로
        composable(ROUTE_REGISTER) {
            _root_ide_package_.com.ippo.taskflow.mvvm.view.init_view.RegisterScreen(
                authViewModel = authViewModel, // 회원가입 로직은 VM에서 처리
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_REGISTER) { inclusive = true }
                    }
                }
            )
        }

        // 4. MainScreen 경로
        composable(ROUTE_MAIN) {
            _root_ide_package_.com.ippo.taskflow.mvvm.view.main_view.MainScreen(
                authViewModel = authViewModel,
                taskViewModel = taskViewModel,
                onNavigateToSettings = { navController.navigate(ROUTE_SETTINGS) },
                onNavigateToProfile = { /* TODO: Profile Screen */ },
                onNavigateToGroups = { /* TODO: Groups Screen */ }
            )
        }

        // 5. SettingScreen 경로 (새로 추가)
        composable(ROUTE_SETTINGS) {
            _root_ide_package_.com.ippo.taskflow.mvvm.view.main_view.SettingScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(ROUTE_LOGIN) {
                        // 로그아웃 시 Main, Settings 모두 백스택에서 제거
                        popUpTo(ROUTE_MAIN) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfileSetting = { /* TODO: ProfileSetting Screen */ },
                onNavigateToSecurity = { /* TODO: Security Screen */ },
                onNavigateToTheme = { /* TODO: Theme Screen */ },
                onNavigateToAbout = { /* TODO: About Screen */ },
                onNavigateToEtc = { /* TODO: Etc Screen */ }
            )
        }

        // ... (나머지 ROUTE_CRUD_TEST 등 추가 가능)
    }
}