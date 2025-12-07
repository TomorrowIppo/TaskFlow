package com.ippo.taskflow.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.view_model.auth.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    // 🚨 로그인 성공 시 Main 화면으로 이동
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onNavigateToMain()
        }
    }

    // ... UI 구현은 이전 디자인 (TaskFlow Green)을 따른다고 가정합니다.

    Column(modifier = Modifier.padding(16.dp)) {
        Text("로그인", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("이메일") })
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("비밀번호") })

        Spacer(modifier = Modifier.height(24.dp))

        // 🔑 로그인 및 게스트 버튼 Row
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { authViewModel.signInAsGuest() }, // 🚨 게스트 로그인 버튼
                enabled = !isLoading
            ) {
                Text("게스트 로그인")
            }
            Button(
                onClick = { authViewModel.signIn(email, password) },
                enabled = !isLoading,
            ) {
                Text("로그인")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 📝 회원가입 링크
        TextButton(onClick = onNavigateToRegister) {
            Text("계정이 없으신가요? 회원가입")
        }
    }
}