package com.ippo.taskflow.mvvm.view.task_flowchart_view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.activity.ui.theme.TaskCardBackground
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.model.TaskStatus
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TaskListScreen(
    groupId: String,
    taskViewModel: TaskViewModel,
    tasks: List<Task>,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToAddTask: () -> Unit
) {
    // 💡 여기서는 Filter UI를 추가하고, tasks를 필터링하여 LazyColumn에 표시합니다.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(Color.White)
    ) {
        // TODO: 필터링 및 정렬 UI (TaskViewModel의 setTaskFilter 사용)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(tasks, key = { it.taskId }) { task ->
                // ⭐️ Task Card List View
                TaskListCard(task = task, onClick = { onNavigateToTaskDetail(task.taskId) })
            }
        }
    }
}

// ⭐️ List 탭에서 사용되는 Task Card (디자인에 맞게 조정)
@Composable
fun TaskListCard(task: Task, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TaskCardBackground),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Task Status Indicator (색상 바)
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when (task.status) {
                            TaskStatus.DONE -> Color(0xFF66BB6A)
                            TaskStatus.IN_PROGRESS -> Color(0xFF42A5F5)
                            TaskStatus.BLOCKED -> Color(0xFFEF5350)
                            else -> Color(0xFFBDBDBD)
                        }
                    )
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Task Details
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontWeight = FontWeight.SemiBold)
                Text(
                    "마감: ${task.dueDate?.let { SimpleDateFormat("MM.dd", Locale.getDefault()).format(it) } ?: "없음"}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}