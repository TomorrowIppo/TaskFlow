package com.ippo.taskflow.mvvm.view.main_view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ippo.taskflow.activity.ui.theme.TaskFlowGreen
import com.ippo.taskflow.activity.ui.theme.TaskFlowLightGreen
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.model.TaskStatus
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToEditTask: (String) -> Unit, // ✅ 추가: taskId 받아 EditTaskScreen으로
) {
    // ----- ViewModel State Observe -----
    val firebaseUser by authViewModel.currentUser.collectAsState()
    val profile by authViewModel.profile.collectAsState()

    val taskList by taskViewModel.taskList.collectAsState()
    val isLoadingTasks by taskViewModel.isLoading.collectAsState()
    val taskError by taskViewModel.error.collectAsState()

    // ⭐️ ViewModel에서 계산된 완료율
    val completionPercentage by taskViewModel.completionPercentage.collectAsState()

    // 이름: User 프로필 → FirebaseUser.displayName 순으로 우선 사용
    val userName = when {
        !profile?.name.isNullOrBlank() -> profile!!.name
        !firebaseUser?.displayName.isNullOrBlank() -> firebaseUser!!.displayName!!
        else -> "사용자"
    }

    // 사진 URL (없으면 null → 기본 아바타 렌더링)
    val photoUrl = firebaseUser?.photoUrl?.toString()

    Scaffold(
        bottomBar = {
            MainBottomNavBar(
                onHomeClick = { /* 현재 화면이므로 아무 동작 없음 */ },
                onGroupsClick = onNavigateToGroups,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 상단 헤더
                MainHeader(
                    userName = userName,
                    photoUrl = photoUrl,
                    onSettingsClick = onNavigateToSettings
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 진행률 대시보드
                ProgressDashboard(
                    completionPercentage = completionPercentage
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Task 목록 제목
                Text(
                    text = "오늘의 Task",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Task 목록 리스트
                TaskListSection(
                    tasks = taskList,
                    isLoading = isLoadingTasks,
                    errorMessage = taskError,
                    onTaskClick = { taskId ->
                        onNavigateToEditTask(taskId) // ✅ 카드 클릭 → Edit 이동
                    },
                    onTaskStatusToggle = { task ->
                        val newStatus =
                            if (task.status == TaskStatus.DONE) TaskStatus.TODO.name else TaskStatus.DONE.name
                        taskViewModel.updateTaskStatus(task.taskId, newStatus)
                    }
                )
            }
        }
    }
}

@Composable
private fun MainHeader(
    userName: String,
    photoUrl: String?,
    onSettingsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 프로필 사진 / 기본 아바타
            if (photoUrl != null) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TaskFlowGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.toString() ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "안녕!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { /* 알림 기능은 추후 구현 */ }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "알림",
                    tint = Color.Black
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "설정",
                    tint = Color.Black
                )
            }
        }
    }
}

@Composable
private fun ProgressDashboard(
    completionPercentage: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TaskFlowGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = "오늘의 Task가 거의\n완료됐어요!",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "TaskFlow",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = TaskFlowGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier.size(90.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = completionPercentage / 100f,
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    strokeWidth = 8.dp,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Text(
                    text = "$completionPercentage%",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun TaskListSection(
    tasks: List<Task>,
    isLoading: Boolean,
    errorMessage: String?,
    onTaskClick: (String) -> Unit,     // ✅ 추가
    onTaskStatusToggle: (Task) -> Unit,
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "로딩 중...", color = Color.Gray)
        }
        return
    }

    if (!errorMessage.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = errorMessage, color = Color.Red, fontSize = 12.sp)
        }
    }

    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "오늘 등록된 Task가 없습니다.", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(tasks, key = { it.taskId }) { task ->
            TaskCard(
                task = task,
                onClick = { onTaskClick(task.taskId) },      // ✅ 카드 클릭 → Edit
                onToggleStatus = { onTaskStatusToggle(task) }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    onClick: () -> Unit,        // ✅ 추가
    onToggleStatus: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },   // ✅ 카드 전체 클릭
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(TaskFlowLightGreen),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📌", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title.ifBlank { "제목 없음" },
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val dueText = formatDueDate(task.dueDate)
                val priorityText = "우선순위 ${task.priority}"

                Text(
                    text = listOfNotNull(dueText, priorityText).joinToString(" • "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onToggleStatus) {
                val isDone = task.status == TaskStatus.DONE
                Icon(
                    imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                    contentDescription = if (isDone) "완료" else "미완료",
                    tint = if (isDone) TaskFlowGreen else Color.LightGray
                )
            }
        }
    }
}

private fun formatDueDate(date: Date?): String? {
    if (date == null) return null
    val formatter = SimpleDateFormat("a hh:mm", Locale.getDefault())
    return formatter.format(date)
}

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
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = TaskFlowGreen
                )
            }
            IconButton(onClick = onGroupsClick) {
                Icon(
                    imageVector = Icons.Filled.Group,
                    contentDescription = "Groups",
                    tint = Color.Black
                )
            }
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = Color.Black
                )
            }
        }
    }
}
