package com.ippo.taskflow.mvvm.view.group_view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // 🚨 추가: Task 클릭 이벤트 처리
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// 🚨🚨🚨 [필수 가정 및 Import] 🚨🚨🚨
// 이 경로들은 프로젝트 구조에 맞춰 수정이 필요할 수 있습니다.
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel // AuthViewModel은 현재 사용되지 않으나, 이전 코드에 남아있어 포함

// 🎨 색상 상수 정의 (다른 파일에서 가져와야 하지만, 컴파일을 위해 임시 정의)
val PrimaryGreen = Color(0xFF69F0AE)
val LightGreyBackground = Color(0xFFF5F5F5)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    // 🚨 [수정된 시그니처] MainActivity의 NavHost 요구 사항 충족
    groupId: String,
    groupViewModel: GroupViewModel, // NavHost에서 주입됨
    taskViewModel: TaskViewModel,   // NavHost에서 주입됨
    onNavigateToAddTask: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit // 🚨 추가: Task ID를 인자로 받는 상세 화면 이동 콜백
) {
    // 1. ViewModel 상태 수집
    val taskList by taskViewModel.taskList.collectAsState(initial = emptyList())
    val isLoading by taskViewModel.isLoading.collectAsState(initial = false)
    val error by taskViewModel.error.collectAsState(initial = null)

    // Snackbar 상태를 위한 State
    val snackbarHostState = remember { SnackbarHostState() }

    // Side Effect: 화면 진입 및 groupId 변경 시 데이터 로드 트리거
    LaunchedEffect(groupId) {
        if (groupId.isNotBlank()) {
            taskViewModel.loadTasks(groupId) // 해당 그룹의 Task 로드
        }
    }

    // Side Effect: Task 삭제 후 오류 발생 시 Snackbar 표시
    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(
                message = error!!,
                actionLabel = "확인"
            )
            // 오류 메시지 초기화 (선택 사항)
            // taskViewModel.clearError()
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddTask,
                containerColor = PrimaryGreen
            ) {
                Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }, // 🚨 SnackbarHostState 사용
        bottomBar = { SimpleBottomNavBar() } // 하단 메뉴바
    ) { padding ->

        // --- UI 콘텐츠 시작 ---
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Group ID: ${groupId.take(8)}...", style = MaterialTheme.typography.titleLarge)

            Spacer(modifier = Modifier.height(16.dp))

            // 에러 메시지 표시 (Snackbar로 대체 가능하지만 일단 유지)
            // if (!error.isNullOrBlank()) {
            //     Text(error!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
            //     Spacer(modifier = Modifier.height(8.dp))
            // }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Tasks (${taskList.size})", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            // 2. Task List
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (taskList.isNotEmpty()) {
                LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    items(taskList, key = { it.taskId }) { task ->
                        // 🚨 TaskListItem에 클릭 이벤트와 삭제 콜백 연결
                        TaskListItem(
                            task = task,
                            onTaskClick = { clickedTaskId ->
                                onNavigateToTaskDetail(clickedTaskId)
                            },
                            onDeleteTask = { taskIdToDelete ->
                                taskViewModel.deleteTask(taskIdToDelete)
                            }
                        )
                        Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    }
                }
            } else {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    Text("이 그룹에는 할당된 Task가 없습니다.", color = Color.Gray)
                }
            }

            // 4. Group Action Button (예시)
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

// 🚨 수정된 Task List Item (클릭 및 삭제 기능 포함)
@Composable
private fun TaskListItem(
    task: Task,
    onTaskClick: (String) -> Unit, // Task 클릭 시 상세 이동 콜백
    onDeleteTask: (String) -> Unit // Task 삭제 버튼 클릭 콜백
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick(task.taskId) } // 🚨 Row 전체를 클릭 가능하게 만듭니다.
            .padding(vertical = 12.dp, horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // 🚨 아이템을 양 끝으로 분산
    ) {
        // 왼쪽 (상태 + 제목)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Task Status Indicator Placeholder
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (task.isCompleted) PrimaryGreen else Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (task.isCompleted) FontWeight.Light else FontWeight.Normal
            )
        }

        // 오른쪽 (삭제 버튼)
        IconButton(
            onClick = { onDeleteTask(task.taskId) }, // 🚨 삭제 콜백 호출
            modifier = Modifier.size(36.dp) // 클릭 영역 확보
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Task 삭제",
                tint = Color.Red.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// 사용자 정의 컴포넌트 (변경 없음)
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
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
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
    val NavBarColor = Color(0xFFB9F6CA) // TaskFlowLightGreen과 유사
    val NavItemColor = PrimaryGreen

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavBarColor)
                .padding(horizontal = 40.dp, vertical = 10.dp)
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder Icons for Home, Group, Profile
            Box(modifier = Modifier.size(32.dp).background(NavItemColor, CircleShape))
            Box(modifier = Modifier.size(48.dp).background(NavItemColor, RoundedCornerShape(8.dp)))
            Box(modifier = Modifier.size(32.dp).background(NavItemColor, CircleShape))
        }
    }
}