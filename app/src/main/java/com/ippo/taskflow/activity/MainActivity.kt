package com.ippo.taskflow.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel 인스턴스 생성을 위한 Import
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ippo.taskflow.activity.ui.theme.TaskFlowTheme // 기존 테마 Import
// 🚨 필수 MVVM/Screen Imports (경로 확인 필요)
import com.ippo.taskflow.screen.FirstScreen
import com.ippo.taskflow.auth.AuthViewModel

// 🚨 NavHost 경로 상수 정의 (MainActivity에 위치)
const val ROUTE_MAIN = "main_screen"
const val ROUTE_LOGIN = "login_screen"
const val ROUTE_ONBOARDING = "onboarding_screen" // FirstScreen 경로
const val ROUTE_REGISTER = "register_screen" // 회원가입 경로
const val ROUTE_CRUD_TEST = "crud_test_screen" // 테스트 화면 경로 (필요시)

// [기존 Greeting 및 Preview 함수는 삭제하거나 다른 파일로 옮기셔야 합니다.]

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🚨 기존 setContent 로직을 AppNavigation으로 대체
        setContent {
            TaskFlowTheme {
                // SAA: MainActivity가 NavHost를 호스팅합니다.
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

    // 💡 ViewModel 생성 (DI는 TestActivity에서 진행했으나, 여기서는 표준 생성으로 가정)
    val authViewModel: AuthViewModel = viewModel()

    // AuthViewModel의 상태를 이용해 FirstScreen에서 바로 Main으로 분기하도록 연결
    NavHost(
        navController = navController,
        startDestination = ROUTE_ONBOARDING // 🚨 FirstScreen을 시작점으로 설정
    ) {

        // 1. FirstScreen (Onboarding/Splash) 경로
        composable(ROUTE_ONBOARDING) {
            FirstScreen(
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

        // 2. LoginScreen 경로 (Placeholder)
        composable(ROUTE_LOGIN) {
            // TODO: LoginScreen Composable을 호출하여 구현
            Text("Login Screen Placeholder", modifier = Modifier.fillMaxSize())
        }

        // 3. MainScreen 경로 (Placeholder)
        composable(ROUTE_MAIN) {
            // TODO: MainScreen Composable을 호출하여 구현
            Text("Main Screen Placeholder", modifier = Modifier.fillMaxSize())
        }

        // ... (나머지 ROUTE_REGISTER, ROUTE_CRUD_TEST 등 추가 가능)
    }
}