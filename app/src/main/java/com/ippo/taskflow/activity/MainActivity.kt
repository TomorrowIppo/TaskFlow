package com.ippo.taskflow.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // ViewModel 인스턴스 생성을 위한 import
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ippo.taskflow.ui.theme.TaskFlowTheme
// 🚨 MVVM Integration Imports (외부 파일에서 분리된 컴포넌트들을 가져옴)
import com.ippo.taskflow.screen.FirstScreen // 👈 1. 루트 패키지에서 FirstScreen Composable import
import com.ippo.taskflow.auth.AuthViewModel // ViewModel 클래스 import
import com.ippo.taskflow.screen.LoginScreen // LoginScreen Composable import


// 1. 🚀 네비게이션 경로 상수 정의 (변화 없음)
const val ROUTE_MAIN = "main_screen"
const val ROUTE_LOGIN = "login_screen"
const val ROUTE_ONBOARDING = "onboarding_screen"

// 2. 🖼️ MainActivity: Single Activity Host (변화 없음)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaskFlowTheme {
                AppNavigation()
            }
        }
    }
}

// 3. 🧭 AppNavigation: NavHost (화면 전환 시스템) 정의
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 💡 ViewModel 생성: AuthViewModel은 앱 전체에서 인증 상태를 공유하므로 여기서 한 번만 생성
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = ROUTE_ONBOARDING) {

        // 3-1. FirstScreen (온보딩/스플래시) 정의
        composable(ROUTE_ONBOARDING) {
            FirstScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(ROUTE_LOGIN) { popUpTo(ROUTE_ONBOARDING) { inclusive = true } }
                },
                onNavigateToMain = {
                    navController.navigate(ROUTE_MAIN) { popUpTo(ROUTE_ONBOARDING) { inclusive = true } }
                }
            )
        }

        // 3-2. 🔑 로그인 화면 정의 (MVVM 적용)
        composable(ROUTE_LOGIN) {
            // LoginScreen에게 AuthViewModel을 주입
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToMain = {
                    navController.navigate(ROUTE_MAIN) { popUpTo(ROUTE_LOGIN) { inclusive = true } }
                },
            )
        }

        // 3-3. 메인 TaskFlow 화면 정의 (임시 MainScreen 호출)
        composable(ROUTE_MAIN) {
            MainScreen(
                authViewModel = authViewModel,
                onNavigateToOnboarding = {
                    // 로그아웃 후 온보딩 화면으로 이동하고 스택 정리
                    navController.navigate(ROUTE_ONBOARDING) { popUpTo(ROUTE_MAIN) { inclusive = true } }
                }
            )
        }
    }
}


// 4. 🎨 MainScreen: 임시 구현 (로그아웃 기능)
@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onNavigateToOnboarding: () -> Unit
) {
    val context = LocalContext.current
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    // 💡 로그아웃 상태 변화 감지: 로그아웃되면 온보딩으로 이동
    // (AuthViewModel.signOut() 호출 후 isAuthenticated가 false가 되는 것을 감지)
    if (!isAuthenticated) {
        onNavigateToOnboarding()
        return // 네비게이션이 발생했으므로 UI 렌더링 중단
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "메인 TaskFlow 화면 (로그인됨: ${authViewModel.userId})",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        // 🚀 로그아웃 버튼
        Button(
            onClick = {
                authViewModel.signOut() // ViewModel에 로그아웃 요청
                Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text("로그아웃")
        }
    }
}