package com.ippo.taskflow.mvvm.view.group_view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext // Context를 가져오기 위해 필요
import android.app.DatePickerDialog // 네이티브 다이얼로그 클래스
import android.app.TimePickerDialog // 네이티브 다이얼로그 클래스
import java.util.Calendar // 날짜 처리를 위한 Calendar 클래스

import com.ippo.taskflow.activity.ui.theme.TaskFlowGreen
import com.ippo.taskflow.mvvm.model.Task
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    initialGroupId: String,
    taskViewModel: TaskViewModel,
    groupViewModel: GroupViewModel,
    onTaskCreated: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // ViewModel 상태 관찰
    val isLoading by taskViewModel.isLoading.collectAsState()
    val error by taskViewModel.error.collectAsState()

    val currentGroupTasks by taskViewModel.taskList.collectAsState()

    // 데이터 로드: 화면 진입 시 Task 목록을 로드하여 선행 Task 선택에 사용
    LaunchedEffect(initialGroupId) {
        taskViewModel.loadTasks(initialGroupId)
        taskViewModel.clearError()
    }

    // 로컬 상태
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedTime by remember { mutableStateOf<Date?>(null) }
    var assignedTo by remember { mutableStateOf("") }

    var selectedPriority by remember { mutableStateOf(3) }
    var selectedPrecursorTask by remember { mutableStateOf<Task?>(null) }

    // 드롭다운 상태 관리
    var expandedPriority by remember { mutableStateOf(false) }
    var expandedPrecursor by remember { mutableStateOf(false) }

    // 최종 마감일 계산 (Date 생성자 Deprecated 경고는 Date Picker 구현 후 사라질 예정)
    val finalDueDate = selectedDate?.let { date ->
        selectedTime?.let { time ->
            Date(date.year, date.month, date.date, time.hours, time.minutes)
        } ?: date
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("새로운 Task 추가하기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TextField(value = title, onValueChange = { title = it }, label = { Text("Task 이름") }, modifier = Modifier.fillMaxWidth())
                }
                item {
                    TextField(value = description, onValueChange = { description = it }, label = { Text("설명") }, modifier = Modifier.fillMaxWidth())
                }

                // ⭐️ [수정] 날짜 및 시간 선택 (weight 대체 및 Modifer 전달)
                item {
                    Text("날짜 / 시간", fontSize = 14.sp, color = Color.Gray)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        // 50% 공간 할당
                        DateSelectField(selectedDate, Modifier.fillMaxWidth(0.5f)) { selectedDate = it }
                        // 남은 공간 할당
                        TimeSelectField(selectedTime, Modifier.fillMaxWidth(1f)) { selectedTime = it }
                    }
                }

                // ⭐️ [수정] 우선순위 선택 필드 (클릭 영역 안정화)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("우선순위", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = when (selectedPriority) {
                                    1 -> "1 (높음)"; 2 -> "2 (보통)"; 3 -> "3 (낮음)"; else -> "선택"
                                },
                                onValueChange = { /* 읽기 전용 */ }, readOnly = true,
                                trailingIcon = { Icon(Icons.Filled.ArrowBack, contentDescription = "우선순위", modifier = Modifier.rotate(if (expandedPriority) 90f else 270f)) },
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
                                (1..3).forEach { priority ->
                                    DropdownMenuItem(
                                        text = { Text("$priority (${if (priority == 1) "높음" else if (priority == 2) "보통" else "낮음"})") },
                                        onClick = { selectedPriority = priority; expandedPriority = false }
                                    )
                                }
                            }
                        }
                    }
                }

                // ⭐️ [수정] 선행 태스크 선택 필드 (클릭 영역 안정화)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("선행 태스크 (Precursor)", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedPrecursorTask?.title ?: "없음 (선택 사항)", onValueChange = { /* 읽기 전용 */ }, readOnly = true,
                                trailingIcon = { Icon(Icons.Filled.ArrowBack, contentDescription = "선행 태스크", modifier = Modifier.rotate(if (expandedPrecursor) 90f else 270f)) },
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
                                DropdownMenuItem(text = { Text("없음") }, onClick = { selectedPrecursorTask = null; expandedPrecursor = false })
                                Divider()
                                currentGroupTasks.filter { it.taskId.isNotBlank() && it.title.isNotBlank() }.forEach { task ->
                                    DropdownMenuItem(text = { Text(task.title) }, onClick = { selectedPrecursorTask = task; expandedPrecursor = false })
                                }
                            }
                        }
                    }
                }

                item {
                    if (!error.isNullOrBlank()) {
                        Text(text = error!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Task 추가 버튼
            Button(
                onClick = {
                    if (title.isBlank()) { return@Button }
                    taskViewModel.createTask(
                        groupId = initialGroupId, title = title, assignedToUid = assignedTo, dueDate = finalDueDate,
                        priority = selectedPriority, precursorTaskId = selectedPrecursorTask?.taskId
                    )
                    onTaskCreated()
                },
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(50.dp),
                enabled = title.isNotBlank() && !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = TaskFlowGreen)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Task 추가")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ⭐️ [최종 수정] Date/Time Picker 컴포넌트 (Box + Spacer 패턴 적용)
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

    val dateText = selectedDate?.let { SimpleDateFormat("MM/dd", Locale.getDefault()).format(it) } ?: "날짜"

    // ⭐️ [수정] Box로 감싸고 들어온 Modifier를 Box에 적용
    Box(modifier = modifier) {
        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            readOnly = true,
            // ⭐️ [수정] 내부 OutlinedTextField는 fillMaxWidth를 사용하여 Box 크기에 맞춥니다.
            modifier = Modifier.fillMaxWidth()
        )
        // ⭐️ [추가] 투명한 Spacer를 위에 덮어 안정적으로 클릭 감지
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

    val timeText = selectedTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "시간"

    // ⭐️ [수정] Box로 감싸고 들어온 Modifier를 Box에 적용
    Box(modifier = modifier) {
        OutlinedTextField(
            value = timeText,
            onValueChange = {},
            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
            readOnly = true,
            // ⭐️ [수정] 내부 OutlinedTextField는 fillMaxWidth를 사용하여 Box 크기에 맞춥니다.
            modifier = Modifier.fillMaxWidth()
        )
        // ⭐️ [추가] 투명한 Spacer를 위에 덮어 안정적으로 클릭 감지
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .clickable { timePickerDialog.show() }
        )
    }
}