package com.ippo.taskflow.mvvm.view.init_view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel

// 🚨 임시 색상 정의 (오류를 피하기 위함)
private val TaskFlowGreen = Color(0xFF9DE7B2)
private val PrimaryGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel?,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    // ⭐️ AuthViewModel 상태 관찰
    val isLoading by authViewModel?.isLoading?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }
    val error by authViewModel?.error?.collectAsState(initial = null) ?: remember { mutableStateOf(null) }

    // ⭐️ 회원가입 성공 상태 관찰 (네비게이션 전환용)
    val isRegistrationSuccessful by authViewModel?.isRegistrationSuccessful?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }

    // 로컬 입력 상태
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }

    // ⭐️ 로컬 유효성 검사 에러 상태
    var localError by remember { mutableStateOf<String?>(null) }

    // ⭐️ 버튼 활성화 조건
    val isFormValid = name.isNotBlank() && email.isNotBlank() &&
            password.isNotBlank() && passwordConfirm.isNotBlank()

    // 에러 상태 초기화 효과
    LaunchedEffect(name, email, password, passwordConfirm) {
        if (!error.isNullOrBlank()) {
            authViewModel?.clearError()
        }
        localError = null
    }

    // ⭐️ 회원가입 성공 시 네비게이션 처리
    LaunchedEffect(isRegistrationSuccessful) {
        if (isRegistrationSuccessful) {
            authViewModel?.clearRegistrationSuccess()
            // AuthNavHost에서 MainAppNavHost로 전환되므로, 현재 화면을 pop하는 것으로 충분
            onNavigateBack()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("회원가입") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFDF5FF))
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "회원가입",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "새로운 계정을 만들어보세요",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 이름
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("이름", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                                placeholder = { Text("이름을 입력하세요") },
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 이메일
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("이메일", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                                placeholder = { Text("이메일을 입력하세요") },
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 비밀번호
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("비밀번호", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                                placeholder = { Text("비밀번호를 입력하세요") },
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 비밀번호 확인
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("비밀번호 확인", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = passwordConfirm,
                                onValueChange = { passwordConfirm = it },
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)),
                                placeholder = { Text("비밀번호를 다시 입력하세요") },
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // ⭐️ 에러 메시지 표시 영역
                        if (!error.isNullOrBlank()) {
                            Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                        } else if (!localError.isNullOrBlank()) {
                            Text(text = localError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 이메일로 가입하기 버튼
                        Button(
                            onClick = {
                                localError = null
                                // 로컬 유효성 검사 (비밀번호 일치 확인)
                                if (password != passwordConfirm) {
                                    localError = "비밀번호와 비밀번호 확인이 일치하지 않습니다."
                                    return@Button
                                }

                                // ⭐️ [핵심 수정] ViewModel 시그니처에 맞춰 4개 인자 모두 전달
                                authViewModel?.registerUser(
                                    name = name,
                                    email = email,
                                    password = password,
                                    confirmPassword = passwordConfirm // 4번째 인자 추가
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            enabled = isFormValid && !isLoading,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TaskFlowGreen,
                                contentColor = Color.Black
                            )
                        ) {
                            // 로딩 상태 표시
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                            } else {
                                Text("이메일로 가입하기")
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 또는 구분선
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Divider(modifier = Modifier.weight(1f))
                            Text(text = "  또는  ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Divider(modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 소셜 로그인 버튼들
                        SocialLoginButton(text = "Google로 가입하기", onClick = { /* TODO */ })
                        Spacer(modifier = Modifier.height(10.dp))
                        SocialLoginButton(text = "Facebook으로 가입하기", onClick = { /* TODO */ })
                        Spacer(modifier = Modifier.height(10.dp))
                        SocialLoginButton(text = "Apple로 가입하기", onClick = { /* TODO */ })

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(text = "이미 계정이 있으신가요? ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(
                                text = "로그인하기",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = PrimaryGreen,
                                modifier = Modifier.clickable { onNavigateToLogin() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
    }
}


@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(
            authViewModel = null,
            onNavigateBack = {},
            onNavigateToLogin = {}
        )
    }
}