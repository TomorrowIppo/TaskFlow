package com.ippo.taskflow.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.R
import com.ippo.taskflow.activity.ui.theme.TaskFlowTheme
import com.ippo.taskflow.auth.AuthViewModel
import com.ippo.taskflow.data.User

// ✅ Main/Login/Register와 동일 컬러 시스템
private val TaskFlowGreen = Color(0xFF41CC67)
private val TaskFlowLightGreen = Color(0xFF60FF8A)
private val ScreenBg = Color(0xFFFFFFFF)

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    // ✅ 하단바 네비게이션 연결(기능 유지/확장)
    onNavigateToMain: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    val userProfile by authViewModel.profile.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val error by authViewModel.error.collectAsState()
    LaunchedEffect(error) {
        if (error != null) authViewModel.clearError()
    }

    Scaffold(
        bottomBar = {
            MainBottomNavBar(
                onHomeClick = onNavigateToMain,
                onGroupsClick = onNavigateToGroups,
                onProfileClick = onNavigateToProfile
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        ProfileContent(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ScreenBg)
                .padding(horizontal = 16.dp, vertical = 8.dp), // ✅ Main과 동일 패딩
            userProfile = userProfile,
            onNavigateBack = onNavigateBack,
            onEditProfileClick = onNavigateToSettings,
            isLoading = isLoading
        )
    }
}

// -------------------------------------------------------------
// 프로필 내용 영역 (기능 유지 + UI 통일)
// -------------------------------------------------------------
@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    userProfile: User?,
    onNavigateBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    isLoading: Boolean
) {
    Column(modifier = modifier) {
        // ✅ Main/Login/Register와 동일 헤더 패턴
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
                text = "프로필",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 로딩 처리 (기존 기능 유지)
        if (isLoading && userProfile == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        val displayName = userProfile?.name ?: "Guest User"
        val statusMessage = userProfile?.statusMsg ?: "상태 메시지를 설정해주세요."

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ 프로필 상세 카드 (Main의 리스트 카드 톤)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 프로필 이미지(기존 컴포넌트 유지 - placeholder는 프로젝트에 있을 때만)
                // 없으면 아래 기본 아바타 박스만 써도 됨.
                // ProfileImage(imageResId = R.drawable.profile_placeholder, contentDescription = "사용자 프로필 사진")

                // ✅ 리소스 없는 상태에서도 깨지지 않게 기본 아바타 제공
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(TaskFlowGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.firstOrNull()?.toString() ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 편집 버튼 (기능 유지) - Main/Login 버튼 톤
                Button(
                    onClick = onEditProfileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskFlowGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "프로필 편집",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "프로필 편집",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 기존 하위 컴포넌트 (유지 가능)
// -------------------------------------------------------------
@Composable
fun ProfileImage(imageResId: Int, contentDescription: String) {
    val borderModifier = Modifier
        .size(160.dp)
        .clip(CircleShape)
        .border(
            width = 3.dp,
            color = Color.LightGray.copy(alpha = 0.5f),
            shape = CircleShape
        )

    Box(modifier = borderModifier) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * ✅ MainScreen과 동일 하단 네비게이션 바 (PNG 아이콘)
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

// -------------------------------------------------------------
// Preview (기존 요구: 먼저 프리뷰 보고 싶음)
// -------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview_Full() {
    TaskFlowTheme {
        Scaffold(
            bottomBar = {
                MainBottomNavBar(
                    onHomeClick = {},
                    onGroupsClick = {},
                    onProfileClick = {}
                )
            }
        ) { padding ->
            ProfileContent(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(ScreenBg)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                userProfile = User(
                    uid = "preview_uid",
                    name = "희주",
                    statusMsg = "오늘도 TaskFlow로 정리하는 중"
                ),
                onNavigateBack = {},
                onEditProfileClick = {},
                isLoading = false
            )
        }
    }
}