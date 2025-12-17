package com.ippo.taskflow.mvvm.view.main_view

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ippo.taskflow.activity.ui.theme.AccentBlue
import com.ippo.taskflow.activity.ui.theme.LightGreyBackground
import com.ippo.taskflow.activity.ui.theme.PrimaryGreen
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingScreen(
    authViewModel: AuthViewModel = viewModel(),
    onSignedOut: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val profile by authViewModel.profile.collectAsState()
    val isProfileUpdating by authViewModel.isProfileUpdating.collectAsState()
    val error by authViewModel.error.collectAsState()

    var nameState by remember { mutableStateOf("") }
    var statusMsgState by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // 프로필 데이터 로딩 시 상태 업데이트
    LaunchedEffect(profile) {
        profile?.let {
            nameState = it.name.orEmpty()
            statusMsgState = it.statusMsg.orEmpty()
        }
    }

    // 에러 및 저장 성공 시 Snackbar 표시
    LaunchedEffect(error, isProfileUpdating) {
        if (!isProfileUpdating && error != null) {
            snackbarHostState.showSnackbar("프로필 업데이트 실패: $error")
            authViewModel.clearError()
        } else if (!isProfileUpdating && error == null && profile != null) {
            if (nameState == profile!!.name.orEmpty() && statusMsgState == profile!!.statusMsg.orEmpty()) {
                snackbarHostState.showSnackbar("프로필 정보가 저장되었습니다.")
            }
        }
    }

    val isDataModified = remember(nameState, statusMsgState, profile) {
        if (profile == null) return@remember false
        nameState != (profile?.name.orEmpty()) || statusMsgState != (profile?.statusMsg.orEmpty())
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Setting", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
        //  [수정] bottomBar 제거: 전역 BottomNav(TaskFlowBottomNavBar)는 MainActivity에서만 관리
        // profileSetting route는 shouldShowBottomBar()에서 숨김 처리
    ) { padding ->

        // 로딩 및 Null 체크
        if (currentUser == null) {
            LaunchedEffect(Unit) { onSignedOut() }
            return@Scaffold
        }

        if (profile == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 프로필 이미지
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(AccentBlue)
            ) {
                Text("Image", color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            Spacer(modifier = Modifier.height(48.dp))

            // 별명 입력
            Text("별명", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            CustomTextField(
                value = nameState,
                onValueChange = { nameState = it },
                placeholder = "김연아",
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 상태 메시지 입력
            Text("상태메시지", modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            CustomTextField(
                value = statusMsgState,
                onValueChange = { statusMsgState = it },
                placeholder = "안녕!",
                minLines = 3,
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(32.dp))

            // 저장 / 취소 버튼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ActionButton(
                    text = "저장",
                    icon = Icons.Default.Check,
                    onClick = ::handleUpdateProfile,
                    color = PrimaryGreen,
                    enabled = isDataModified && !isProfileUpdating
                )
                Spacer(modifier = Modifier.width(16.dp))
                ActionButton(
                    text = "취소",
                    icon = Icons.Default.Close,
                    onClick = ::handleCancel,
                    color = Color(0xFFE0E0E0),
                    contentColor = Color.Black,
                    enabled = isDataModified
                )
            }
        }
    }
}

// 입력 필드
@Composable
private fun CustomTextField(
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
        shape = RoundedCornerShape(8.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = LightGreyBackground,
            unfocusedContainerColor = LightGreyBackground,
            disabledContainerColor = LightGreyBackground,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
        )
    )
}

// 저장/취소 버튼
@Composable
private fun ActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color,
    contentColor: Color = Color.White,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = contentColor
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = text, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text)
        }
    }
}

/**
 * (기존 코드 유지) 캡처본 스타일의 하단 네비게이션 바
 * - 현재 Screen에서는 사용하지 않음 (전역 바로 통합)
 */
@Composable
private fun SimpleBottomNavBar() {
    val NavBarColor = Color(0xFFB9F6CA)
    val NavItemColor = PrimaryGreen

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .background(NavBarColor),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(NavItemColor, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(NavItemColor, RoundedCornerShape(8.dp))
            )
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(NavItemColor, CircleShape)
            )
        }
    }
}