package com.ippo.taskflow.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ippo.taskflow.ui.theme.TaskFlowTheme
import com.ippo.taskflow.auth.AuthViewModel
import com.ippo.taskflow.group.GroupViewModel
import com.ippo.taskflow.task.TaskViewModel
import com.ippo.taskflow.data.Group
import com.ippo.taskflow.data.Task
import com.ippo.taskflow.screen.TestScreen
import com.ippo.taskflow.utils.ViewModelFactory // 🚨 ViewModelFactory Import

// 🚨 PM Note: 주 색상 상수 정의
val TaskFlowGreen = Color(0xFF1E8A3B)


// 1. 🧪 TestActivity: Group/Task ViewModel 테스트 호스트
class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            // ViewModels 생성 (Activity Scope)
            val authViewModel: AuthViewModel = viewModel()
            val taskViewModel: TaskViewModel = viewModel()

            // 🚨 Custom Factory를 생성하고 TaskViewModel과 AuthViewModel을 인자로 전달합니다. (DI 해결)
            val groupViewModelFactory = remember { ViewModelFactory(authViewModel, taskViewModel) }
            val groupViewModel: GroupViewModel = viewModel(factory = groupViewModelFactory)

            GroupTestHostScreen(
                authViewModel = authViewModel,
                groupViewModel = groupViewModel,
                taskViewModel = taskViewModel
            )
        }
    }
}


// 2. 🏠 GroupTestHostScreen: 인증 상태 관리 및 TestScreen 호출
@Composable
fun GroupTestHostScreen(
    authViewModel: AuthViewModel,
    groupViewModel: GroupViewModel,
    taskViewModel: TaskViewModel
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isAuthLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.error.collectAsState()

    // 🚨 Activity 시작 시 자동으로 익명 로그인 시도 (테스트 세션 확보)
    LaunchedEffect(Unit) {
        if (!isAuthenticated && !isAuthLoading) {
            authViewModel.signInAsGuest()
            Log.d("TestAuth", "GroupTestHostScreen 시작: 익명 로그인 시도...")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (isAuthLoading) {
            CircularProgressIndicator(Modifier.padding(top = 50.dp))
            Text("인증 토큰 획득 중...")
        } else if (!isAuthenticated) {
            Text("인증 실패: ${authError ?: "로그인이 필요합니다."}", color = Color.Red)
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { authViewModel.signInAsGuest() }) {
                Text("Guest 로그인 재시도")
            }
        } else {
            // 💡 인증 성공 시 TestScreen 호출
            Text("✅ 인증 성공! UID: ${authViewModel.userId!!.take(10)}...", color = TaskFlowGreen, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))

            // 핵심 테스트 화면
            TestScreen(
                groupViewModel = groupViewModel,
                taskViewModel = taskViewModel
            )
        }

        // 🚨 로그아웃 버튼
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { authViewModel.signOut() }, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
            Text("세션 종료 (로그아웃)")
        }
    }
}