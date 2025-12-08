package com.ippo.taskflow.test.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    onNavigateUp: () -> Unit,
    onNavigateToSettings: () -> Unit, // 여기서 Settings로 이동 요청
    onLogout: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val profile by authViewModel.profile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프로필") },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) { // ProfileSettingScreen으로 연결
                        Icon(Icons.Filled.Settings, contentDescription = "설정")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start
        ) {

            // --- 1. 사용자 기본 정보 (Firebase Auth) ---
            Text(
                "사용자 계정 정보",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            ProfileInfoRow(
                label = "이름",
                value = profile?.name ?: currentUser?.displayName ?: "Guest"
            )
            Spacer(modifier = Modifier.height(8.dp))

            ProfileInfoRow(
                label = "이메일",
                value = currentUser?.email ?: "Guest"
            )
            Spacer(modifier = Modifier.height(8.dp))

            ProfileInfoRow(
                label = "UID",
                value = currentUser?.uid?.let { "${it.take(10)}..." } ?: "N/A",
                valueStyle = MaterialTheme.typography.bodySmall
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- 2. 개인 프로필 (Firestore User Model) ---
            Text(
                "개인 프로필",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (profile != null) {

                // 상태 메시지
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("상태 메시지:", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            text = profile!!.statusMsg?.ifBlank { "상태 메시지가 설정되지 않았습니다." } ?: "상태 메시지가 설정되지 않았습니다.",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                Text(
                    "프로필 정보를 로드할 수 없습니다. (익명 사용자 혹은 DB 오류)",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 3. 로그아웃 버튼
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("로그아웃")
            }
        }
    }
}

/**
 * 프로필 정보 항목을 표시하는 재사용 가능한 Composable
 */
@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    valueStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontWeight = FontWeight.SemiBold)
        Text(value, style = valueStyle)
    }
}