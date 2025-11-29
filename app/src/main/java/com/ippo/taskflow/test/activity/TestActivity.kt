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
import com.ippo.taskflow.auth.AuthViewModel
import com.ippo.taskflow.group.GroupViewModel
import com.ippo.taskflow.task.TaskViewModel
import com.ippo.taskflow.screen.auth.LoginScreen
import com.ippo.taskflow.screen.auth.RegisterScreen
import com.ippo.taskflow.test.screen.TestCRUDScreen
import com.ippo.taskflow.utils.ViewModelFactory // 🚨 Custom Factory Import

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

    // 🚨 1. TaskViewModel, AuthViewModel 생성 (표준 팩토리 사용)
    val taskViewModel: TaskViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    // 🚨 2. Custom Factory 생성: GroupViewModel의 의존성(Auth, Task) 주입을 위해 사용
    val groupViewModelFactory = remember { ViewModelFactory(authViewModel, taskViewModel) }

    // 3. GroupViewModel은 Custom Factory를 통해 생성
    val groupViewModel: GroupViewModel = viewModel(factory = groupViewModelFactory)

    // 4. NavHost 설정
    NavHost(
        navController = navController,
        startDestination = "login" // 초기 화면은 로그인 테스트
    ) {

        // --- 1. Login Screen ---
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToMain = { navController.navigate("crud_test") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        // --- 2. Register Screen ---
        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    // 회원가입 성공 시 LoginScreen으로 복귀하며, RegisterScreen을 백스택에서 제거
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // --- 3. CRUD Test Screen ---
        composable("crud_test") {
            TestCRUDScreen(
                groupViewModel = groupViewModel,
                taskViewModel = taskViewModel,
                // 로그아웃 시 세션 종료 후 로그인 화면으로 이동
                onLogout = {
                    authViewModel.signOut() // Firebase 세션 종료
                    // 로그인 화면으로 이동하며, CRUD 화면을 백스택에서 제거합니다.
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
    }
}