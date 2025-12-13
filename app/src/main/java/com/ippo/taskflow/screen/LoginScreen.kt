package com.ippo.taskflow.screen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.R
import com.ippo.taskflow.auth.AuthViewModel

// MainScreen과 동일 톤
private val TaskFlowGreen = Color(0xFF1E8A3B)
private val TaskFlowLightGreen = Color(0xFF60FF8A)
private val ScreenBg = Color(0xFF60FF8A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateBack: () -> Unit
) {
    LoginScreenInternal(
        authViewModel = authViewModel,
        onNavigateToMain = onNavigateToMain,
        onNavigateToRegister = onNavigateToRegister,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenInternal(
    authViewModel: AuthViewModel?,
    onNavigateToMain: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var keepLoggedIn by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val isLoading by (authViewModel?.isLoading?.collectAsState()
        ?: remember { mutableStateOf(false) })

    val emailError by (authViewModel?.emailError?.collectAsState()
        ?: remember { mutableStateOf<String?>(null) })

    val passwordError by (authViewModel?.passwordError?.collectAsState()
        ?: remember { mutableStateOf<String?>(null) })

    val isAuthenticated by (authViewModel?.isAuthenticated?.collectAsState()
        ?: remember { mutableStateOf(false) })

    val errorMessage by (authViewModel?.error?.collectAsState()
        ?: remember { mutableStateOf<String?>(null) })

    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) onNavigateToMain()
    }

    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrBlank()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            authViewModel?.clearError()
        }
    }

    Scaffold { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBg)
                .padding(innerPadding),
            color = ScreenBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp) // ✅ MainScreen과 동일 패딩
                    .verticalScroll(rememberScrollState())
            ) {
                // ✅ Main 스타일: 헤더 영역
                LoginHeader(onBack = onNavigateBack)

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 환영 카드(로고 + 문구)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ✅ 요청사항: "환영 텍스트 위"에 로고 넣기
                        Image(
                            painter = painterResource(id = R.drawable.ic_taskflow_logo),
                            contentDescription = "TaskFlow Logo",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "TaskFlow에 오신 걸 환영합니다!",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "계속하려면 로그인하세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4F4F4F),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ 입력 섹션 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ScreenBg),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .background(color = ScreenBg)
                    ) {
                        // Email
                        Text(
                            text = "Email",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your email") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "email"
                                )
                            },
                            isError = emailError != null,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                errorContainerColor = Color.White,

                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        if (!emailError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = emailError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password
                        Text(
                            text = "Password",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your password") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "password"
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            isError = passwordError != null,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                disabledContainerColor = Color.White,
                                errorContainerColor = Color.White,

                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                errorBorderColor = MaterialTheme.colorScheme.error
                            )
                        )
                        if (!passwordError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = passwordError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 옵션 Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = keepLoggedIn,
                                    onCheckedChange = { keepLoggedIn = it }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "로그인 상태 유지",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = "비밀번호를 잊으셨나요?",
                                style = MaterialTheme.typography.bodySmall,
                                color = TaskFlowGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 로그인 버튼
                Button(
                    onClick = {
                        authViewModel?.signIn(email = email, password = password)
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskFlowGreen,
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(if (isLoading) "로그인 중..." else "로그인")
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ✅ 회원가입 링크
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "계정이 없으신가요? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF555555)
                    )
                    TextButton(onClick = onNavigateToRegister) {
                        Text(
                            text = "회원가입하기",
                            style = MaterialTheme.typography.bodySmall,
                            color = TaskFlowGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun LoginHeader(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.Black
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "로그인",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    MaterialTheme {
        LoginScreenInternal(
            authViewModel = null,
            onNavigateToMain = {},
            onNavigateToRegister = {},
            onNavigateBack = {}
        )
    }
}