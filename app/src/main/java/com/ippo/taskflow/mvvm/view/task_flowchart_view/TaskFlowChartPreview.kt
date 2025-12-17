package com.ippo.taskflow.mvvm.view.task_flowchart_view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.activity.ui.theme.TaskFlowGreen
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.view_model.task.TaskMetrics

@Composable
fun TaskFlowChartPreview(
    groupId: String,
    metrics: TaskMetrics,
    tasks: List<Task>,
    onNavigateToTaskDetail: (String) -> Unit,
    onNavigateToAddTask: () -> Unit,
    onViewFlowChart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Task 통계 카드 (Metrics)
        TaskMetricsCard(metrics)
        Spacer(modifier = Modifier.height(16.dp))

        // 2. Flow Chart 미리보기 영역 (디자인의 "플로우차트 미리보기")
        FlowChartPreviewArea(onViewFlowChart)
        Spacer(modifier = Modifier.height(16.dp))

        // 3. 내 Task 바로가기 (Quick Access)
        MyTaskQuickAccess(onNavigateToAddTask)
    }
}

// ⭐️ Task 통계 카드 (Metrics)
@Composable
fun TaskMetricsCard(metrics: TaskMetrics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            MetricItem(label = "할 일", value = metrics.todo + metrics.inProgress, color = Color(0xFF42A5F5), unit = "소요 Task")
            MetricItem(label = "완료", value = metrics.done, color = Color(0xFF66BB6A), unit = "완료 Task")
            MetricItem(label = "블록", value = metrics.blocked, color = Color(0xFFEF5350), unit = "막힌 작업")
        }
    }
}

// ⭐️ 개별 통계 항목
@Composable
fun MetricItem(label: String, value: Int, color: Color, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Text(unit, fontSize = 10.sp, color = Color.LightGray)
    }
}

// ⭐️ Flow Chart 미리보기 영역
@Composable
fun FlowChartPreviewArea(onViewFlowChart: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "플로우차트 미리보기",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 체크 아이콘 (임시)
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = TaskFlowGreen,
                modifier = Modifier.size(48.dp)
            )
            Text("2개 중 1개 Task 완료", color = Color.Gray, fontSize = 12.sp)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onViewFlowChart,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = TaskFlowGreen)
            ) {
                Text("플로우차트 보기")
            }
        }
    }
}

// ⭐️ 내 Task 바로가기
@Composable
fun MyTaskQuickAccess(onNavigateToAddTask: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("내 담당 Task 보기", fontWeight = FontWeight.SemiBold)
                    Text("0개 미완료 Task", fontSize = 12.sp, color = Color.Gray)
                }
            }
            // Add Task Button
            Button(onClick = onNavigateToAddTask) {
                Text("+")
            }
        }
    }
}