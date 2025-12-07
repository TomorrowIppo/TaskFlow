package com.ippo.taskflow.mvvm.view.init_view

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.view_model.auth.AuthViewModel
import androidx.compose.material3.TopAppBar

// 🔹 명세서 준수용 외부 API
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

// 🔹 내부 구현 (프리뷰에서 authViewModel 없이 재사용 가능)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginScreenInternal(
    authViewModel: AuthViewModel?,
    onNavigateToMain: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // 로컬 입력 상태
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var keepLoggedIn by remember { mutableStateOf(false) }

    // ViewModel 상태
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

    // ✅ 로그인 성공 시 메인으로 자동 이동
    LaunchedEffect(isAuthenticated) {
        if (isAuthenticated) {
            onNavigateToMain()
        }
    }

    // ✅ 에러 발생 시 Toast + clearError()
    LaunchedEffect(errorMessage) {
        if (!errorMessage.isNullOrEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            authViewModel?.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("로그인") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE0FFDD)) // 🔹 명세서 배경색 #E0FFDD
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // 상단 아이콘 박스
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Color(0xFF1E8A3B)), // 🔹 TaskFlow Green
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "로그인 체크",
                        tint = Color.White,
                        modifier = Modifier.size(64.dp) // 🔹 64dp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 타이틀/서브타이틀
                Text(
                    text = "TaskFlow에 오신 걸 환영합니다!",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "계속하려면 로그인하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4F4F4F),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Email
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Email",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        placeholder = { Text("Enter your email") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "email"
                            )
                        },
                        isError = emailError != null
                    )
                    emailError?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Password
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        placeholder = { Text("Enter your password") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "password"
                            )
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "toggle visibility"
                            )
                        },
                        isError = passwordError != null
                    )
                    passwordError?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 로그인 상태 유지 / 비밀번호 찾기 (명세에 없지만 유지)
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
                        color = Color(0xFF1E8A3B) // 🔹 TaskFlow Green
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 로그인 버튼
                Button(
                    onClick = {
                        authViewModel?.signIn(
                            email = email,
                            password = password
                        )
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), // 🔹 명세: 56dp
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1E8A3B), // 🔹 TaskFlow Green
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

                // 소셜 로그인 (디자인 참고용)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SocialCircleButton(label = "G")
                    SocialCircleButton(label = "f")
                    SocialCircleButton(label = "")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 회원가입 링크
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
                            color = Color(0xFF1E8A3B) // 🔹 TaskFlow Green
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SocialCircleButton(
    label: String,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
            .size(48.dp) // 🔹 명세: 48dp 아이콘 영역
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = label, fontWeight = FontWeight.Bold)
        }
    }
}

// 🔹 프리뷰는 Internal을 사용해서 AuthViewModel 없이 돌린다.
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