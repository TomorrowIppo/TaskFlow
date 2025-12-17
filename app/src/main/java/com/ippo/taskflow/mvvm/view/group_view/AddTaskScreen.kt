package com.ippo.taskflow.mvvm.view.group_view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.ippo.taskflow.mvvm.model.User // ✅ 추가
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTaskScreen(
    initialGroupId: String,
    taskViewModel: TaskViewModel,
    groupViewModel: GroupViewModel,
    onTaskCreated: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val isTaskLoading by taskViewModel.isLoading.collectAsState()
    val taskError by taskViewModel.error.collectAsState()
    val currentGroupTasks by taskViewModel.taskList.collectAsState()

    // ✅ 추가: 그룹 멤버 목록 Observe
    val groupMembers by groupViewModel.currentGroupMembers.collectAsState()

    // ✅ 추가: 최초 진입 시 precursor 후보/멤버 후보 로드
    LaunchedEffect(initialGroupId) {
        taskViewModel.loadTasks(initialGroupId)
        groupViewModel.loadGroupDetail(initialGroupId)
    }

    var taskName by rememberSaveable { mutableStateOf("") }
    var taskDescription by rememberSaveable { mutableStateOf("") }
    var selectedPriority by rememberSaveable { mutableStateOf(2) }
    var expandedPriority by rememberSaveable { mutableStateOf(false) }
    var selectedPrecursorTask by remember { mutableStateOf<Task?>(null) }
    var expandedPrecursor by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedTime by remember { mutableStateOf<Date?>(null) }

    // ✅ 추가: 담당자 선택 상태
    var selectedAssignee by remember { mutableStateOf<User?>(null) }
    var expandedAssignee by remember { mutableStateOf(false) }

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

    Scaffold { innerPadding ->
        Surface(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "뒤로가기") }
                    Text(
                        "새로운 Task 추가하기",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text("Task 이름", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                TaskFlowInputBox(taskName, { taskName = it }, "Task 이름을 입력하세요.", true)

                Spacer(Modifier.height(16.dp))

                Text("설명", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                TaskFlowInputBox(taskDescription, { taskDescription = it }, "Task 설명을 입력하세요.", false, minHeight = 100.dp)

                Spacer(Modifier.height(16.dp))

                Text("날짜 / 시간", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(12.dp)) {
                    DateSelectField(selectedDate, Modifier.weight(1f)) { selectedDate = it }
                    TimeSelectField(selectedTime, Modifier.weight(1f)) { selectedTime = it }
                }

                Spacer(Modifier.height(16.dp))

                Text("우선순위", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = when (selectedPriority) { 1 -> "1 (높음)"; 2 -> "2 (보통)"; else -> "3 (낮음)" },
                        onValueChange = {}, readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.rotate(if (expandedPriority) 180f else 0f)) },
                        modifier = Modifier.fillMaxWidth()
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

                Spacer(Modifier.height(16.dp))

                // ✅ 추가: 담당자(그룹 멤버) 선택 UI
                Text("담당자", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Box(Modifier.fillMaxWidth()) {
                    val assigneeLabel = selectedAssignee?.let { "${it.name} (${it.email})" } ?: "없음 (미할당)"
                    OutlinedTextField(
                        value = assigneeLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.rotate(if (expandedAssignee) 180f else 0f)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.matchParentSize().clickable { expandedAssignee = true })

                    DropdownMenu(expandedAssignee, { expandedAssignee = false }) {
                        DropdownMenuItem(
                            text = { Text("없음 (미할당)") },
                            onClick = { selectedAssignee = null; expandedAssignee = false }
                        )

                        // 같은 그룹 멤버 목록에서 선택
                        groupMembers
                            .filter { it.uid.isNotBlank() }
                            .forEach { member ->
                                DropdownMenuItem(
                                    text = { Text("${member.name} (${member.email})") },
                                    onClick = {
                                        selectedAssignee = member
                                        expandedAssignee = false
                                    }
                                )
                            }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text("선행 태스크 (Precursor)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPrecursorTask?.title ?: "없음 (선택 사항)",
                        onValueChange = {}, readOnly = true,
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null, Modifier.rotate(if (expandedPrecursor) 180f else 0f)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.matchParentSize().clickable { expandedPrecursor = true })
                    DropdownMenu(expandedPrecursor, { expandedPrecursor = false }) {
                        DropdownMenuItem(
                            text = { Text("없음") },
                            onClick = { selectedPrecursorTask = null; expandedPrecursor = false }
                        )
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

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        taskViewModel.createTask(
                            groupId = initialGroupId,
                            title = taskName,
                            // ✅ 변경: 선택된 담당자 UID 할당
                            assignedToUid = selectedAssignee?.uid ?: "",
                            dueDate = finalDueDate,
                            priority = selectedPriority,
                            precursorTaskId = selectedPrecursorTask?.taskId
                        )
                        onTaskCreated()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = taskName.isNotBlank() && !isTaskLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = TaskFlowGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isTaskLoading) CircularProgressIndicator(Modifier.size(24.dp), Color.White, strokeWidth = 2.dp)
                    else Text("Task 추가", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

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
        textStyle = MaterialTheme.typography.bodyMedium.copy(Color.Black, 14.sp),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                Modifier
                    .background(InputBackground, RoundedCornerShape(10.dp))
                    .then(if (minHeight > 0.dp) Modifier.heightIn(minHeight) else Modifier)
                    .padding(16.dp, 12.dp)
            ) {
                if (value.isEmpty()) Text(placeholder, style = TextStyle(Color.Gray, 14.sp))
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
    val dialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val res = Calendar.getInstance().apply { set(y, m, d) }
            onDateSelected(res.time)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Box(modifier) {
        OutlinedTextField(
            value = selectedDate?.let { SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(it) } ?: "날짜",
            onValueChange = {},
            readOnly = true,
            leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
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
    val dialog = TimePickerDialog(
        context,
        { _, h, m ->
            val res = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
            }
            onTimeSelected(res.time)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    )

    Box(modifier) {
        OutlinedTextField(
            value = selectedTime?.let { SimpleDateFormat("HH:mm", Locale.getDefault()).format(it) } ?: "시간",
            onValueChange = {},
            readOnly = true,
            leadingIcon = { Icon(Icons.Default.Schedule, null) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.matchParentSize().clickable { dialog.show() })
    }
}
