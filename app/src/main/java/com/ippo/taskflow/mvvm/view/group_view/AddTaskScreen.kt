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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel
import com.ippo.taskflow.mvvm.view_model.task.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// 공통 색상 (다른 화면과 통일)
private val TaskFlowGreen = Color(0xFF1E8A3B)
private val TaskFlowLightGreen = Color(0xFFE0FFE8)
private val InputBackground = Color(0xFFF7F7F7)

@Composable
fun AddTaskScreen(
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

    // ---- Local UI state ----
    var taskName by rememberSaveable { mutableStateOf("") }
    var taskDescription by rememberSaveable { mutableStateOf("") }

    val calendar = rememberSaveable { Calendar.getInstance() }
    var selectedDateText by rememberSaveable { mutableStateOf("") }
    var selectedTimeText by rememberSaveable { mutableStateOf("") }
    var dueDate: Date? by rememberSaveable { mutableStateOf<Date?>(null) }

    var selectedGroupId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedGroupName by rememberSaveable { mutableStateOf("Task 그룹을 선택하세요") }
    var isGroupDropdownExpanded by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current

    // 화면 진입 시 그룹 목록 로드
    LaunchedEffect(Unit) {
        groupViewModel.loadGroups()
    }

    val isCreateEnabled = taskName.isNotBlank() && selectedGroupId != null

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
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
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
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
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
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
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

                // 날짜 / 시간
                Text(
                    text = "날짜 / 시간",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 날짜 선택
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(InputBackground)
                            .clickable {
                                val year = calendar.get(Calendar.YEAR)
                                val month = calendar.get(Calendar.MONTH)
                                val day = calendar.get(Calendar.DAY_OF_MONTH)

                                DatePickerDialog(
                                    context,
                                    { _, y, m, d ->
                                        calendar.set(Calendar.YEAR, y)
                                        calendar.set(Calendar.MONTH, m)
                                        calendar.set(Calendar.DAY_OF_MONTH, d)
                                        dueDate = calendar.time
                                        selectedDateText = SimpleDateFormat(
                                            "yyyy.MM.dd",
                                            Locale.getDefault()
                                        ).format(calendar.time)
                                    },
                                    year, month, day
                                ).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (selectedDateText.isNotEmpty())
                                    selectedDateText else "날짜",
                                color = if (selectedDateText.isNotEmpty())
                                    Color.Black else Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "날짜 선택",
                                tint = Color.Gray
                            )
                        }
                    }

                    // 시간 선택
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(InputBackground)
                            .clickable {
                                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                                val minute = calendar.get(Calendar.MINUTE)

                                TimePickerDialog(
                                    context,
                                    { _, h, m ->
                                        calendar.set(Calendar.HOUR_OF_DAY, h)
                                        calendar.set(Calendar.MINUTE, m)
                                        dueDate = calendar.time
                                        selectedTimeText = SimpleDateFormat(
                                            "HH:mm",
                                            Locale.getDefault()
                                        ).format(calendar.time)
                                    },
                                    hour, minute, true
                                ).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (selectedTimeText.isNotEmpty())
                                    selectedTimeText else "시간",
                                color = if (selectedTimeText.isNotEmpty())
                                    Color.Black else Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "시간 선택",
                                tint = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Task 그룹 드롭다운
                Text(
                    text = "Task 그룹",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(InputBackground)
                        .clickable(enabled = groupList.isNotEmpty()) {
                            isGroupDropdownExpanded = true
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedGroupName,
                            color = if (selectedGroupId != null) Color.Black else Color.Gray
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "그룹 선택",
                            tint = Color.Gray
                        )
                    }

                    DropdownMenu(
                        expanded = isGroupDropdownExpanded,
                        onDismissRequest = { isGroupDropdownExpanded = false }
                    ) {
                        if (isGroupLoading) {
                            DropdownMenuItem(
                                text = { Text("그룹 로딩 중...") },
                                onClick = { }
                            )
                        } else if (!groupError.isNullOrBlank()) {
                            DropdownMenuItem(
                                text = { Text("그룹 로드 실패: $groupError") },
                                onClick = { }
                            )
                        } else if (groupList.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("가입된 그룹이 없습니다.") },
                                onClick = { }
                            )
                        } else {
                            groupList.forEach { group ->
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

                Spacer(modifier = Modifier.height(24.dp))

                // Task 생성 에러 메시지
                if (!taskError.isNullOrBlank()) {
                    Text(
                        text = taskError!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Task 추가 버튼
                Button(
                    onClick = {
                        val groupId = selectedGroupId ?: return@Button

                        // description은 현재 TaskViewModel 시그니처에 없어서,
                        // 우선 title, groupId, dueDate, priority만 반영.
                        taskViewModel.createTask(
                            groupId = groupId,
                            title = taskName,
                            assignedToUid = "", // 현재는 할당 사용자 선택 UI가 없으므로 빈 값
                            dueDate = dueDate,
                            priority = 2 // 기본 우선순위
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
                    .then(
                        if (minHeight > 0.dp) Modifier.heightIn(min = minHeight)
                        else Modifier
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.TopStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle.copy(color = Color.Gray)
                    )
                }
                innerTextField()
            }
        }
    )
}



