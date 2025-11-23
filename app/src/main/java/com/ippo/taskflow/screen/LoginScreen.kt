package com.ippo.taskflow.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ippo.taskflow.auth.AuthViewModel

// 💡 MVVM 표준: ViewModel을 인자로 받고, 네비게이션 액션을 받는다.
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel, // 👈 ViewModel 주입
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 1. ViewModel의 상태를 관찰한다.
    val isLoading by authViewModel.isLoading.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val userId = authViewModel.userId
    val error by authViewModel.error.collectAsState() // 에러 메시지 관찰

    // 에러 발생 시 Toast 처리 (View의 역할)
    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, "로그인 실패: $error", Toast.LENGTH_LONG).show()
            // 에러를 처리했으면 다시 null로 초기화
            authViewModel.clearError() // (AuthViewModel에 clearError 함수가 있어야 함)
        }
    }

    // 인증 상태 변경 시 Navigation 처리 (View의 역할)
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            Toast.makeText(context, "익명 로그인 성공!", Toast.LENGTH_SHORT).show()
            onNavigateToMain()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!isAuthenticated) {
            // ... UI ...
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = {
                    // 2. 버튼 클릭 시 ViewModel의 메서드만 호출한다.
                    authViewModel.signInAsGuest()
                }) {
                    Text("게스트 로그인")
                }
            }
        } else {
            // ... (로그인 상태일 때 UI) ...
            Button(onClick = {
                authViewModel.signOut() // 3. 로그아웃도 ViewModel에게 위임
                Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
            }) {
                Text("로그아웃")
            }
        }
    }
}