package com.ippo.taskflow.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.R
import com.ippo.taskflow.auth.AuthViewModel

// 🚨 PM Note: 주 색상 상수 정의
val TaskFlowGreen = Color(0xFF1E8A3B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateBack: () -> Unit,
    // onStartGoogleSignIn: () -> Unit // 소셜 로그인 액션 (현재 UI에서 제외)
) {
    // 1. 폼 상태 관리 및 ViewModel 상태 관찰 (생략 없이 유지)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val isLoading by authViewModel.isLoading.collectAsState()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val error by authViewModel.error.collectAsState()
    val emailError by authViewModel.emailError.collectAsState()
    val passwordError by authViewModel.passwordError.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 네비게이션 및 에러 처리 (LaunchedEffect 블록 유지)
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) { onNavigateToMain() }
    }
    LaunchedEffect(error) {
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            authViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE0FFDD)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0FFDD))
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 🟢 체크 아이콘 레이어 (중앙 정렬 확실히 적용)
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center // 🚨 Box 내부 자식들을 중앙에 정렬
            ) {
                // 배경 동그라미 (꽉 채움)
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_circle_cover),
                    contentDescription = "체크 배경",
                    modifier = Modifier.fillMaxSize(),
                    tint = TaskFlowGreen
                )
                // 흰색 체크 아이콘 (크기 유지 및 중앙 정렬 명시)
                Icon(
                    painter = painterResource(id = R.drawable.ic_check_circle),
                    contentDescription = "체크",
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.Center), // 🚨 아이콘도 명시적으로 중앙 정렬
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TaskFlow에 오신 걸 환영합니다!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "계속하려면 로그인하세요",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 📧 이메일 입력 필드 (기존 유지)
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                isError = emailError != null,
                leadingIcon = { Icon(Icons.Default.MailOutline, contentDescription = null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedBorderColor = TaskFlowGreen,
                    unfocusedBorderColor = Color.Gray
                )
            )
            if (emailError != null) {
                Text(emailError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 🔒 비밀번호 입력 필드 (기존 유지)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                isError = passwordError != null,
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "비밀번호 보기")
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    focusedBorderColor = TaskFlowGreen,
                    unfocusedBorderColor = Color.Gray
                )
            )
            if (passwordError != null) {
                Text(passwordError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.Start))
            }
            Spacer(modifier = Modifier.height(8.dp))

            // 💾 로그인 상태 유지 & 비밀번호 찾기 (기존 유지)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text("로그인 상태 유지", style = MaterialTheme.typography.bodySmall)
                }
                Text("비밀번호를 잊으셨나요?", style = MaterialTheme.typography.bodySmall, textDecoration = TextDecoration.Underline, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(24.dp))

            // 🔑 로그인 버튼 (기존 유지)
            Button(
                onClick = { authViewModel.signIn(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = TaskFlowGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("로그인", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // 🌐 소셜 로그인 버튼 (기존 유지)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SocialLoginButton(R.drawable.ic_google, onClick = { /* TODO Google */ })
                SocialLoginButton(R.drawable.ic_facebook, onClick = { /* TODO Facebook */ })
                SocialLoginButton(R.drawable.ic_apple, onClick = { /* TODO Apple */ })
            }
            Spacer(modifier = Modifier.height(32.dp))

            // 📝 회원가입 링크 (기존 유지)
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth().height(
                IntrinsicSize.Min)) {
                Text("계정이 없으신가요? ", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray, modifier = Modifier.align(Alignment.CenterVertically).padding(end = 0.dp))

                TextButton(
                    onClick = onNavigateToSignup,
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp),
                    modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp)
                ) {
                    Text("회원가입하기", fontWeight = FontWeight.Bold, color = TaskFlowGreen)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// 🌐 SocialLoginButton Composable (기존 유지)
@Composable
fun SocialLoginButton(iconResId: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(70.dp),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray)
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = "소셜 로그인",
            modifier = Modifier.size(70.dp), // 크기 유지
            tint = Color.Unspecified // 원본 색상 유지
        )
    }
}