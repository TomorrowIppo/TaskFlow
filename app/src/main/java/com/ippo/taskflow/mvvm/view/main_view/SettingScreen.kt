package com.ippo.taskflow.mvvm.view.main_view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel


// ✅ Main/Login/Register와 동일 톤
private val TaskFlowLightGreen = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToProfileSetting: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToEtc: () -> Unit,

    // ✅ 하단바 네비게이션(추가)
    onNavigateToMain: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    val isLoading by authViewModel.isLoading.collectAsState(initial = false)

    SettingScreenScaffold(
        isLoading = isLoading,
        onNavigateBack = onNavigateBack,
        onNavigateToProfileSetting = onNavigateToProfileSetting,
        onNavigateToSecurity = onNavigateToSecurity,
        onNavigateToTheme = onNavigateToTheme,
        onNavigateToAbout = onNavigateToAbout,
        onNavigateToEtc = onNavigateToEtc,

        // ✅ bottom bar actions
        onNavigateToMain = onNavigateToMain,
        onNavigateToGroups = onNavigateToGroups,
        onNavigateToProfile = onNavigateToProfile,

        onLogout = {
            if (!isLoading) {
                authViewModel.signOut()
                onNavigateToLogin()
            }
        }
    )
}

/**
 * ViewModel에 의존하지 않는 순수 UI Composable
 * 👉 Preview에서는 이걸 직접 호출한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingScreenScaffold(
    isLoading: Boolean,
    onNavigateBack: () -> Unit,
    onNavigateToProfileSetting: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToEtc: () -> Unit,

    // ✅ 하단바 네비게이션(추가)
    onNavigateToMain: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToProfile: () -> Unit,

    onLogout: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "설정",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                }
            )
        },
        bottomBar = {
            // ✅ 공통 하단 네비게이션바 적용
            TaskFlowBottomNavBar(
                onHomeClick = onNavigateToMain,
                onMainClick = onNavigateToGroups,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(TaskFlowLightGreen),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp
            ) {
                Column {
                    SettingMenuItem(title = "프로필설정", onClick = onNavigateToProfileSetting)
                    Divider()
                    SettingMenuItem(title = "개인/보안", onClick = onNavigateToSecurity)
                    Divider()
                    SettingMenuItem(title = "테마", onClick = onNavigateToTheme)
                    Divider()
                    SettingMenuItem(title = "지역", onClick = onNavigateToTheme)
                    Divider()
                    SettingMenuItem(title = "언어", onClick = onNavigateToTheme)
                    Divider()
                    SettingMenuItem(title = "알림", onClick = onNavigateToAbout)
                    Divider()
                    SettingMenuItem(title = "공지사항", onClick = onNavigateToAbout)
                    Divider()
                    SettingMenuItem(title = "이용약관", onClick = onNavigateToAbout)
                    Divider()
                    SettingMenuItem(title = "앱 버전", onClick = onNavigateToAbout)
                    Divider()
                    SettingMenuItem(title = "기타", onClick = onNavigateToEtc)
                    Divider()
                    SettingMenuItem(
                        title = if (isLoading) "처리 중..." else "로그아웃",
                        isDestructive = true,
                        enabled = !isLoading,
                        onClick = onLogout
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingMenuItem(
    title: String,
    isDestructive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = when {
                    !enabled -> Color.Gray
                    isDestructive -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) Color.Black else Color.Gray
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    MaterialTheme {
        SettingScreenScaffold(
            isLoading = false,
            onNavigateBack = {},
            onNavigateToProfileSetting = {},
            onNavigateToSecurity = {},
            onNavigateToTheme = {},
            onNavigateToAbout = {},
            onNavigateToEtc = {},
            onNavigateToMain = {},
            onNavigateToGroups = {},
            onNavigateToProfile = {},
            onLogout = {}
        )
    }
}