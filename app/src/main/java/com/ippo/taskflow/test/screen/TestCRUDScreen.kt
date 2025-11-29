package com.ippo.taskflow.test.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.ippo.taskflow.data.Task // 🚨 Data Class Import
import com.ippo.taskflow.data.Group // 🚨 Data Class Import
import java.util.Date // Date Import

// 🚨 PM Note: 주 색상 상수 정의
val TaskFlowGreen = Color(0xFF1E8A3B)


// =========================================================================
// 1. 메인 테스트 호스트 컴포저블 (Main Host Composable)
// =========================================================================

@Composable
fun TestCRUDScreen(
    groupViewModel: GroupViewModel,
    taskViewModel: TaskViewModel,
    onLogout: () -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 🚨🚨 [수정] 제목과 로그아웃 버튼을 수직으로 배치하여 충돌 방지
        Text("Task/Group CRUD 테스트 화면", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)

        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp, vertical = 8.dp), // 너비 조정
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242)),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            Text("로그아웃 (세션 종료)", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Group 테스트 섹션
        Card(modifier = Modifier.fillMaxWidth().heightIn(min = 350.dp, max = 350.dp)) {
            GroupTestSection(
                groups = groups,
                isLoading = groupIsLoading,
                error = groupError,
                groupViewModel = groupViewModel,
                taskViewModel = taskViewModel // TaskViewModel 전달
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task 테스트 섹션
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            TaskTestSection(
                groups = groups,
                selectedGroupId = selectedGroupId,
                onGroupSelected = { newId ->
                    selectedGroupId = newId
                },
                tasks = tasks,
                isLoading = taskIsLoading,
                error = taskError,
                taskViewModel = taskViewModel
            )
        }
    }
}


// =========================================================================
// 2. GroupTestSection (그룹 CRUD 및 멤버 관리)
// =========================================================================

@Composable
fun GroupTestSection(
    groups: List<Group>,
    isLoading: Boolean,
    error: String?,
    groupViewModel: GroupViewModel,
    taskViewModel: TaskViewModel // TaskViewModel 추가
) {
    val testMemberId = "DEV_MEMBER_B456"

    Column(modifier = Modifier.padding(16.dp)) {
        Text("그룹 관리 (Group CRUD + 멤버)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        if (isLoading) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        else if (error != null) { Text("에러: $error", color = MaterialTheme.colorScheme.error) }
        else { Text("로드된 그룹 수: ${groups.size}", style = MaterialTheme.typography.bodyMedium) }

        Spacer(modifier = Modifier.height(8.dp))

        // 그룹 생성/로드 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                groupViewModel.createGroup(
                    name = "Test Group ${System.currentTimeMillis() % 1000}",
                    description = "삭제 테스트용 그룹"
                )
            }, enabled = !isLoading) {
                Text("생성")
            }
            Button(onClick = {
                groupViewModel.loadGroups()
            }, enabled = !isLoading) {
                Text("로드")
            }
        }

        // 멤버 추가/삭제 테스트
        val firstGroup = groups.firstOrNull()
        if (firstGroup != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    groupViewModel.addMember(firstGroup.groupId, testMemberId)
                }, enabled = !isLoading, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64B5F6))) {
                    Text("멤버 추가")
                }
                Button(onClick = {
                    groupViewModel.deleteMember(firstGroup.groupId, testMemberId)
                }, enabled = !isLoading, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE57373))) {
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
                            // 🚨 그룹 삭제 시 Task 자동 삭제 (Cascading Delete)
                            taskViewModel.deleteAllTasksInGroup(group.groupId)
                            groupViewModel.deleteGroup(group.groupId)
                        }
                    )
                }
            }
        } else if (!isLoading && error == null) {
            Text("로드된 그룹이 없습니다.", modifier = Modifier.padding(top = 8.dp))
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
    // 🚨 [핵심 유지] 선택된 그룹 ID가 바뀔 때 Task 로드를 자동으로 트리거
    LaunchedEffect(selectedGroupId) {
        if (selectedGroupId != null) {
            taskViewModel.loadTasks(selectedGroupId)
        }
    }

    val selectedGroup = groups.find { it.groupId == selectedGroupId }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("작업 관리 (Task CRUD)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        if (isLoading) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        else if (error != null) { Text("에러: $error", color = MaterialTheme.colorScheme.error) }
        else { Text("로드된 작업 수: ${tasks.size}", style = MaterialTheme.typography.bodyMedium) }

        Spacer(modifier = Modifier.height(8.dp))

        if (groups.isEmpty()) {
            Text("Task를 테스트하려면 Group을 먼저 **생성 및 로드**해야 합니다.", color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
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
                    "현재 그룹: ${it.name} (ID: ${it.groupId.take(4)}..)",
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
                        onGroupSelected(groups[nextIndex].groupId) // 다음 그룹으로 업데이트
                    }
                },
                enabled = groups.size > 1 && !isLoading
            ) {
                Text("그룹 변경")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 🚀 Task 생성 버튼 (전체 너비 사용)
        Button(
            onClick = {
                if (selectedGroupId != null) {
                    taskViewModel.createTask(
                        groupId = selectedGroupId,
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

        // Task 목록 (R/U/D 테스트)
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
            Text("로드된 작업이 없습니다. 위에 생성 버튼을 눌러보세요.", modifier = Modifier.padding(top = 8.dp))
        }
    }
}

// ----------------------------------------------------
// 4. SUPPORTING LIST ITEMS
// ----------------------------------------------------

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
            Text(groupName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("ID: ${groupId.take(8)}...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        // 🗑️ 삭제 버튼
        Button(
            onClick = onDeleteClicked,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("삭제")
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
                onCheckedChange = onToggleComplete, // (Boolean) -> Unit 타입 일치
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
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "삭제", tint = Color.Red)
        }
    }
    Divider()
}