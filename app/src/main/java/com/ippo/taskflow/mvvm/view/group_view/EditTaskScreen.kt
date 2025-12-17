package com.ippo.taskflow.mvvm.view.group_view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.activity.ui.theme.InputBackground
import com.ippo.taskflow.activity.ui.theme.TaskFlowGreen
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.model.TaskStatus
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EditTaskScreen(
    taskId: String,
    taskViewModel: TaskViewModel,
    groupViewModel: GroupViewModel,
    onTaskUpdated: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isLoading by taskViewModel.isLoading.collectAsState()
    val error by taskViewModel.error.collectAsState()
    val currentTask by taskViewModel.selectedTask.collectAsState()

    // 그룹 내 전체 Task 목록 (선행 작업 후보)
    val currentGroupTasks by taskViewModel.taskList.collectAsState()
    val groupMembers by groupViewModel.currentGroupMembers.collectAsState()

    LaunchedEffect(taskId) {
        taskViewModel.loadTaskById(taskId)
        taskViewModel.clearError()
    }

    LaunchedEffect(currentTask?.groupId) {
        val gid = currentTask?.groupId ?: return@LaunchedEffect
        if (gid.isNotBlank()) {
            groupViewModel.loadGroupDetail(gid)
            taskViewModel.loadTasks(gid)
        }
    }

    var initialized by rememberSaveable(taskId) { mutableStateOf(false) }
    var title by rememberSaveable(taskId) { mutableStateOf("") }
    var description by rememberSaveable(taskId) { mutableStateOf("") }

    // 우선순위 상태 복구
    var selectedPriority by rememberSaveable(taskId) { mutableIntStateOf(2) }
    var expandedPriority by remember { mutableStateOf(false) }

    var selectedStatus by rememberSaveable(taskId) { mutableStateOf(TaskStatus.TODO) }
    var expandedStatus by remember { mutableStateOf(false) }

    var selectedPrecursorTaskId by rememberSaveable(taskId) { mutableStateOf<String?>(null) }
    var expandedPrecursor by remember { mutableStateOf(false) }

    var selectedDate by rememberSaveable(taskId) { mutableStateOf<Date?>(null) }
    var selectedTime by rememberSaveable(taskId) { mutableStateOf<Date?>(null) }
    var selectedAssignedToUid by rememberSaveable(taskId) { mutableStateOf("") }
    var expandedAssignee by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(currentTask) {
        val t = currentTask ?: return@LaunchedEffect
        if (!initialized) {
            title = t.title
            description = t.description
            selectedPriority = t.priority
            selectedStatus = t.status
            selectedDate = t.dueDate
            selectedTime = t.dueDate
            selectedPrecursorTaskId = t.precursorTaskId
            selectedAssignedToUid = t.assignedToUid
            initialized = true
        }
    }

    val finalDueDate = remember(selectedDate, selectedTime) {
        selectedDate?.let { d ->
            Calendar.getInstance().apply {
                time = d
                selectedTime?.let { t ->
                    val tCal = Calendar.getInstance().apply { time = t }
                    set(Calendar.HOUR_OF_DAY, tCal.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, tCal.get(Calendar.MINUTE))
                }
            }.time
        }
    }

    val isSaveEnabled = title.isNotBlank() && currentTask != null && !isLoading

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Task 삭제", fontWeight = FontWeight.Bold) },
            text = { Text("정말 이 Task를 삭제할까요? 삭제 후에는 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    taskViewModel.deleteTask(taskId)
                    onTaskUpdated()
                    onNavigateBack()
                }) { Text("삭제", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") }
            }
        )
    }

    Scaffold { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(modifier = Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                    Text("Task 수정하기", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(Modifier.height(10.dp))

                Text("Task 이름", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                TaskFlowInputBox(title, { title = it }, "Task 이름을 입력하세요.", true)

                Spacer(Modifier.height(10.dp))

                Text("설명", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                TaskFlowInputBox(description, { description = it }, "Task 설명을 입력하세요.", false, minHeight = 84.dp)

                Spacer(Modifier.height(10.dp))

                Text("날짜 / 시간", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateSelectField(selectedDate, Modifier.weight(1f)) { selectedDate = it }
                    TimeSelectField(selectedTime, Modifier.weight(1f)) { selectedTime = it }
                }

                Spacer(Modifier.height(10.dp))

                Text("담당자", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth()) {
                    val label = groupMembers.find { it.uid == selectedAssignedToUid }?.name ?: "없음"
                    OutlinedTextField(
                        value = label, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.rotate(if (expandedAssignee) 180f else 0f)) }
                    )
                    Spacer(Modifier.matchParentSize().clickable { expandedAssignee = true })
                    DropdownMenu(expandedAssignee, { expandedAssignee = false }) {
                        DropdownMenuItem(text = { Text("없음") }, onClick = { selectedAssignedToUid = ""; expandedAssignee = false })
                        groupMembers.forEach { member ->
                            DropdownMenuItem(text = { Text(member.name) }, onClick = { selectedAssignedToUid = member.uid; expandedAssignee = false })
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // ⭐️ 우선순위 드롭다운 복구
                Text("우선순위", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = when (selectedPriority) { 1 -> "1 (높음)"; 2 -> "2 (보통)"; else -> "3 (낮음)" },
                        onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.rotate(if (expandedPriority) 180f else 0f)) }
                    )
                    Spacer(Modifier.matchParentSize().clickable { expandedPriority = true })
                    DropdownMenu(expandedPriority, { expandedPriority = false }) {
                        (1..3).forEach { p ->
                            DropdownMenuItem(
                                text = { Text("$p (${if (p == 1) "높음" else if (p == 2) "보통" else "낮음"})") },
                                onClick = { selectedPriority = p; expandedPriority = false }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // 선행 태스크 (Precursor)
                Text("선행 태스크 (Precursor)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth()) {
                    val precursorTitle = currentGroupTasks.find { it.taskId == selectedPrecursorTaskId }?.title ?: "없음"
                    OutlinedTextField(
                        value = precursorTitle, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.rotate(if (expandedPrecursor) 180f else 0f)) }
                    )
                    Spacer(Modifier.matchParentSize().clickable { expandedPrecursor = true })
                    DropdownMenu(expandedPrecursor, { expandedPrecursor = false }) {
                        DropdownMenuItem(text = { Text("없음") }, onClick = { selectedPrecursorTaskId = null; expandedPrecursor = false })
                        currentGroupTasks.filter { it.taskId != taskId && it.title.isNotBlank() }.forEach { task ->
                            DropdownMenuItem(text = { Text(task.title) }, onClick = { selectedPrecursorTaskId = task.taskId; expandedPrecursor = false })
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Text("상태", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedStatus.value, onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(),
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.rotate(if (expandedStatus) 180f else 0f)) }
                    )
                    Spacer(Modifier.matchParentSize().clickable { expandedStatus = true })
                    DropdownMenu(expandedStatus, { expandedStatus = false }) {
                        TaskStatus.entries.forEach { st ->
                            DropdownMenuItem(text = { Text(st.value) }, onClick = { selectedStatus = st; expandedStatus = false })
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                if (!error.isNullOrBlank()) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(Modifier.weight(1f))

                Row(modifier = Modifier.fillMaxWidth().height(54.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.weight(1f).fillMaxHeight(), shape = RoundedCornerShape(12.dp)) {
                        Text("삭제", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            val base = currentTask ?: return@Button
                            // .copy 함수 대신 새로운 객체 생성 시 누락되었던 필드들 포함
                            val updated = base.copy(
                                title = title,
                                description = description,
                                dueDate = finalDueDate,
                                priority = selectedPriority,
                                status = selectedStatus,
                                precursorTaskId = selectedPrecursorTaskId,
                                assignedToUid = selectedAssignedToUid
                            )
                            taskViewModel.updateTask(updated)
                            onTaskUpdated()
                        },
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        enabled = isSaveEnabled,
                        colors = ButtonDefaults.buttonColors(containerColor = TaskFlowGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(Modifier.size(22.dp), color = Color.White)
                        else Text("저장", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

// -------------------------------------------------------------
// 하단 공통 컴포넌트들
// -------------------------------------------------------------

@Composable
private fun TaskFlowInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black, fontSize = 14.sp),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                Modifier
                    .background(InputBackground, RoundedCornerShape(10.dp))
                    .then(if (minHeight > 0.dp) Modifier.heightIn(min = minHeight) else Modifier)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = TextStyle(color = Color.Gray, fontSize = 14.sp))
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun DateSelectField(selectedDate: Date?, modifier: Modifier, onDateSelected: (Date?) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    selectedDate?.let { calendar.time = it }
    val dialog = DatePickerDialog(context, { _, y, m, d ->
        val res = Calendar.getInstance().apply { set(y, m, d) }
        onDateSelected(res.time)
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    Box(modifier) {
        OutlinedTextField(
            value = selectedDate?.let { SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(it) } ?: "날짜",
            onValueChange = {}, readOnly = true, leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.matchParentSize().clickable { dialog.show() })
    }
}

@Composable
private fun TimeSelectField(selectedTime: Date?, modifier: Modifier, onTimeSelected: (Date?) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    selectedTime?.let { calendar.time = it }
    val dialog = TimePickerDialog(context, { _, h, m ->
        val res = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, h); set(Calendar.MINUTE, m) }
        onTimeSelected(res.time)
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)

    Box(modifier) {
        OutlinedTextField(
            value = selectedTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "시간",
            onValueChange = {}, readOnly = true, leadingIcon = { Icon(Icons.Default.Schedule, null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.matchParentSize().clickable { dialog.show() })
    }
}