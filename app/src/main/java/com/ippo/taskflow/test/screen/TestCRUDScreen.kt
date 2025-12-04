package com.ippo.taskflow.test.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.group.GroupViewModel
import com.ippo.taskflow.task.TaskViewModel
import com.ippo.taskflow.data.Task
import com.ippo.taskflow.data.Group
import java.util.Date

// PM Note: 주 색상 상수 정의
val TaskFlowGreen = Color(0xFF1E8A3B)

// =========================================================================
// 1. 메인 테스트 호스트 컴포저블 (Main Host Composable)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestCRUDScreen(
    groupViewModel: GroupViewModel,
    taskViewModel: TaskViewModel,
    onLogout: () -> Unit,
    // ⭐️ 수정: ProfileScreen으로 이동하는 콜백으로 변경 ⭐️
    onNavigateToProfile: () -> Unit
) {
    // 1. ViewModel 상태 관찰
    val groups by groupViewModel.groupList.collectAsState()
    val groupIsLoading by groupViewModel.isLoading.collectAsState()
    val groupError by groupViewModel.error.collectAsState()

    val tasks by taskViewModel.taskList.collectAsState()
    val taskIsLoading by taskViewModel.isLoading.collectAsState()
    val taskError by taskViewModel.error.collectAsState()

    // 2. Task 작업을 위한 선택된 그룹 ID 상태
    var selectedGroupId by remember { mutableStateOf<String?>(null) }

    // 그룹이 로드될 때, selectedGroupId를 유효한 첫 번째 그룹으로 초기 설정
    LaunchedEffect(groups) {
        if (groups.isNotEmpty() && (selectedGroupId == null || groups.none { it.groupId == selectedGroupId })) {
            selectedGroupId = groups.first().groupId
        } else if (groups.isEmpty()) {
            selectedGroupId = null
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Task/Group CRUD 테스트 화면") },
                actions = {
                    // ⭐️ 수정: onNavigateToProfile 콜백 호출 ⭐️
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(Icons.Filled.Settings, contentDescription = "프로필 화면으로 이동")
                    }

                    // 기존 로그아웃 버튼
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "로그아웃")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Group 테스트 섹션 - 남은 공간의 40%
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(top = 8.dp)
            ) {
                GroupTestSection(
                    groups = groups,
                    isLoading = groupIsLoading,
                    error = groupError,
                    groupViewModel = groupViewModel,
                    taskViewModel = taskViewModel
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Task 테스트 섹션 - 남은 공간의 60%
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {
                TaskTestSection(
                    groups = groups,
                    selectedGroupId = selectedGroupId,
                    onGroupSelected = { newId -> selectedGroupId = newId },
                    tasks = tasks,
                    isLoading = taskIsLoading,
                    error = taskError,
                    taskViewModel = taskViewModel
                )
            }
        }
    }
}


// =========================================================================
// 2. GroupTestSection (그룹 CRUD 및 멤버 관리)
// (이하 GroupTestSection, TaskTestSection, Supporting Composables 코드는 변경 없음)
// =========================================================================

@Composable
fun GroupTestSection(
    groups: List<Group>,
    isLoading: Boolean,
    error: String?,
    groupViewModel: GroupViewModel,
    taskViewModel: TaskViewModel
) {
    val testMemberId = "DEV_MEMBER_B456"
    val firstGroup = groups.firstOrNull()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("그룹 관리", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        LoadingAndErrorDisplay(
            isLoading = isLoading,
            error = error,
            successMessage = "로드된 그룹 수: ${groups.size}"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 그룹 생성/로드 버튼
        TestButtonsRow {
            Button(onClick = {
                groupViewModel.createGroup(
                    name = "Test Group ${System.currentTimeMillis() % 1000}",
                    description = "삭제 테스트용 그룹"
                )
            }, enabled = !isLoading, modifier = Modifier.weight(1f)) {
                Text("생성")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { groupViewModel.loadGroups() }, enabled = !isLoading, modifier = Modifier.weight(1f)) {
                Text("로드")
            }
        }

        // 멤버 추가/삭제 테스트
        if (firstGroup != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TestButtonsRow {
                Button(
                    onClick = { groupViewModel.addMember(firstGroup.groupId, testMemberId) },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("멤버 추가")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { groupViewModel.deleteMember(firstGroup.groupId, testMemberId) },
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("멤버 삭제")
                }
            }
        }

        Divider(Modifier.padding(vertical = 8.dp))

        // 그룹 목록 (삭제 연동)
        if (groups.isNotEmpty()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(groups, key = { it.groupId }) { group ->
                    GroupListItem(
                        groupName = group.name,
                        groupId = group.groupId,
                        onDeleteClicked = {
                            taskViewModel.deleteAllTasksInGroup(group.groupId) // Cascading Delete
                            groupViewModel.deleteGroup(group.groupId)
                        }
                    )
                }
            }
        } else if (!isLoading && error == null) {
            Text("로드된 그룹이 없습니다.", modifier = Modifier.padding(top = 8.dp), color = Color.Gray)
        }
    }
}

