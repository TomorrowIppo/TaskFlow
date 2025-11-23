// [새 파일] FirstScreen.kt 파일

package com.ippo.taskflow.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.auth.AuthViewModel

// 💡 MVVM 표준: AuthViewModel과 네비게이션 액션만 인자로 받는다.
@Composable
fun FirstScreen(
    authViewModel: AuthViewModel, // 👈 ViewModel 주입
    onNavigateToLogin: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    // 1. ViewModel의 상태를 관찰한다.
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()

    // 2. 🔥 인증 상태 변경 시 Navigation 처리 (로직 처리)
    LaunchedEffect(isAuthenticated) {
        // ViewModel이 이미 인증 상태를 가지고 있으므로, View는 그 상태가 true일 때만 네비게이션 명령을 호출한다.
        if (isAuthenticated) {
            onNavigateToMain()
        }
    }

    // UI 레이아웃 구현 (이전과 동일)
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
            // ... UI 요소 (이미지, 제목 등) ...
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

        // 🟢 시작하기 버튼 (로그아웃 상태일 때만 표시)
        if (!isAuthenticated) {
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E8A3B))
            ) {
                Text(text = "시작하기", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}