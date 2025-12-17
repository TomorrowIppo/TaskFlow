package com.ippo.taskflow.mvvm.view.main_view

import androidx.compose.foundation.Image
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.model.User
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val userProfile by authViewModel.profile.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val tasks by taskViewModel.taskList.collectAsState()

    val error by authViewModel.error.collectAsState()
    LaunchedEffect(error) {
        if (error != null) authViewModel.clearError()
    }

    LaunchedEffect(userProfile?.uid) {
        val uid = userProfile?.uid ?: return@LaunchedEffect
        taskViewModel.loadMyTasks(uid)
    }

    // ✅ 바텀바는 MainActivity(MainAppNavHost)의 Scaffold에서만 관리한다.
    Scaffold(
        topBar = { ProfileTopBar(onNavigateBack = onNavigateBack) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        ProfileContent(
            modifier = Modifier.padding(paddingValues),
            userProfile = userProfile,
            tasks = tasks,
            onEditProfileClick = onNavigateToSettings,
            isLoading = isLoading
        )
    }
}

// -------------------------------------------------------------
// TopBar
// -------------------------------------------------------------
@Composable
fun ProfileTopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "프로필",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// -------------------------------------------------------------
// Content
// -------------------------------------------------------------
@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    userProfile: User?,
    tasks: List<Task>,
    onEditProfileClick: () -> Unit,
    isLoading: Boolean
) {
    if (isLoading && userProfile == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
        return
    }

    val displayName = userProfile?.name ?: "Guest User"
    val statusMessage = userProfile?.statusMsg ?: "상태 메시지를 설정해주세요."

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // ProfileImage(imageResId = R.drawable.profile_placeholder, contentDescription = "프로필")

        Spacer(Modifier.height(16.dp))

        Text(
            text = displayName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(Modifier.height(4.dp))

        Text(
            text = statusMessage,
            fontSize = 16.sp,
            color = Color.Gray
        )

        Spacer(Modifier.height(20.dp))

        EditProfileButton(onClick = onEditProfileClick)

        Spacer(Modifier.height(24.dp))

        // ✅ 달력 (일자별 수행률 색상)
        ProfileTaskCalendar(
            tasks = tasks,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(48.dp))
    }
}

// -------------------------------------------------------------
// Components
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

@Composable
fun EditProfileButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Icon(
            Icons.Default.Edit,
            contentDescription = "프로필 편집",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("프로필 편집", color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}