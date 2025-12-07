package com.ippo.taskflow.screen.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.view_model.auth.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit // 회원가입 성공 시 호출
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    val isLoading by authViewModel.isLoading.collectAsState()
    val isSuccess by authViewModel.isRegistrationSuccessful.collectAsState()
    val emailError by authViewModel.emailError.collectAsState()
    // ... 나머지 error states

    // 🚨 회원가입 성공 시 LoginScreen으로 이동
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            authViewModel.clearRegistrationSuccess()
            onNavigateToLogin()
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("회원가입", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름") }, isError = authViewModel.nameError.collectAsState().value != null)
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("이메일") }, isError = emailError != null)
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("비밀번호") }, isError = authViewModel.passwordError.collectAsState().value != null)
        OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("비밀번호 확인") }, isError = authViewModel.confirmPasswordError.collectAsState().value != null)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { authViewModel.registerUser(name, email, password, confirmPassword) },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("계정 생성")
        }

        // ... 오류 메시지 표시 영역 추가 ...
    }
}