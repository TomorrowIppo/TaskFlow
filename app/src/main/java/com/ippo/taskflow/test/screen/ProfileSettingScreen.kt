package com.ippo.taskflow.test.screen

import androidx.compose.foundation.layout.*
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
    // 1. 상태 로딩 및 임시 저장 상태
    val currentProfile by authViewModel.profile.collectAsState()

    // UI에 보여줄 사용자 이름 (수정 가능한 상태)
    var displayName by remember { mutableStateOf("") }
    // UI에 보여줄 상태 메시지 (수정 가능한 상태)
    var statusMessage by remember { mutableStateOf("") }

    // 로딩 상태 및 에러 메시지
    // ⚠️ ViewModel의 _isLoading 대신 프로필 업데이트 전용 _isProfileUpdating을 사용합니다.
    val isUpdating by authViewModel.isProfileUpdating.collectAsState()
    val error by authViewModel.error.collectAsState()

    // 2. 현재 프로필이 로드되면 TextField의 초기값을 설정합니다.
    LaunchedEffect(currentProfile) {
        currentProfile?.let { profile ->
            // 현재 User 모델의 필드가 'name'이므로, 'displayName' 대신 'name'을 사용해야 합니다.
            displayName = profile.name ?: ""
            statusMessage = profile.statusMsg ?: ""
        }
    }

    // 상태 메시지 업데이트 성공 시 피드백 제공
    val snackbarHostState = remember { SnackbarHostState() }

    // 3. 사용자 이름과 상태 메시지 모두 업데이트하는 함수 (ViewModel의 updateProfile 호출)
    fun updateProfile() {
        val trimmedName = displayName.trim()
        val trimmedMsg = statusMessage.trim()

        // ⭐️ 통합된 ViewModel 함수 호출
        authViewModel.updateProfile(trimmedName, trimmedMsg)
    }

    // 4. Firestore 업데이트 성공 또는 실패 시 스낵바 표시
    LaunchedEffect(isUpdating, error) {
        if (!isUpdating) {
            if (error != null) {
                snackbarHostState.showSnackbar("저장 실패: $error")
                authViewModel.clearError()
            } else if (currentProfile?.name == displayName && currentProfile?.statusMsg == statusMessage) {
                // 로딩이 끝났고, UI 상태와 현재 프로필 상태가 일치하는 경우 (성공)
                // 이 로직은 `currentProfile`이 업데이트된 후에만 실행되어야 하므로,
                // `updateProfile`이 성공적으로 완료되었을 때만 메시지를 표시하도록 보장해야 합니다.
                // (일반적으로 ViewModel 내부에서 성공 시 이벤트를 보내는 것이 더 안전함)
                // 여기서는 간단하게 업데이트가 끝났고 에러가 없으면 성공으로 간주합니다.
                if (currentProfile != null) {
                    snackbarHostState.showSnackbar("프로필이 성공적으로 저장되었습니다.")
                }
            }
        }
    }

    // 5. 버튼 활성화 조건
    val isDataChanged = remember(displayName, statusMessage, currentProfile) {
        // ViewModel의 User 모델 필드명인 'name'을 사용합니다.
        val currentName = currentProfile?.name?.trim() ?: ""
        val currentMsg = currentProfile?.statusMsg?.trim() ?: ""
        val newName = displayName.trim()
        val newMsg = statusMessage.trim()

        // 현재 값과 UI의 값이 하나라도 다르면 true
        newName != currentName || newMsg != currentMsg
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
                "프로필 정보 설정",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 사용자 이름 입력 필드
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("사용자 이름") },
                placeholder = { Text("새로운 이름을 입력해주세요 (필수)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 상태 메시지 입력 필드
            OutlinedTextField(
                value = statusMessage,
                onValueChange = { statusMessage = it },
                label = { Text("상태 메시지") },
                placeholder = { Text("현재 상태를 입력해주세요 (선택)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 6. 저장 버튼 (로직 통합)
            Button(
                onClick = ::updateProfile,
                // 로딩 중이 아니고 (isUpdating) 데이터가 변경되었으며 (isDataChanged) 이름이 비어있지 않은 경우 활성화
                enabled = !isUpdating && isDataChanged && displayName.trim().isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isUpdating) { // ⭐️ isUpdating 사용
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("프로필 저장")
                }
            }
        }
    }
}