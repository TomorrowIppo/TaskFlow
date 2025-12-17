package com.ippo.taskflow.mvvm.view.main_view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel

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
        onLogout = {
            if (!isLoading) {
                authViewModel.signOut()
            }
        }
    )
}

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
        }
        // [수정] bottomBar 제거: 전역 하단바(TaskFlowBottomNavBar)는 MainActivity에서 관리
        // SettingScreen은 shouldShowBottomBar()에서 숨김 대상(route == settings)로 처리 가능
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
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
                        title = "로그아웃",
                        isDestructive = true,
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
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = if (isDestructive)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "›",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * (기존 코드 유지) DailyTaskScreen 과 동일하게 사용할 하단 네비게이션 바 (Home / Group / Profile)
 * - 현재는 Screen 내부에서 호출되지 않음 (전역 바 사용)
 */
@Composable
private fun TaskFlowBottomNavBar() {
    Surface(
        color = Color(0xFF9CFFC4),
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 48.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                onClick = { }
            )
            BottomNavItem(
                icon = Icons.Default.Group,
                label = "Group",
                onClick = { }
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                onClick = { }
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
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
            onLogout = {}
        )
    }
}