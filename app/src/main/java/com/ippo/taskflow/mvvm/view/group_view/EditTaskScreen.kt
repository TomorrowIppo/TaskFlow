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
import com.ippo.taskflow.mvvm.model.TaskStatus
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun EditTaskScreen(
    taskId: String,
    taskViewModel: TaskViewModel,
    onTaskUpdated: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val isLoading by taskViewModel.isLoading.collectAsState()
    val error by taskViewModel.error.collectAsState()
    val currentTask by taskViewModel.selectedTask.collectAsState()

    // ✅ 화면 진입 시 단일 Task 로드 (taskList 의존 제거)
    LaunchedEffect(taskId) {
        taskViewModel.loadTaskById(taskId)
        taskViewModel.clearError()
    }

    // ---- Local UI state (Task 로드 후 1회만 세팅) ----
    var initialized by rememberSaveable(taskId) { mutableStateOf(false) }

    var title by rememberSaveable(taskId) { mutableStateOf("") }
    var description by rememberSaveable(taskId) { mutableStateOf("") }

    var selectedPriority by rememberSaveable(taskId) { mutableStateOf(2) }
    var expandedPriority by rememberSaveable(taskId) { mutableStateOf(false) }

    var selectedStatus by rememberSaveable(taskId) { mutableStateOf(TaskStatus.TODO) }
    var expandedStatus by rememberSaveable(taskId) { mutableStateOf(false) }

    var selectedPrecursorTaskId by rememberSaveable(taskId) { mutableStateOf<String?>(null) }
    var expandedPrecursor by rememberSaveable(taskId) { mutableStateOf(false) }

    var selectedDate by rememberSaveable(taskId) { mutableStateOf<Date?>(null) }
    var selectedTime by rememberSaveable(taskId) { mutableStateOf<Date?>(null) }

    // ✅ [추가] 삭제 확인 다이얼로그 상태
    var showDeleteConfirm by rememberSaveable(taskId) { mutableStateOf(false) } // ✅ ADDED

    // 폼 초기 세팅(딱 1번)
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
            initialized = true
        }
    }

    // 최종 dueDate 계산 (AddTaskScreen 방식 유지)
    val finalDueDate = selectedDate?.let { date ->
        selectedTime?.let { time ->
            Date(date.year, date.month, date.date, time.hours, time.minutes)
        } ?: date
    }

    val isSaveEnabled = title.isNotBlank() && currentTask != null && !isLoading

    // ✅ [추가] 삭제 확인 다이얼로그 UI
    if (showDeleteConfirm) { // ✅ ADDED
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Task 삭제") },
            text = { Text("정말 이 Task를 삭제할까요? 삭제하면 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        taskViewModel.deleteTask(taskId)
                        onTaskUpdated()
                        onNavigateBack()
                    }
                ) { Text("삭제") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("취소") }
            }
        )
    } // ✅ ADDED END

    Scaffold { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // 상단 바
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                    Text(
                        text = "Task 수정하기",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentTask == null && !isLoading) {
                    Text(
                        text = "Task 정보를 불러올 수 없습니다.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onNavigateBack) { Text("뒤로가기") }
                    return@Column
                }

                // 제목
                Text(
                    text = "Task 이름",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                TaskFlowInputBox(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Task 이름을 입력하세요.",
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 설명
                Text(
                    text = "설명",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                TaskFlowInputBox(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Task 설명을 입력하세요.",
                    singleLine = false,
                    minHeight = 100.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 날짜/시간
                Text(
                    text = "날짜 / 시간",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DateSelectField(
                        selectedDate = selectedDate,
                        modifier = Modifier.weight(1f),
                        onDateSelected = { selectedDate = it }
                    )
                    TimeSelectField(
                        selectedTime = selectedTime,
                        modifier = Modifier.weight(1f),
                        onTimeSelected = { selectedTime = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 상태
                Text(
                    text = "상태",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedStatus.value,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "상태 선택",
                                modifier = Modifier.rotate(if (expandedStatus) 180f else 0f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedStatus = true }
                    )
                    DropdownMenu(
                        expanded = expandedStatus,
                        onDismissRequest = { expandedStatus = false }
                    ) {
                        TaskStatus.entries.forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.value) },
                                onClick = {
                                    selectedStatus = st
                                    expandedStatus = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 우선순위
                Text(
                    text = "우선순위",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = when (selectedPriority) {
                            1 -> "1 (높음)"
                            2 -> "2 (보통)"
                            else -> "3 (낮음)"
                        },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "우선순위",
                                modifier = Modifier.rotate(if (expandedPriority) 180f else 0f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedPriority = true }
                    )
                    DropdownMenu(
                        expanded = expandedPriority,
                        onDismissRequest = { expandedPriority = false }
                    ) {
                        (1..3).forEach { p ->
                            DropdownMenuItem(
                                text = { Text("$p (${if (p == 1) "높음" else if (p == 2) "보통" else "낮음"})") },
                                onClick = { selectedPriority = p; expandedPriority = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 선행 Task (ID만 저장: 후보 리스트는 이후에 그룹 기준으로 확장 가능)
                Text(
                    text = "선행 태스크 (Precursor)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPrecursorTaskId ?: "없음 (선택 사항)",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "선행 태스크",
                                modifier = Modifier.rotate(if (expandedPrecursor) 180f else 0f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { expandedPrecursor = true }
                    )

                    DropdownMenu(
                        expanded = expandedPrecursor,
                        onDismissRequest = { expandedPrecursor = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("없음") },
                            onClick = { selectedPrecursorTaskId = null; expandedPrecursor = false }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!error.isNullOrBlank()) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // ✅ [변경] 하단 버튼을 Row로 나눠서 "삭제/저장" 반반 배치
                Row( // ✅ ADDED
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton( // ✅ ADDED
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        enabled = currentTask != null && !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = "삭제", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val base = currentTask ?: return@Button
                            val updated = base.copy(
                                title = title,
                                description = description,
                                dueDate = finalDueDate,
                                priority = selectedPriority,
                                status = selectedStatus,
                                precursorTaskId = selectedPrecursorTaskId
                            )
                            taskViewModel.updateTask(updated)
                            onTaskUpdated()
                        },
                        modifier = Modifier
                            .weight(1f) // ✅ CHANGED (fillMaxWidth -> weight)
                            .fillMaxHeight(),
                        enabled = isSaveEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaskFlowGreen,
                            disabledContainerColor = TaskFlowGreen.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Text(text = "저장", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                } // ✅ ADDED END

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * AddTaskScreen 스타일 입력 박스 재사용
 */
@Composable
private fun TaskFlowInputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean,
    modifier: Modifier = Modifier,
    minHeight: Dp = 0.dp,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = Color.Black,
        fontSize = 14.sp
    )
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = textStyle,
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .background(InputBackground, RoundedCornerShape(10.dp))
                    .then(if (minHeight > 0.dp) Modifier.heightIn(min = minHeight) else Modifier)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = textStyle.copy(color = Color.Gray))
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun DateSelectField(
    selectedDate: Date?,
    modifier: Modifier,
    onDateSelected: (Date?) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    selectedDate?.let { calendar.time = it }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newCalendar = Calendar.getInstance().apply {
                time = selectedDate ?: Date()
                set(year, month, dayOfMonth)
            }
            onDateSelected(newCalendar.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val dateText = selectedDate?.let {
        SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(it)
    } ?: "날짜"

    Box(modifier = modifier) {
        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { datePickerDialog.show() }
        )
    }
}

@Composable
private fun TimeSelectField(
    selectedTime: Date?,
    modifier: Modifier,
    onTimeSelected: (Date?) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    selectedTime?.let { calendar.time = it }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val newCalendar = Calendar.getInstance().apply {
                time = selectedTime ?: Date()
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            onTimeSelected(newCalendar.time)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    val timeText = selectedTime?.let {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
    } ?: "시간"

    Box(modifier = modifier) {
        OutlinedTextField(
            value = timeText,
            onValueChange = {},
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { timePickerDialog.show() }
        )
    }
}
