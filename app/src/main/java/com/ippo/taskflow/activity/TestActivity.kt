package com.ippo.taskflow.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ippo.taskflow.ui.theme.TaskFlowTheme
import com.ippo.taskflow.auth.AuthViewModel
import com.ippo.taskflow.group.GroupViewModel
import com.ippo.taskflow.task.TaskViewModel
import com.ippo.taskflow.data.Group // 🚨 Group Data Class Import
import com.ippo.taskflow.data.Task // 🚨 Task Data Class Import
import java.util.Date // Date Import (TaskModel의 dueDate용)


// 🚨 PM Note: 주 색상 상수 정의
val TaskFlowGreen = Color(0xFF1E8A3B)


// 1. 🧪 TestActivity: Group/Task ViewModel 테스트 호스트
class TestActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TaskFlowTheme {
                // ViewModels 생성 (Activity Scope)
                val authViewModel: AuthViewModel = viewModel()
                val groupViewModel: GroupViewModel = viewModel()
                val taskViewModel: TaskViewModel = viewModel()

                GroupTestHostScreen(
                    authViewModel = authViewModel,
                    groupViewModel = groupViewModel,
                    taskViewModel = taskViewModel // TaskViewModel 주입
                )
            }
        }
    }
}


// 2. 🏠 GroupTestHostScreen: 인증 상태 관리 및 TestScreen 호출
@Composable
fun GroupTestHostScreen(
    authViewModel: AuthViewModel,
    groupViewModel: GroupViewModel,
    taskViewModel: TaskViewModel
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val isAuthLoading by authViewModel.isLoading.collectAsState()
    val authError by authViewModel.error.collectAsState()

    // 🚨 Activity 시작 시 자동으로 익명 로그인 시도 (테스트 세션 확보)
    LaunchedEffect(Unit) {
        if (!isAuthenticated && !isAuthLoading) {
            authViewModel.signInAsGuest()
            Log.d("TestAuth", "GroupTestHostScreen 시작: 익명 로그인 시도...")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        if (isAuthLoading) {
            CircularProgressIndicator(Modifier.padding(top = 50.dp))
            Text("인증 토큰 획득 중...")
        } else if (!isAuthenticated) {
            Text("인증 실패: ${authError ?: "로그인이 필요합니다."}", color = Color.Red)
            Spacer(modifier = Modifier.height(10.dp))
            Button(onClick = { authViewModel.signInAsGuest() }) {
                Text("Guest 로그인 재시도")
            }
        } else {
            // 💡 인증 성공 시 TestScreen 호출
            Text("✅ 인증 성공! UID: ${authViewModel.userId!!.take(10)}...", color = TaskFlowGreen, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))

            // 핵심 테스트 화면
            TestScreen(
                groupViewModel = groupViewModel,
                taskViewModel = taskViewModel
            )
        }

        // 🚨 로그아웃 버튼 (세션 종료 후 재시작 테스트용)
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = { authViewModel.signOut() }, colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)) {
            Text("세션 종료 (로그아웃)")
        }
    }
}


// 3. 🧪 TestScreen: 그룹 및 작업 CRUD 통합
@Composable
fun TestScreen(
    groupViewModel: GroupViewModel,
    taskViewModel: TaskViewModel
) {
    val groups by groupViewModel.groupList.collectAsState()
    val groupIsLoading by groupViewModel.isLoading.collectAsState()
    val groupError by groupViewModel.error.collectAsState()

    val tasks by taskViewModel.taskList.collectAsState()
    val taskIsLoading by taskViewModel.isLoading.collectAsState()
    val taskError by taskViewModel.error.collectAsState()

    val firstGroup = groups.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Group 테스트 섹션 (스크롤 가능하게 처리)
        Card(modifier = Modifier.fillMaxWidth().heightIn(min = 350.dp, max = 350.dp)) {
            GroupTestSection(
                groups = groups,
                isLoading = groupIsLoading,
                error = groupError,
                groupViewModel = groupViewModel
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Task 테스트 섹션 (스크롤 가능하게 처리)
        Card(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
            TaskTestSection(
                firstGroupId = firstGroup?.groupId,
                tasks = tasks,
                isLoading = taskIsLoading,
                error = taskError,
                taskViewModel = taskViewModel
            )
        }
    }
}


// 4. 🛠️ 지원 Composable: GroupTestSection
@Composable
fun GroupTestSection(
    groups: List<Group>,
    isLoading: Boolean,
    error: String?,
    groupViewModel: GroupViewModel
) {
    // ... (GroupTestSection 구현은 이전 답변과 동일) ...
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
        val testMemberId = "DEV_MEMBER_B456"
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

        // 그룹 목록 (삭제 가능)
        if (groups.isNotEmpty()) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(groups, key = { it.groupId }) { group ->
                    GroupListItem(
                        groupName = group.name,
                        groupId = group.groupId,
                        onDeleteClicked = {
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


// 5. 🛠️ 지원 Composable: TaskTestSection
@Composable
fun TaskTestSection(
    firstGroupId: String?,
    tasks: List<Task>,
    isLoading: Boolean,
    error: String?,
    taskViewModel: TaskViewModel
) {
    // ... (TaskTestSection 구현은 이전 답변과 동일) ...
    Column(modifier = Modifier.padding(16.dp)) {
        Text("작업 관리 (Task CRUD)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        if (isLoading) { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
        else if (error != null) { Text("에러: $error", color = MaterialTheme.colorScheme.error) }
        else { Text("로드된 작업 수: ${tasks.size}", style = MaterialTheme.typography.bodyMedium) }

        Spacer(modifier = Modifier.height(8.dp))

        if (firstGroupId == null) {
            Text("Task를 테스트하려면 Group을 먼저 **생성 및 로드**해야 합니다.", color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            return
        }

        // C: Create Task 버튼
        Button(onClick = {
            taskViewModel.createTask(
                groupId = firstGroupId,
                title = "New Task ${tasks.size + 1}",
                assignedToUid = "DEV_TEST_USER",
                dueDate = Date(),
                priority = 1
            )
        }, enabled = !isLoading) {
            Text("작업 생성 (그룹 ${firstGroupId.take(4)}..에)")
        }

        Divider(Modifier.padding(vertical = 8.dp))

        // Task 목록 (R/U/D 테스트)
        if (tasks.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(tasks, key = { it.taskId }) { task ->
                    TaskListItem(
                        task = task,
                        onToggleComplete = {
                            val newStatus = if (task.status == "DONE") "TODO" else "DONE"
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


// 6. 🛠️ 지원 Composable: GroupListItem
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


// 7. 🛠️ 지원 Composable: TaskListItem
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
            Icon(imageVector = Icons.Filled.Delete, contentDescription = "삭제", tint = Color.Red)
        }
    }
    Divider()
}