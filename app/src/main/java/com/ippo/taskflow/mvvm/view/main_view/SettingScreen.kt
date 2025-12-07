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

// 💡 MVVM 표준: ViewModel + 네비게이션 액션만 인자로 받는다.
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
    // 1. ViewModel의 상태를 관찰한다.
    val isLoading by authViewModel.isLoading.collectAsState(initial = false)

    // 2. ViewModel → 순수 UI 레이어로 브릿지
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
            TaskFlowBottomNavBar()
        }
    ) { innerPadding ->
        // 실제 Setting UI
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)), // Figma 배경 느낌
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Figma 기준 카드 컨테이너
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
                    // 🔴 로그아웃
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

/**
 * 단일 설정 항목 Composable
 * - Figma 리스트 아이템 느낌으로 패딩/폰트 맞춤
 */
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
        // ▶ 우측 화살표(Placeholder) — 나중에 아이콘 리소스로 교체 가능
        Text(
            text = "›",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * DailyTaskScreen 과 동일하게 사용할 하단 네비게이션 바 (Home / Group / Profile)
 * - 실제 네비게이션 람다는 NavHost 쪽에서 교체 예정.
 */
@Composable
private fun TaskFlowBottomNavBar() {
    Surface(
        color = Color(0xFF9CFFC4), // Figma 하단 그린 톤과 비슷하게
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
                onClick = { /* TODO: Home 이동 */ }
            )
            BottomNavItem(
                icon = Icons.Default.Group,
                label = "Group",
                onClick = { /* TODO: Group 이동 */ }
            )
            BottomNavItem(
                icon = Icons.Default.Person,
                label = "Profile",
                onClick = { /* TODO: Profile 이동 */ }
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

/**
 * 🖼 Preview용 Composable
 * - ViewModel 없이 순수 UI만 확인
 */
@Preview(showBackground = true)
@Composable
private fun SettingScreenPreview() {
    MaterialTheme { // 프로젝트 테마가 있으면 여기서 감싸도 됨 (ex. TaskFlowTheme)
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
