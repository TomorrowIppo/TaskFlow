package com.ippo.taskflow.mvvm.view.init_view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel

private val ScreenBg = Color(0xFFACFFC1)
private val TaskFlowGreen = Color(0xFF1E8A3B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    RegisterScreenInternal(
        authViewModel = authViewModel,
        onNavigateBack = onNavigateBack,
        onNavigateToLogin = onNavigateToLogin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterScreenInternal(
    authViewModel: AuthViewModel?,
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
) {
    val context = LocalContext.current

    // 입력 상태
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    // VM 상태
    val isLoading by (authViewModel?.isLoading?.collectAsState()
        ?: remember { mutableStateOf(false) })

    val errorMessage by (authViewModel?.error?.collectAsState()
        ?: remember { mutableStateOf<String?>(null) })

    val isRegistrationSuccessful by (authViewModel?.isRegistrationSuccessful?.collectAsState()
        ?: remember { mutableStateOf(false) })

    val isFormValid = name.isNotBlank() && email.isNotBlank() &&
            password.isNotBlank() && passwordConfirm.isNotBlank()

    // 입력 변경 시 에러 초기화
    LaunchedEffect(name, email, password, passwordConfirm) {
        if (!errorMessage.isNullOrBlank()) authViewModel?.clearError()
        localError = null
    }

    // 회원가입 성공 처리
    LaunchedEffect(isRegistrationSuccessful) {
        if (isRegistrationSuccessful) {
            authViewModel?.clearRegistrationSuccess()
            onNavigateBack()
        }
    }

    // 에러 토스트(로그인과 톤 맞춤)
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
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                RegisterHeader(onBack = onNavigateBack)

                Spacer(modifier = Modifier.height(16.dp))

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
                        Image(
                            painter = painterResource(id = R.drawable.ic_taskflow_logo),
                            contentDescription = "TaskFlow Logo",
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(20.dp))
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "회원가입",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "새로운 계정을 만들어보세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF4F4F4F),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                            .background(ScreenBg)
                    ) {
                        // 이름
                        Text("이름", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("이름을 입력하세요") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "name") },
                            shape = RoundedCornerShape(16.dp),
                            colors = tfFieldColors()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 이메일
                        Text("Email", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your email") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "email") },
                            shape = RoundedCornerShape(16.dp),
                            colors = tfFieldColors()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 비밀번호
                        Text("Password", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your password") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "password") },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "toggle password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            colors = tfFieldColors()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 비밀번호 확인
                        Text("Password Confirm", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = passwordConfirm,
                            onValueChange = { passwordConfirm = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Re-enter your password") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "confirm password") },
                            trailingIcon = {
                                IconButton(onClick = { confirmVisible = !confirmVisible }) {
                                    Icon(
                                        imageVector = if (confirmVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "toggle confirm password"
                                    )
                                }
                            },
                            visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            shape = RoundedCornerShape(16.dp),
                            colors = tfFieldColors()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                //로컬 에러만 아래에 표시
                if (!localError.isNullOrBlank()) {
                    Text(
                        text = localError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // “이메일로 가입하기” 버튼
                Button(
                    onClick = {
                        localError = null
                        if (password != passwordConfirm) {
                            localError = "비밀번호와 비밀번호 확인이 일치하지 않습니다."
                            return@Button
                        }
                        authViewModel?.registerUser(
                            name = name,
                            email = email,
                            password = password,
                            confirmPassword = passwordConfirm
                        )
                    },
                    enabled = isFormValid && !isLoading,
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
                        Text("가입 중...")
                    } else {
                        Text("이메일로 가입하기")
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color(0x33000000))
                    Text("  또는  ", style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
                    Divider(modifier = Modifier.weight(1f), color = Color(0x33000000))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 소셜 가입
                SocialJoinButton(
                    iconRes = R.drawable.ic_google,
                    text = "Google로 가입하기",
                    onClick = { /* TODO */ }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SocialJoinButton(
                    iconRes = R.drawable.ic_facebook,
                    text = "Facebook으로 가입하기",
                    onClick = { /* TODO */ }
                )
                Spacer(modifier = Modifier.height(10.dp))
                SocialJoinButton(
                    iconRes = R.drawable.ic_apple,
                    text = "Apple로 가입하기",
                    onClick = { /* TODO */ }
                )

                Spacer(modifier = Modifier.height(22.dp))

                // 하단 링크
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "이미 계정이 있으신가요? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF555555)
                    )
                    Text(
                        text = "로그인하기",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = TaskFlowGreen,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RegisterHeader(onBack: () -> Unit) {
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
            text = "회원가입",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.Black
        )
    }
}

@Composable
private fun SocialJoinButton(
    iconRes: Int,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = text,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun tfFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    disabledContainerColor = Color.White,
    errorContainerColor = Color.White,
    focusedBorderColor = Color.Black,
    unfocusedBorderColor = Color.Black,
    errorBorderColor = MaterialTheme.colorScheme.error
)

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreenInternal(
            authViewModel = null,
            onNavigateBack = {},
            onNavigateToLogin = {}
        )
    }
}