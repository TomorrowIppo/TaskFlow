package com.ippo.taskflow.mvvm.view.task_flowchart_view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.activity.ui.theme.InputBackground
import com.ippo.taskflow.activity.ui.theme.LightGreyBackground
import com.ippo.taskflow.activity.ui.theme.TaskCardBackground
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.model.TaskStatus
import java.text.SimpleDateFormat
import java.util.Locale

// 🎨 Task Flow UI를 위한 상태 정의
enum class TaskFlowStatus(
    val status: TaskStatus?,
    val displayName: String,
    val color: Color
) {
    START(TaskStatus.IN_PROGRESS, "Start", Color(0xFF9DE7B2)),
    TO_DO(TaskStatus.TODO, "To Do", Color(0xFFE0E0E0)),
    IN_PROGRESS(TaskStatus.IN_PROGRESS, "In Progress", Color(0xFFBBDEFB)),
    BLOCKED(TaskStatus.BLOCKED, "Blocked", Color(0xFFF44336)),
    DONE(TaskStatus.DONE, "Done", Color(0xFF69F0AE)),
    GOAL(null, "Goal", Color(0xFF4CAF50));

    companion object {
        val ORDERED_STATUSES = listOf(START, TO_DO, IN_PROGRESS, BLOCKED, DONE, GOAL)
    }
}


/**
 * ⭐️ TaskFlowChartScreen Composable: 그룹 상세 정보와 별개로 Task 흐름을 시각화합니다.
 * 이 화면은 Task 목록 데이터를 인수로 받아옵니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFlowChartScreen(
    groupName: String,
    tasks: List<Task>,
    onNavigateBack: () -> Unit,
    onNavigateToTaskDetail: (String) -> Unit
) {
    // Task 목록을 TaskStatus Enum에 따라 그룹화
    val groupedTasks = tasks.groupBy { it.status }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = groupName, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
                // TODO: 기타 액션 버튼 추가
            )
        }
    ) { paddingValues ->

        // ⭐️ Task Flow Chart UI (LazyRow로 Kanban Board 구현)
        LazyRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(LightGreyBackground),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // TaskFlowStatus.ORDERED_STATUSES를 순회하며 각 상태별 Column 생성
            items(TaskFlowStatus.ORDERED_STATUSES) { flowStatus ->
                // TaskFlowStatus에 따라 데이터를 매핑하여 TaskStatusColumn에 전달
                val currentTasks = when (flowStatus) {
                    TaskFlowStatus.DONE -> groupedTasks[TaskStatus.DONE] ?: emptyList()
                    TaskFlowStatus.START, TaskFlowStatus.IN_PROGRESS -> groupedTasks[TaskStatus.IN_PROGRESS] ?: emptyList()
                    TaskFlowStatus.TO_DO -> groupedTasks[TaskStatus.TODO] ?: emptyList()
                    TaskFlowStatus.BLOCKED -> groupedTasks[TaskStatus.BLOCKED] ?: emptyList()
                    TaskFlowStatus.GOAL -> emptyList() // Goal Task가 필요하면 Task 모델 수정 필요
                }

                TaskStatusColumn(
                    flowStatus = flowStatus,
                    tasks = currentTasks,
                    onTaskClick = onNavigateToTaskDetail
                )
            }
        }
    }
}

// ⭐️ 각 상태별 Task 목록을 표시하는 Column 컴포넌트
@Composable
fun TaskStatusColumn(
    flowStatus: TaskFlowStatus,
    tasks: List<Task>,
    onTaskClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(InputBackground.copy(alpha = 0.8f))
            .padding(8.dp)
    ) {
        StatusHeader(flowStatus, tasks.size)

        Spacer(modifier = Modifier.height(8.dp))

        if (tasks.isEmpty()) {
            Text(
                "Task가 없습니다.",
                color = Color.Gray,
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(tasks, key = { it.taskId }) { task ->
                    TaskCard(task = task, onTaskClick = { onTaskClick(task.taskId) })
                }
            }
        }
    }
}

// ⭐️ Task Status 헤더
@Composable
fun StatusHeader(flowStatus: TaskFlowStatus, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(flowStatus.color.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = flowStatus.displayName,
            fontWeight = FontWeight.Bold,
            color = flowStatus.color
        )
        Text(
            text = count.toString(),
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}

// ⭐️ Task 정보를 담는 카드
@Composable
fun TaskCard(task: Task, onTaskClick: () -> Unit) {
    Card(
        onClick = onTaskClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = TaskCardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Task Title
            Text(task.title, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)

            // 🚨 선행 태스크(Precursor) 표시 로직
            if (task.precursorTaskId != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "▶️ 선행 Task 필요",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Red
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Due Date / Assigned User Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "담당: ${task.assignedToUid.take(4)}...", // 임시 사용자 표시
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "마감: ${task.dueDate?.let { SimpleDateFormat("MM.dd", Locale.getDefault()).format(it) } ?: "없음"}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}