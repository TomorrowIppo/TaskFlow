package com.ippo.taskflow.mvvm.view.group_view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddTaskScreen(
    initialGroupId: String,
    taskViewModel: TaskViewModel,
    groupViewModel: GroupViewModel,
    onTaskCreated: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    // ---- ViewModel state observe ----
    val groupList by groupViewModel.groupList.collectAsState()
    val isGroupLoading by groupViewModel.isLoading.collectAsState()
    val groupError by groupViewModel.error.collectAsState()

    val isTaskLoading by taskViewModel.isLoading.collectAsState()
    val taskError by taskViewModel.error.collectAsState()

    // ✅ 선행 Task 후보 목록
    val currentGroupTasks by taskViewModel.taskList.collectAsState()

    // ---- Local UI state ----
    var taskName by rememberSaveable { mutableStateOf("") }
    var taskDescription by rememberSaveable { mutableStateOf("") }

    var selectedGroupId by rememberSaveable { mutableStateOf(initialGroupId) }
    var selectedGroupName by rememberSaveable { mutableStateOf("Task 그룹을 선택하세요") }
    var isGroupDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    // ✅ 우선순위/선행Task 상태
    var selectedPriority by rememberSaveable { mutableStateOf(2) } // 기본 2
    var expandedPriority by rememberSaveable { mutableStateOf(false) }

    var selectedPrecursorTask by rememberSaveable { mutableStateOf<Task?>(null) }
    var expandedPrecursor by rememberSaveable { mutableStateOf(false) }

    // ✅ Date/Time (너가 원하는 컴포넌트 패턴 사용)
    var selectedDate by rememberSaveable { mutableStateOf<Date?>(null) }
    var selectedTime by rememberSaveable { mutableStateOf<Date?>(null) }

    // 최종 dueDate (기존 방식 유지)
    val finalDueDate = selectedDate?.let { date ->
        selectedTime?.let { time ->
            // Date ctor deprecated이긴 한데, 너도 “나중에 수정 예정”이라고 했으니 우선 유지
            Date(date.year, date.month, date.date, time.hours, time.minutes)
        } ?: date
    }

    // 초기 groupName 세팅
    LaunchedEffect(groupList, initialGroupId) {
        val initialGroup = groupList.find { it.groupId == initialGroupId }
        if (initialGroup != null) {
            selectedGroupName = initialGroup.name
        }
    }

    // ✅ 최초 진입 시: initialGroupId의 Task를 로드해서 선행 Task 드롭다운에 사용
    LaunchedEffect(initialGroupId) {
        taskViewModel.loadTasks(initialGroupId)
    }

    // ✅ 그룹을 바꾸면 해당 그룹 task도 로드해서 선행 후보 갱신
    LaunchedEffect(selectedGroupId) {
        if (selectedGroupId.isNotBlank()) {
            taskViewModel.loadTasks(selectedGroupId)
            selectedPrecursorTask = null // 그룹 바뀌면 선행 선택 초기화(안전)
        }
    }

    val isCreateEnabled = taskName.isNotBlank() && selectedGroupId.isNotBlank()

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
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                    Text(
                        text = "새로운 Task 추가하기",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task 이름
                Text(
                    text = "Task 이름",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                TaskFlowInputBox(
                    value = taskName,
                    onValueChange = { taskName = it },
                    placeholder = "Task 이름을 입력하세요.",
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Task 설명
                Text(
                    text = "설명",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                TaskFlowInputBox(
                    value = taskDescription,
                    onValueChange = { taskDescription = it },
                    placeholder = "Task 설명을 입력하세요.",
                    singleLine = false,
                    minHeight = 100.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 날짜 / 시간 (컴포넌트화 + 클릭 안정화 방식)
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

                // Task 그룹 드롭다운 (원래 UI 유지)
                Text(
                    text = "Task 그룹",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(InputBackground)
                        .clickable(enabled = groupList.isNotEmpty()) { isGroupDropdownExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedGroupName,
                            color = if (selectedGroupId.isNotBlank()) Color.Black else Color.Gray
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "그룹 선택", tint = Color.Gray)
                    }

                    DropdownMenu(
                        expanded = isGroupDropdownExpanded,
                        onDismissRequest = { isGroupDropdownExpanded = false }
                    ) {
                        when {
                            isGroupLoading -> DropdownMenuItem(text = { Text("그룹 로딩 중...") }, onClick = {})
                            !groupError.isNullOrBlank() -> DropdownMenuItem(text = { Text("그룹 로드 실패: $groupError") }, onClick = {})
                            groupList.isEmpty() -> DropdownMenuItem(text = { Text("가입된 그룹이 없습니다.") }, onClick = {})
                            else -> groupList.forEach { group ->
                                DropdownMenuItem(
                                    text = { Text(group.name) },
                                    onClick = {
                                        selectedGroupId = group.groupId
                                        selectedGroupName = group.name
                                        isGroupDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ✅ 우선순위 (너가 준 방식: Box + Spacer overlay로 클릭 안정화)
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

                // ✅ 선행 Task (precursor) (너가 준 방식 그대로)
                Text(
                    text = "선행 태스크 (Precursor)",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPrecursorTask?.title ?: "없음 (선택 사항)",
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
                            .clickable(enabled = selectedGroupId.isNotBlank()) { expandedPrecursor = true }
                    )

                    DropdownMenu(
                        expanded = expandedPrecursor,
                        onDismissRequest = { expandedPrecursor = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("없음") },
                            onClick = { selectedPrecursorTask = null; expandedPrecursor = false }
                        )
                        Divider()

                        currentGroupTasks
                            .filter { it.taskId.isNotBlank() && it.title.isNotBlank() }
                            .forEach { task ->
                                DropdownMenuItem(
                                    text = { Text(task.title) },
                                    onClick = { selectedPrecursorTask = task; expandedPrecursor = false }
                                )
                            }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Task 생성 에러 메시지
                if (!taskError.isNullOrBlank()) {
                    Text(text = taskError!!, color = Color.Red, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Task 추가 버튼 (원래 스타일 유지)
                Button(
                    onClick = {
                        val groupId = selectedGroupId

                        taskViewModel.createTask(
                            groupId = groupId,
                            title = taskName,
                            assignedToUid = "", // TODO: 추후 멤버 선택 UI 붙이기
                            dueDate = finalDueDate,
                            priority = selectedPriority,
                            precursorTaskId = selectedPrecursorTask?.taskId
                        )

                        onTaskCreated()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isCreateEnabled && !isTaskLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskFlowGreen,
                        disabledContainerColor = TaskFlowGreen.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Task 추가",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Figma 스타일에 맞춘 공통 입력 박스 (BasicTextField 기반)
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

// -------------------------------------------------------------
// Date/Time Picker (너가 준 “Box + Spacer overlay” 패턴 그대로)
// -------------------------------------------------------------

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
