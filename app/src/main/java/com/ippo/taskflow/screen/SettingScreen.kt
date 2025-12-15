package com.ippo.taskflow.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.R
import com.ippo.taskflow.auth.AuthViewModel

// ✅ Main/Login/Register와 동일 컬러 시스템
private val TaskFlowGreen = Color(0xFF1E8A3B)
private val TaskFlowLightGreen = Color(0xFFACFFC1)
private val ScreenBg = Color(0xFFFFFFFF)

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
    // ✅ 하단바 동작도 유지 가능하게 외부로 빼둠 (기존 TODO 대신 실제 네비게이션 연결 가능)
    onNavigateToMain: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
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
    onNavigateToMain: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
) {
    // ✅ 기존 기능(타이틀/항목/로그아웃)은 유지, UI만 통일
    val items = listOf(
        SettingItem("프로필설정", onNavigateToProfileSetting),
        SettingItem("개인/보안", onNavigateToSecurity),
        SettingItem("테마", onNavigateToTheme),
        SettingItem("지역", onNavigateToTheme),
        SettingItem("언어", onNavigateToTheme),
        SettingItem("알림", onNavigateToAbout),
        SettingItem("공지사항", onNavigateToAbout),
        SettingItem("이용약관", onNavigateToAbout),
        SettingItem("앱 버전", onNavigateToAbout),
        SettingItem("기타", onNavigateToEtc),
    )

    Scaffold(
        bottomBar = {
            // ✅ MainScreen과 동일 하단바 UI (PNG 아이콘)
            MainBottomNavBar(
                onHomeClick = onNavigateToMain,
                onGroupsClick = onNavigateToGroups,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
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
            ) {
                // ✅ Main/Login과 동일 헤더 패턴
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "설정",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))


                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 설정 리스트 카드 (Main의 TaskCard 톤)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 6.dp)
                    ) {
                        items(items) { item ->
                            SettingMenuItem(
                                title = item.title,
                                isDestructive = false,
                                enabled = !isLoading,
                                onClick = item.onClick
                            )
                            Divider()
                        }

                        // 🔴 로그아웃(기능 유지)
                        item {
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
    }
}

private data class SettingItem(
    val title: String,
    val onClick: () -> Unit
)

@Composable
private fun SettingMenuItem(
    title: String,
    isDestructive: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
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

/**
 * ✅ MainScreen과 동일한 하단 네비게이션 바 (PNG 아이콘)
 * - setting에서도 UI 통일을 위해 그대로 사용
 */
@Composable
private fun MainBottomNavBar(
    onHomeClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TaskFlowLightGreen)
                .padding(horizontal = 40.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onHomeClick) {
                Image(
                    painter = painterResource(R.drawable.ic_home),
                    contentDescription = "Home",
                    modifier = Modifier.size(50.dp)
                )
            }

            IconButton(onClick = onGroupsClick) {
                Image(
                    painter = painterResource(R.drawable.ic_taskflow),
                    contentDescription = "Groups",
                    modifier = Modifier.size(50.dp)
                )
            }

            IconButton(onClick = onProfileClick) {
                Image(
                    painter = painterResource(R.drawable.ic_profile),
                    contentDescription = "Profile",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
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