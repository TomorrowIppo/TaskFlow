package com.ippo.taskflow.test.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingScreen(
    authViewModel: AuthViewModel,
    onNavigateUp: () -> Unit
) {
    // 1. 상태 메시지 로딩 및 임시 저장 상태
    // Firestore에서 불러온 현재 상태메시지를 초기값으로 사용
    val currentProfile by authViewModel.profile.collectAsState()

    // UI에 보여줄 상태 메시지 (수정 가능한 상태)
    var statusMessage by remember { mutableStateOf("") }

    // 로딩 상태 및 에러 메시지
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()

    // 현재 프로필이 로드되면 TextField의 초기값을 설정합니다.
    LaunchedEffect(currentProfile) {
        currentProfile?.statusMsg?.let {
            statusMessage = it
        }
    }

    // 상태 메시지 업데이트 성공 시 피드백 제공
    val snackbarHostState = remember { SnackbarHostState() }

    // 2. 상태 메시지 업데이트 함수
    fun updateStatusMessage() {
        // null 또는 빈 문자열을 허용하지 않는다고 가정하고, trimming 처리
        val trimmedMsg = statusMessage.trim()

        // AuthViewModel에 상태 업데이트 요청
        authViewModel.updateStatusMessage(trimmedMsg)
    }

    // 3. Firestore 업데이트 성공 또는 실패 시 스낵바 표시
    LaunchedEffect(isLoading, error) {
        if (!isLoading) {
            if (error != null) {
                snackbarHostState.showSnackbar("저장 실패: $error")
                // 실패 시 에러 상태 초기화
                authViewModel.clearError()
            } else if (currentProfile?.statusMsg == statusMessage && statusMessage.isNotEmpty()) {
                // 로딩이 끝났고, 상태 메시지가 UI 상태와 일치하며 비어있지 않은 경우 (성공)
                snackbarHostState.showSnackbar("상태 메시지가 성공적으로 저장되었습니다.")
            }
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("프로필 설정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "상태 메시지 설정",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 4. 상태 메시지 입력 필드
            // 'KeyboardOptions' 섹션을 완전히 제거하고 singleLine = true로 변경
            OutlinedTextField(
                value = statusMessage,
                onValueChange = { statusMessage = it },
                label = { Text("상태 메시지") },
                placeholder = { Text("현재 상태를 입력해주세요 (최대 50자)") },
                // ⭐️ singleLine을 true로 설정 (핵심 변경)
                singleLine = true,
                // maxLines 설정은 singleLine=true일 때 무시됩니다.
                // maxLines = 3,

                // ❌ KeyboardOptions 블록을 제거함
                // keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions( ... ),

                modifier = Modifier
                    .fillMaxWidth()
                    // ⭐️ 높이 제약이 불필요하거나 단순화됩니다.
                    .heightIn(min = 100.dp, max = 150.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. 저장 버튼
            Button(
                onClick = ::updateStatusMessage,
                enabled = !isLoading && (statusMessage.trim() != (currentProfile?.statusMsg ?: "").trim()),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("상태 저장")
                }
            }
        }
    }
}