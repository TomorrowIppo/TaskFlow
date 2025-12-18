package com.ippo.taskflow.mvvm.view.group_view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.ippo.taskflow.activity.ui.theme.*
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.model.TaskStatus
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskMetrics
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel

// Redeclaration 방지를 위해 정의 유지
enum class GroupDetailTab {
    FLOW, LIST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    groupViewModel: GroupViewModel,
    taskViewModel: TaskViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddTask: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit
) {
    val group by groupViewModel.currentGroup.collectAsState()
    val taskList by taskViewModel.taskList.collectAsState()
    val metrics by taskViewModel.taskMetrics.collectAsState()
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    var selectedTab by remember { mutableStateOf(GroupDetailTab.FLOW) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(groupId) {
        groupViewModel.loadGroupDetail(groupId)
        taskViewModel.loadTasks(groupId)
    }

    // 🛠️ [버그 해결] 사용자님의 ViewModel에 있는 loadTaskById를 사용하여 데이터 잔상 제거
    val navigateWithDataSync: (String) -> Unit = { taskId ->
        taskViewModel.loadTaskById(taskId) // ⭐️ ViewModel의 _selectedTask 상태를 즉시 갱신
        onNavigateToTaskDetail(taskId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("그룹 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("이 그룹과 관련된 모든 Task가 영구적으로 삭제됩니다. 정말 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        groupViewModel.deleteGroup(groupId)
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("취소") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = group?.name ?: "그룹 상세", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기") } },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "그룹 삭제", tint = Color.Gray)
                    }
                    IconButton(onClick = onNavigateToAddTask) {
                        Icon(Icons.Default.Add, contentDescription = "Task 추가")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightGreyBackground)
        ) {
            TabSelectionBar(selectedTab) { selectedTab = it }

            Crossfade(targetState = selectedTab, label = "TabSwitch") { tab ->
                when (tab) {
                    GroupDetailTab.FLOW -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item { TaskMetricsCard(metrics) }
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("플로우차트 미리보기", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                    val taskDependencyMap = createTaskDependencyMap(taskList)
                                    // ⭐️ 수정된 동기화 함수 적용
                                    TaskFlowChartVertical(taskList, taskDependencyMap, navigateWithDataSync)
                                }
                            }
                            item { MyTaskQuickAccess(onNavigateToAddTask) }
                        }
                    }
                    GroupDetailTab.LIST -> {
                        val myTasks = taskList.filter { it.assignedToUid == currentUserId }
                        // ⭐️ 수정된 동기화 함수 적용
                        TaskListScreen(myTasks, navigateWithDataSync)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// UI 컴포넌트들 (기존의 요청하신 디자인 유지)
// -------------------------------------------------------------
@Composable
fun TaskListScreen(tasks: List<Task>, onNavigateToTaskDetail: (String) -> Unit) {
    if (tasks.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("나에게 할당된 Task가 없습니다.", color = Color.Gray)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(tasks, key = { it.taskId }) { task ->
                TaskNodeCardInList(task = task, onClick = { onNavigateToTaskDetail(task.taskId) })
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun TaskNodeCardInList(task: Task, onClick: () -> Unit) {
    val statusColor = when (task.status) {
        TaskStatus.DONE -> Color(0xFF66BB6A)
        TaskStatus.IN_PROGRESS -> Color(0xFF42A5F5)
        TaskStatus.BLOCKED -> Color(0xFFEF5350)
        else -> Color(0xFFBDBDBD)
    }
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().height(75.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(statusColor).align(Alignment.TopCenter))
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "우선순위: ${task.priority}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TaskFlowChartVertical(tasks: List<Task>, taskDependencyMap: Map<String?, List<Task>>, onNavigateToTaskDetail: (String) -> Unit) {
    val rootTasks = taskDependencyMap[null] ?: emptyList()
    val horizontalScrollState = rememberScrollState()
    Card(modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 600.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        if (tasks.isEmpty()) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Task가 없습니다.", color = Color.Gray) }
            return@Card
        }
        Box(modifier = Modifier.fillMaxSize().horizontalScroll(horizontalScrollState), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.padding(20.dp).wrapContentWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                rootTasks.forEach { rootTask -> TaskTreeColumn(rootTask, taskDependencyMap, onNavigateToTaskDetail) }
            }
        }
    }
}

@Composable
fun TaskTreeColumn(task: Task, taskDependencyMap: Map<String?, List<Task>>, onNavigateToTaskDetail: (String) -> Unit) {
    val children = taskDependencyMap[task.taskId] ?: emptyList()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TaskNodeCardInFlow(task) { onNavigateToTaskDetail(task.taskId) }
        if (children.isNotEmpty()) {
            Box(modifier = Modifier.width(2.dp).height(20.dp).background(Color.LightGray.copy(alpha = 0.6f)))
            Row(modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Top) {
                children.forEach { child -> TaskTreeColumn(child, taskDependencyMap, onNavigateToTaskDetail) }
            }
        }
    }
}

@Composable
fun TaskNodeCardInFlow(task: Task, onClick: () -> Unit) {
    val statusColor = when (task.status) {
        TaskStatus.DONE -> Color(0xFF66BB6A)
        TaskStatus.IN_PROGRESS -> Color(0xFF42A5F5)
        TaskStatus.BLOCKED -> Color(0xFFEF5350)
        else -> Color(0xFFBDBDBD)
    }
    Card(onClick = onClick, modifier = Modifier.width(140.dp).height(65.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)), elevation = CardDefaults.cardElevation(1.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(statusColor).align(Alignment.TopCenter))
            Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = task.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "우선순위: ${task.priority}", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

fun createTaskDependencyMap(tasks: List<Task>): Map<String?, List<Task>> {
    val map = mutableMapOf<String?, MutableList<Task>>()
    for (task in tasks) {
        val key = if (task.precursorTaskId.isNullOrBlank()) null else task.precursorTaskId
        map.getOrPut(key) { mutableListOf() }.add(task)
    }
    return map
}

@Composable
fun TaskMetricsCard(metrics: TaskMetrics) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            MetricItem("진행중", metrics.inProgress, Color(0xFF42A5F5))
            MetricItem("완료", metrics.done, Color(0xFF66BB6A))
            MetricItem("막힘", metrics.blocked, Color.Red)
            MetricItem("진행대기", metrics.todo, Color.Gray)
        }
    }
}

@Composable
fun MetricItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
fun MyTaskQuickAccess(onNavigateToAddTask: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("내 담당 Task 보기", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            FloatingActionButton(onClick = onNavigateToAddTask, containerColor = Color(0xFF5C6BC0), contentColor = Color.White, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}

@Composable
fun TabSelectionBar(selectedTab: GroupDetailTab, onTabSelected: (GroupDetailTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TabButton("Flow", selectedTab == GroupDetailTab.FLOW) { onTabSelected(GroupDetailTab.FLOW) }
        TabButton("List", selectedTab == GroupDetailTab.LIST) { onTabSelected(GroupDetailTab.LIST) }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) Color(0xFF69F0AE) else Color.White, contentColor = if (isSelected) Color.White else Color.Gray), shape = RoundedCornerShape(20.dp), modifier = Modifier.height(40.dp)) { Text(text) }
}