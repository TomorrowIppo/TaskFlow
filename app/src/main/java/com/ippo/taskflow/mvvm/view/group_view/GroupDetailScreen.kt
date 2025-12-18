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

// 화면 상단에서 전환되는 두 가지 뷰 모드(플로우차트/리스트) 정의
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
    // ViewModel의 상태를 Compose 생명주기에 맞춰 관찰 (구독)
    val group by groupViewModel.currentGroup.collectAsState()
    val taskList by taskViewModel.taskList.collectAsState()
    val metrics by taskViewModel.taskMetrics.collectAsState()

    // 리스트 필터링 및 본인 할당 여부 확인을 위한 현재 사용자 식별자
    val currentUserId = remember { FirebaseAuth.getInstance().currentUser?.uid }

    // 탭 상태 및 삭제 팝업 제어를 위한 내부 UI 상태
    var selectedTab by remember { mutableStateOf(GroupDetailTab.FLOW) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // 화면 진입 시 해당 그룹의 상세 정보와 작업 목록을 서버에서 호출
    LaunchedEffect(groupId) {
        groupViewModel.loadGroupDetail(groupId)
        taskViewModel.loadTasks(groupId)
    }

    // 상세 화면 이동 시 데이터 정합성 버그를 방지하기 위한 동기화 로직
    // 이동 직전에 해당 ID의 최신 데이터를 ViewModel에 로드하여 잔상을 지움
    val navigateWithDataSync: (String) -> Unit = { taskId ->
        taskViewModel.loadTaskById(taskId)
        onNavigateToTaskDetail(taskId)
    }

    // 그룹 삭제 실행 전 최종 확인을 위한 경고창
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
                        onNavigateBack() // 삭제 완료 후 그룹 목록으로 이탈
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
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
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
            // Flow/List 전환 탭
            TabSelectionBar(selectedTab) { selectedTab = it }

            // 탭 변경 시 시각적 부드러움을 위한 교차 애니메이션 적용
            Crossfade(targetState = selectedTab, label = "TabSwitch") { tab ->
                when (tab) {
                    GroupDetailTab.FLOW -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // 그룹 전체 진행 상황 통계 카드
                            item { TaskMetricsCard(metrics) }
                            item {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Text("플로우차트 미리보기", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                                    // 선행 관계 데이터를 맵핑하여 그래프 구조 생성
                                    val taskDependencyMap = createTaskDependencyMap(taskList)
                                    TaskFlowChartVertical(taskList, taskDependencyMap, navigateWithDataSync)
                                }
                            }
                            // 하단 작업 추가 퀵 메뉴
                            item { MyTaskQuickAccess(onNavigateToAddTask) }
                        }
                    }
                    GroupDetailTab.LIST -> {
                        // 리스트 탭에서는 협업 전체가 아닌 '내가 할 일'만 필터링하여 노출
                        val myTasks = taskList.filter { it.assignedToUid == currentUserId }
                        TaskListScreen(myTasks, navigateWithDataSync)
                    }
                }
            }
        }
    }
}

// 필터링된 내 담당 작업들을 리스트 형태로 나열하는 컴포넌트
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
            // 하단 FAB나 네비게이션 바에 가려지지 않도록 여백 확보
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// 리스트 탭 전용 작업 카드 (상태별 컬러 바 포함)
@Composable
fun TaskNodeCardInList(task: Task, onClick: () -> Unit) {
    // 작업 상태(완료, 진행, 막힘 등)에 따른 대표 색상 결정
    val statusColor = when (task.status) {
        TaskStatus.DONE -> Color(0xFF66BB6A)
        TaskStatus.IN_PROGRESS -> Color(0xFF42A5F5)
        TaskStatus.BLOCKED -> Color(0xFFEF5350)
        else -> Color(0xFFBDBDBD)
    }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(75.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 카드 상단에 상태를 나타내는 색상 띠 배치
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(statusColor).align(Alignment.TopCenter))
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "우선순위: ${task.priority}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

// 플로우차트의 가로 스크롤 및 전체적인 트리 구조 컨테이너
@Composable
fun TaskFlowChartVertical(tasks: List<Task>, taskDependencyMap: Map<String?, List<Task>>, onNavigateToTaskDetail: (String) -> Unit) {
    // 선행 조건이 없는(루트) 작업들 추출
    val rootTasks = taskDependencyMap[null] ?: emptyList()
    val horizontalScrollState = rememberScrollState()

    Card(
        modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 600.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Task가 없습니다.", color = Color.Gray) }
            return@Card
        }
        // 가로가 길어질 경우를 대비한 가로 스크롤 영역
        Box(modifier = Modifier.fillMaxSize().horizontalScroll(horizontalScrollState), contentAlignment = Alignment.TopCenter) {
            Column(modifier = Modifier.padding(20.dp).wrapContentWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                rootTasks.forEach { rootTask ->
                    TaskTreeColumn(rootTask, taskDependencyMap, onNavigateToTaskDetail)
                }
            }
        }
    }
}

// 특정 노드를 기준으로 하위 자식 노드들을 재귀적으로 그리는 로직
@Composable
fun TaskTreeColumn(task: Task, taskDependencyMap: Map<String?, List<Task>>, onNavigateToTaskDetail: (String) -> Unit) {
    val children = taskDependencyMap[task.taskId] ?: emptyList()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // 부모 노드 카드
        TaskNodeCardInFlow(task) { onNavigateToTaskDetail(task.taskId) }

        // 자식이 있다면 연결선(Vertical Line)과 함께 하위 노드 배치
        if (children.isNotEmpty()) {
            Box(modifier = Modifier.width(2.dp).height(20.dp).background(Color.LightGray.copy(alpha = 0.6f)))
            Row(modifier = Modifier.wrapContentWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp), verticalAlignment = Alignment.Top) {
                children.forEach { child ->
                    TaskTreeColumn(child, taskDependencyMap, onNavigateToTaskDetail)
                }
            }
        }
    }
}

// 플로우차트 내부에 들어가는 소형 작업 카드
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
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(statusColor).align(Alignment.TopCenter))
            Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = task.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(text = "우선순위: ${task.priority}", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

// Task 리스트를 순회하며 부모 ID별로 자식들을 그룹핑하는 유틸리티
fun createTaskDependencyMap(tasks: List<Task>): Map<String?, List<Task>> {
    val map = mutableMapOf<String?, MutableList<Task>>()
    for (task in tasks) {
        val key = if (task.precursorTaskId.isNullOrBlank()) null else task.precursorTaskId
        map.getOrPut(key) { mutableListOf() }.add(task)
    }
    return map
}

// 그룹 내 전체 작업들의 상태별 수치 요약 레이아웃
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

// 수치와 라벨을 수직으로 나열하는 개별 지표 아이템
@Composable
fun MetricItem(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
        Text(text = label, fontSize = 12.sp, color = Color.Gray)
    }
}

// 내 작업 바로가기 및 신규 작업 생성 유도 카드
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

// 화면 상단 선택 탭 영역
@Composable
fun TabSelectionBar(selectedTab: GroupDetailTab, onTabSelected: (GroupDetailTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TabButton("Flow", selectedTab == GroupDetailTab.FLOW) { onTabSelected(GroupDetailTab.FLOW) }
        TabButton("List", selectedTab == GroupDetailTab.LIST) { onTabSelected(GroupDetailTab.LIST) }
    }
}

// 개별 탭 버튼 스타일 및 선택 효과
@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color(0xFF69F0AE) else Color.White,
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.height(40.dp)
    ) { Text(text) }
}