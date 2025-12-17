package com.ippo.taskflow.mvvm.view.group_view

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.ippo.taskflow.activity.ui.theme.*
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.model.TaskStatus
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskMetrics
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import java.util.Locale

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
    val isLoading by taskViewModel.isLoading.collectAsState()
    val metrics by taskViewModel.taskMetrics.collectAsState()

    var selectedTab by remember { mutableStateOf(GroupDetailTab.FLOW) }

    LaunchedEffect(groupId) {
        groupViewModel.loadGroupDetail(groupId)
        taskViewModel.loadTasks(groupId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = group?.name ?: "그룹 로딩 중...", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기") } },
                actions = { IconButton(onClick = onNavigateToAddTask) { Icon(Icons.Default.Add, contentDescription = "추가") } }
            )
        }
    ) { paddingValues ->
        if (isLoading && group == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }

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
                                    TaskFlowChartVertical(taskList, taskDependencyMap, onNavigateToTaskDetail)
                                }
                            }
                            item { MyTaskQuickAccess(onNavigateToAddTask) }
                        }
                    }
                    GroupDetailTab.LIST -> {
                        TaskListScreen(taskList, onNavigateToTaskDetail, onNavigateToAddTask)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ⭐️ [리팩토링] 세로 트리 그래프 컴포넌트 (에러 수정 완료)
// -------------------------------------------------------------

@Composable
fun TaskFlowChartVertical(
    tasks: List<Task>,
    taskDependencyMap: Map<String?, List<Task>>,
    onNavigateToTaskDetail: (String) -> Unit
) {
    val rootTasks = taskDependencyMap[null] ?: emptyList()
    // 세로형 트리는 아래로 길어질 뿐만 아니라 자식이 많으면 옆으로도 길어지므로 가로 스크롤 추가
    val horizontalScrollState = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 300.dp, max = 600.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Task가 없습니다.", color = Color.Gray)
            }
            return@Card
        }

        // 가로 스크롤만 적용 (세로 스크롤은 부모 LazyColumn이 담당)
        Box(modifier = Modifier.fillMaxSize().horizontalScroll(horizontalScrollState)) {
            Column(
                modifier = Modifier.padding(20.dp).wrapContentWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rootTasks.forEach { rootTask ->
                    TaskTreeColumn(rootTask, taskDependencyMap, onNavigateToTaskDetail)
                }
            }
        }
    }
}

@Composable
fun TaskTreeColumn(
    task: Task,
    taskDependencyMap: Map<String?, List<Task>>,
    onNavigateToTaskDetail: (String) -> Unit
) {
    val children = taskDependencyMap[task.taskId] ?: emptyList()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 1. 부모 노드 (디자인 통일)
        TaskNodeCardInFlow(task) { onNavigateToTaskDetail(task.taskId) }

        if (children.isNotEmpty()) {
            // 2. 부모-자식 연결 세로선
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(20.dp)
                    .background(Color.LightGray.copy(alpha = 0.6f))
            )

            // 3. 자식들을 가로로 나열
            Row(
                modifier = Modifier.wrapContentWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                children.forEach { child ->
                    TaskTreeColumn(child, taskDependencyMap, onNavigateToTaskDetail)
                }
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

    Card(
        onClick = onClick,
        modifier = Modifier.width(140.dp).height(65.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 상단 상태 색상 바
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(statusColor).align(Alignment.TopCenter))

            Column(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = task.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "우선순위: ${task.priority}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// -------------------------------------------------------------
// ⭐️ 데이터 매핑 및 보조 컴포넌트 (동일 유지)
// -------------------------------------------------------------

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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            MetricItem("할 일", metrics.todo, AccentBlue)
            MetricItem("완료", metrics.done, PrimaryGreen)
            MetricItem("막힘", metrics.blocked, Color.Red)
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
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
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) PrimaryGreen else Color.White, contentColor = if (isSelected) Color.White else Color.Gray),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(40.dp)
    ) { Text(text) }
}

@Composable
fun TaskListScreen(tasks: List<Task>, onNavigateToTaskDetail: (String) -> Unit, onNavigateToAddTask: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(tasks, key = { it.taskId }) { task ->
            Card(
                onClick = { onNavigateToTaskDetail(task.taskId) },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(Color(0xFFBDBDBD), RoundedCornerShape(5.dp)))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(task.title, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}