// =========================================================================
// 3. TaskTestSection (작업 CRUD)
// =========================================================================

@Composable
fun TaskTestSection(
    groups: List<Group>,
    selectedGroupId: String?,
    onGroupSelected: (String) -> Unit,
    tasks: List<Task>,
    isLoading: Boolean,
    error: String?,
    taskViewModel: TaskViewModel
) {
    // 핵심: 선택된 그룹 ID가 바뀔 때 Task 로드를 자동으로 트리거
    LaunchedEffect(selectedGroupId) {
        if (selectedGroupId != null) {
            taskViewModel.loadTasks(selectedGroupId)
        }
    }

    val selectedGroup = groups.find { it.groupId == selectedGroupId }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("작업 관리", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        LoadingAndErrorDisplay(
            isLoading = isLoading,
            error = error,
            successMessage = "로드된 작업 수: ${tasks.size}"
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (groups.isEmpty()) {
            Text(
                "Task를 테스트하려면 Group을 먼저 **생성 및 로드**해야 합니다.",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            return
        }

        // 그룹 변경 및 현재 그룹 표시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            selectedGroup?.let {
                Text(
                    "그룹: ${it.name} (ID: ${it.groupId.take(4)}..)",
                    style = MaterialTheme.typography.titleMedium.copy(color = TaskFlowGreen),
                    modifier = Modifier.weight(1f)
                )
            }

            // 🔄 그룹 순차 변경 버튼
            Button(
                onClick = {
                    val currentIndex = groups.indexOfFirst { it.groupId == selectedGroupId }
                    if (currentIndex != -1) {
                        val nextIndex = (currentIndex + 1) % groups.size
                        onGroupSelected(groups[nextIndex].groupId)
                    }
                },
                enabled = groups.size > 1 && !isLoading
            ) {
                Text("그룹 변경")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 🚀 Task 생성 버튼
        Button(
            onClick = {
                selectedGroupId?.let { groupId ->
                    taskViewModel.createTask(
                        groupId = groupId,
                        title = "New Task ${tasks.size + 1}",
                        assignedToUid = "DEV_TEST_USER",
                        dueDate = Date(),
                        priority = 1
                    )
                }
            },
            enabled = !isLoading && selectedGroupId != null,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("작업 생성")
        }


        Divider(Modifier.padding(vertical = 8.dp))

        // Task 목록
        if (tasks.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks, key = { it.taskId }) { task ->
                    TaskListItem(
                        task = task,
                        onToggleComplete = { newCheckedState ->
                            val newStatus = if (newCheckedState) "DONE" else "TODO"
                            taskViewModel.updateTaskStatus(task.taskId, newStatus)
                        },
                        onDeleteClicked = {
                            taskViewModel.deleteTask(task.taskId)
                        }
                    )
                }
            }
        } else if (!isLoading && error == null) {
            Text("로드된 작업이 없습니다.", modifier = Modifier.padding(top = 8.dp), color = Color.Gray)
        }
    }
}

// =========================================================================
// 4. SUPPORTING COMPOSABLES (재사용 가능한 UI 요소)
// =========================================================================

@Composable
fun GroupListItem(groupName: String, groupId: String, onDeleteClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(groupName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("ID: ${groupId.take(8)}...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        // 🗑️ 삭제 버튼
        IconButton(
            onClick = onDeleteClicked,
            colors = IconButtonDefaults.iconButtonColors(contentColor = Color.Red)
        ) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "그룹 삭제")
        }
    }
    Divider()
}

@Composable
fun TaskListItem(task: Task, onToggleComplete: (Boolean) -> Unit, onDeleteClicked: () -> Unit) {
    val isCompleted = task.status == "DONE"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleComplete(!isCompleted) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = onToggleComplete,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                task.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isCompleted) Color.Gray else Color.Black,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
            )
        }

        // D: Delete (삭제 버튼)
        IconButton(onClick = onDeleteClicked, modifier = Modifier.size(36.dp)) {
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "작업 삭제", tint = Color.Red)
        }
    }
    Divider()
}

@Composable
fun LoadingAndErrorDisplay(isLoading: Boolean, error: String?, successMessage: String) {
    if (isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    } else if (error != null) {
        Text("에러: $error", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    } else {
        Text(successMessage, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}

@Composable
fun TestButtonsRow(content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        content = content
    )
}