package com.ippo.taskflow.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

// ✅ MainScreen과 동일 톤(팀 공통 컬러로 맞춤)
private val TaskFlowGreen = Color(0xFF1E8A3B)
private val TaskFlowLightGreen = Color(0xFFACFFC1)
private val ScreenBg = Color(0xFFFFFFFF)

// 기존 캡처 기반 accent는 유지 가능
private val AccentBlue = Color(0xFF00B0FF)
private val LightGreyBackground = Color(0xFFF0F0F0)

@Composable
fun ProfileSettingScreen(
    authViewModel: AuthViewModel,
    onSignedOut: () -> Unit,
    onNavigateBack: () -> Unit,
    // ✅ Main과 동일 하단바 네비게이션 연결
    onNavigateToMain: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val profile by authViewModel.profile.collectAsState()
    val isProfileUpdating by authViewModel.isProfileUpdating.collectAsState()
    val error by authViewModel.error.collectAsState()

    var nameState by remember { mutableStateOf("") }
    var statusMsgState by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }

    // 프로필 로딩 -> 입력 상태 세팅
    LaunchedEffect(profile) {
        profile?.let {
            nameState = it.name.orEmpty()
            statusMsgState = it.statusMsg.orEmpty()
        }
    }

    // 에러 처리
    LaunchedEffect(error, isProfileUpdating) {
        if (!isProfileUpdating && error != null) {
            snackbarHostState.showSnackbar("프로필 업데이트 실패: $error")
            authViewModel.clearError()
        }
    }

    val isDataModified = remember(nameState, statusMsgState, profile) {
        if (profile == null) return@remember false
        nameState != profile?.name.orEmpty() || statusMsgState != profile?.statusMsg.orEmpty()
    }

    fun handleUpdateProfile() {
        if (isDataModified && !isProfileUpdating) {
            authViewModel.updateProfile(
                name = nameState,
                statusMessage = statusMsgState
            )
        }
    }

    fun handleCancel() {
        profile?.let {
            nameState = it.name.orEmpty()
            statusMsgState = it.statusMsg.orEmpty()
        }
    }

    ProfileSettingScreenScaffold(
        snackbarHostState = snackbarHostState,
        currentUserExists = currentUser != null,
        profileLoaded = profile != null,
        isProfileUpdating = isProfileUpdating,
        isDataModified = isDataModified,
        nameState = nameState,
        statusMsgState = statusMsgState,
        onNameChange = { nameState = it },
        onStatusChange = { statusMsgState = it },
        onSignedOut = onSignedOut,
        onNavigateBack = onNavigateBack,
        onSave = ::handleUpdateProfile,
        onCancel = ::handleCancel,
        onNavigateToMain = onNavigateToMain,
        onNavigateToGroups = onNavigateToGroups,
        onNavigateToProfile = onNavigateToProfile
    )
}

/**
 * ✅ ViewModel 의존 없는 순수 UI 레이어
 */
@Composable
private fun ProfileSettingScreenScaffold(
    snackbarHostState: SnackbarHostState,
    currentUserExists: Boolean,
    profileLoaded: Boolean,
    isProfileUpdating: Boolean,
    isDataModified: Boolean,
    nameState: String,
    statusMsgState: String,
    onNameChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onSignedOut: () -> Unit,
    onNavigateBack: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MainBottomNavBar(
                onHomeClick = onNavigateToMain,
                onGroupsClick = onNavigateToGroups,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->

        // ✅ 로그인 세션 없으면 탈출
        if (!currentUserExists) {
            LaunchedEffect(Unit) { onSignedOut() }
            return@Scaffold
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ScreenBg),
            color = ScreenBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp) // ✅ Main과 동일
            ) {
                // ✅ Main/Login과 동일 헤더 패턴
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "프로필 설정",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 프로필 로딩 중
                if (!profileLoaded) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    return@Surface
                }

                // ✅ 카드 톤: Main TaskCard와 동일
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
                        // 프로필 이미지 placeholder
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(AccentBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Image", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // 별명
                        Text("별명", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        CustomTextFieldMainTone(
                            value = nameState,
                            onValueChange = onNameChange,
                            placeholder = "김연아",
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 상태메시지
                        Text("상태메시지", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        CustomTextFieldMainTone(
                            value = statusMsgState,
                            onValueChange = onStatusChange,
                            placeholder = "안녕!",
                            minLines = 3,
                            maxLines = 5
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // 저장/취소
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            ActionButton(
                                text = if (isProfileUpdating) "저장 중..." else "저장",
                                icon = Icons.Default.Check,
                                onClick = onSave,
                                color = TaskFlowGreen,
                                enabled = isDataModified && !isProfileUpdating
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            ActionButton(
                                text = "취소",
                                icon = Icons.Default.Close,
                                onClick = onCancel,
                                color = Color(0xFFE0E0E0),
                                contentColor = Color.Black,
                                enabled = isDataModified && !isProfileUpdating
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ✅ Main 톤 TextField (화이트 컨테이너 + 라운드 16dp)
 */
@Composable
private fun CustomTextFieldMainTone(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        singleLine = maxLines == 1,
        minLines = minLines,
        maxLines = maxLines,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,

            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Composable
private fun ActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    color: Color,
    contentColor: Color = Color.White,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = TaskFlowLightGreen,
            contentColor = Color.Black
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text)
        }
    }
}

/**
 * ✅ MainScreen과 동일 하단바 (PNG 아이콘)
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
private fun ProfileSettingScreenPreview() {
    MaterialTheme {
        ProfileSettingScreenScaffold(
            snackbarHostState = remember { SnackbarHostState() },
            currentUserExists = true,
            profileLoaded = true,
            isProfileUpdating = false,
            isDataModified = true,
            nameState = "김연아",
            statusMsgState = "안녕!",
            onNameChange = {},
            onStatusChange = {},
            onSignedOut = {},
            onNavigateBack = {},
            onSave = {},
            onCancel = {},
            onNavigateToMain = {},
            onNavigateToGroups = {},
            onNavigateToProfile = {}
        )
    }
}