package com.ippo.taskflow.mvvm.view.main_view

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.R
import com.ippo.taskflow.activity.ui.theme.TaskFlowGreen
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.model.TaskStatus
import com.ippo.taskflow.mvvm.model.User
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    groupViewModel: GroupViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val userProfile by authViewModel.profile.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    // 프로필 달력/팝업용 전체 Task
    val tasks by taskViewModel.profileTasks.collectAsState()

    val groups by groupViewModel.groupList.collectAsState()

    val error by authViewModel.error.collectAsState()
    LaunchedEffect(error) {
        if (error != null) authViewModel.clearError()
    }

    // 프로필 진입 시: 전체 Task + 내 그룹 목록 로드
    LaunchedEffect(userProfile?.uid) {
        val uid = userProfile?.uid ?: return@LaunchedEffect
        taskViewModel.loadMyAssignedAllTasks(uid)
        groupViewModel.loadGroups()
    }

    // groupId -> groupName 맵
    val groupNameMap = remember(groups) {
        groups.associate { it.groupId to it.name }
    }

    Scaffold(
        topBar = { ProfileTopBar(onNavigateBack = onNavigateBack) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        ProfileContent(
            modifier = Modifier.padding(paddingValues),
            userProfile = userProfile,
            tasks = tasks,
            groupNameMap = groupNameMap,
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
    groupNameMap: Map<String, String>,
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

    // ✅ 날짜 클릭 상태 (year, month0, day)
    var selectedYmd by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        ProfileImage(
            imageResId = R.drawable.ic_taskflow_logo,
            contentDescription = "프로필"
        )

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
            color = Color.Black
        )

        Spacer(Modifier.height(20.dp))

        EditProfileButton(onClick = onEditProfileClick)

        Spacer(Modifier.height(24.dp))

        // 달력 (날짜 클릭 → 팝업)
        ProfileTaskCalendar(
            tasks = tasks,
            modifier = Modifier.fillMaxWidth(),
            onDayClick = { y, m0, d ->
                selectedYmd = Triple(y, m0, d)
            }
        )

        // 날짜 클릭 팝업
        selectedYmd?.let { (y, m0, d) ->
            val dayTasks = remember(tasks, y, m0, d) {
                tasks.filter { t ->
                    val due = t.dueDate ?: return@filter false
                    val cal = Calendar.getInstance().apply { time = due }
                    cal.get(Calendar.YEAR) == y &&
                            cal.get(Calendar.MONTH) == m0 &&
                            cal.get(Calendar.DAY_OF_MONTH) == d
                }.sortedBy { it.dueDate }
            }

            AlertDialog(
                onDismissRequest = { selectedYmd = null },
                confirmButton = {
                    TextButton(onClick = { selectedYmd = null }) { Text("닫기") }
                },
                title = {
                    Text(
                        text = "${y}.${(m0 + 1).toString().padStart(2, '0')}.${d.toString().padStart(2, '0')} Task"
                    )
                },
                text = {
                    if (dayTasks.isEmpty()) {
                        Text("해당 날짜에 Task가 없습니다.")
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            dayTasks.forEach { task ->
                                val groupName = groupNameMap[task.groupId] ?: "이름 없는 그룹"
                                DayTaskRow(
                                    task = task,
                                    groupName = groupName
                                )
                            }
                        }
                    }
                }
            )
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun DayTaskRow(
    task: Task,
    groupName: String
) {
    val timeText = task.dueDate?.let {
        SimpleDateFormat("a hh:mm", Locale.getDefault()).format(it)
    } ?: "-"

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title.ifBlank { "제목 없음" },
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "시간: $timeText  •  상태: ${task.status.name}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Text(
                text = "그룹: $groupName",  //groupId -> groupName
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        val isDone = task.status == TaskStatus.DONE
        Icon(
            imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = if (isDone) TaskFlowGreen else Color.LightGray
        )
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
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFACFFC1)),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Icon(
            Icons.Default.Edit,
            contentDescription = "프로필 편집",
            tint = Color.Black,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("프로필 편집", color = Color.Black, fontWeight = FontWeight.SemiBold)
    }
}