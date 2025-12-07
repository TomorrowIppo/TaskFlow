package com.ippo.taskflow.mvvm.view.group_view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ippo.taskflow.activity.ui.theme.LightGreyBackground
import com.ippo.taskflow.activity.ui.theme.PrimaryGreen
import com.ippo.taskflow.view_model.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
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
        if (!isProfileUpdating) {
            if (error != null) {
                snackbarHostState.showSnackbar("프로필 업데이트 실패: $error")
                authViewModel.clearError()
            } else if (profile != null) {
                if (nameState == profile!!.name.orEmpty() && statusMsgState == profile!!.statusMsg.orEmpty() && error == null) {
                    snackbarHostState.showSnackbar("프로필 정보가 저장되었습니다.")
                }
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
                title = { Text("Group Detail", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { SimpleBottomNavBar() }
    ) { padding ->

        if (currentUser == null) {
            LaunchedEffect(Unit) { onSignedOut() }
            return@Scaffold
        }

        if (profile == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // --- UI 콘텐츠 시작 ---
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group Detail Content (Placeholder - 이전 ProfileSetting UI 재사용)
            Text("Group Name: ${profile?.name.orEmpty()}", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Members: 5", style = MaterialTheme.typography.bodyLarge)

            // 4. 저장 / 취소 버튼 그룹 (예시)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                ActionButton(
                    text = "그룹 나가기",
                    icon = Icons.Default.Close,
                    onClick = { /* Handle leave group */ },
                    color = Color.Red,
                    contentColor = Color.White
                )
            }
        }
    }
}

// 사용자 정의 컴포넌트
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
            // 아이콘 Placeholder
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