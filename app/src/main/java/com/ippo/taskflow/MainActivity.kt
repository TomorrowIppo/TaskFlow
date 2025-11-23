package com.ippo.taskflow

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.ippo.taskflow.ui.theme.TaskFlowTheme // 네 프로젝트의 Theme 경로로 변경

// 1. 🚀 네비게이션 경로 상수 정의
const val ROUTE_MAIN = "main_screen"
const val ROUTE_LOGIN = "login_screen"
const val ROUTE_ONBOARDING = "onboarding_screen"

// 2. 🖼️ MainActivity: Single Activity Host
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Android 14+ 엣지 투 엣지 설정

        setContent {
            TaskFlowTheme {
                // NavHost를 호스팅하는 Composable 호출
                AppNavigation()
            }
        }
    }
}

// 3. 🧭 AppNavigation: NavHost (화면 전환 시스템) 정의
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // 시작 지점을 온보딩(FirstScreen)으로 설정
    NavHost(navController = navController, startDestination = ROUTE_ONBOARDING) {

        // 3-1. FirstScreen (온보딩/스플래시) 정의
        composable(ROUTE_ONBOARDING) {
            FirstScreen(
                onNavigateToLogin = {
                    navController.navigate(ROUTE_LOGIN) {
                        popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                    }
                },
                onNavigateToMain = {
                    navController.navigate(ROUTE_MAIN) {
                        popUpTo(ROUTE_ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // 3-2. 로그인 화면 정의 (AuthScreen Composable 연결)
        composable(ROUTE_LOGIN) {
            AuthScreen(
                onNavigateToMain = {
                    navController.navigate(ROUTE_MAIN) {
                        popUpTo(ROUTE_LOGIN) { inclusive = true }
                    }
                },
            )
        }

        // 3-3. 메인 TaskFlow 화면 정의 (향후 구현)
        composable(ROUTE_MAIN) {
            Text(
                "메인 TaskFlow 화면",
                modifier = Modifier.fillMaxSize(),
                textAlign = TextAlign.Center
            )
        }
    }
}


// 4. 🎨 FirstScreen: UI 및 초기 인증 체크 로직
@Composable
fun FirstScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    // 🔥 Firebase 인증 상태 확인 (초기 로직)
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            onNavigateToMain() // 로그인 상태면 바로 메인으로
        }
    }

    // UI 레이아웃 구현 (생략된 이미지 리소스는 R.drawable.illustration_placeholder로 대체)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE0FFDD))
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // 이미지 영역 (주석 처리된 이미지 리소스를 대체해야 함)
            Spacer(modifier = Modifier.height(250.dp))

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "TaskFlow",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E8A3B)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "이 생산성 도구는 프로젝트별로 작업을 더 쉽고 효율적으로 관리할 수 있도록 도와줍니다!",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E8A3B))
        ) {
            Text(text = "시작하기", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}


// 5. 🔑 AuthScreen: 로그인 로직 (익명 로그인 기능 포함)
@Composable
fun AuthScreen(
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val auth = Firebase.auth
    val context = LocalContext.current

    var userId by remember { mutableStateOf(auth.currentUser?.uid) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (userId == null) {
            // 로그아웃 상태일 때
            Text(text = "게스트로 로그인하여 시작하세요.")
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = {
                    isLoading = true
                    auth.signInAnonymously()
                        .addOnCompleteListener { task ->
                            isLoading = false
                            if (task.isSuccessful) {
                                userId = auth.currentUser?.uid
                                Toast.makeText(context, "익명 로그인 성공!", Toast.LENGTH_SHORT).show()
                                onNavigateToMain() // 🚀 로그인 성공 시 메인으로 이동
                            } else {
                                userId = null
                                Toast.makeText(context, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }) {
                    Text("게스트 로그인")
                }
            }
        } else {
            // 로그인 상태일 때
            Text(text = "환영합니다, Guest ($userId)!", modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                auth.signOut()
                userId = null
                Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
            }) {
                Text("로그아웃")
            }
        }
    }
}