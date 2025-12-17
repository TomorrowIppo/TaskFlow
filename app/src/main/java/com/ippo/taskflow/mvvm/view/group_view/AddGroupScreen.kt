package com.ippo.taskflow.mvvm.view.group_view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel

// 🚨 임시 색상 정의 (실제 프로젝트 경로에 따라 Import 필요)
val TaskFlowGreen = Color(0xFF69F0AE)
val TaskFlowLightGreen = Color(0xFFB9F6CA)

@Composable
fun AddGroupScreen(
    groupViewModel: GroupViewModel,
    authViewModel: AuthViewModel,
    onTaskCreated: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    // UI 상태 (이름/설명/이메일 입력값 등)
    var groupName by rememberSaveable { mutableStateOf("") }
    var groupDescription by rememberSaveable { mutableStateOf("") }

    var inviteEmail by rememberSaveable { mutableStateOf("") }
    var invitedEmails by rememberSaveable { mutableStateOf(listOf<String>()) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }

    val isLoading by groupViewModel.isLoading.collectAsState()
    val groupError by groupViewModel.error.collectAsState()

    val isCreateEnabled = groupName.isNotBlank()

    // 💡 Side Effect: Group 생성 성공 시 Navigaton
    LaunchedEffect(groupViewModel.groupCreationSuccess) {
        groupViewModel.groupCreationSuccess.collect { isSuccess ->
            if (isSuccess) {
                onTaskCreated()
                groupViewModel.resetGroupCreationStatus()
            }
        }
    }

    // ✅ [수정] 이 화면은 MainAppNavHost에서 bottomBar를 숨기도록 처리됨.
    // 따라서 Screen 내부 Scaffold의 bottomBar는 제거한다.
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
                // 상단바
                AddGroupTopBar(onBackClick = onNavigateBack)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "새로운 Group 추가하기",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Group 이름 필드
                Text(
                    text = "Group 이름",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                TaskFlowInputBox(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = "Group 이름을 입력하세요.",
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Group 설명 필드
                Text(
                    text = "설명",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                TaskFlowInputBox(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    placeholder = "Group 설명을 입력하세요.",
                    singleLine = false,
                    minHeight = 100.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 멤버 이메일 추가 영역
                Text(
                    text = "멤버 이메일로 초대",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TaskFlowInputBox(
                        value = inviteEmail,
                        onValueChange = {
                            inviteEmail = it
                            emailError = null
                        },
                        placeholder = "이메일을 입력하세요.",
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val email = inviteEmail.trim()
                            if (email.isBlank()) return@Button

                            authViewModel.getUidByEmail(email) { uid ->
                                if (uid != null) {
                                    if (!invitedEmails.contains(email)) {
                                        invitedEmails = invitedEmails + email
                                    }
                                    inviteEmail = ""
                                    emailError = null
                                } else {
                                    emailError = "해당 이메일의 사용자를 찾을 수 없습니다."
                                }
                            }
                        },
                        enabled = inviteEmail.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaskFlowGreen,
                            disabledContainerColor = TaskFlowGreen.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(text = "추가", color = Color.White)
                    }
                }

                if (emailError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = emailError!!,
                        color = Color.Red,
                        fontSize = 11.sp
                    )
                }

                if (invitedEmails.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "추가된 멤버 (${invitedEmails.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Group 생성 에러 메시지
                if (!groupError.isNullOrBlank()) {
                    Text(
                        text = groupError!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 하단 여백 밀어내기
                Spacer(modifier = Modifier.weight(1f))

                // Group 추가 버튼
                Button(
                    onClick = {
                        groupViewModel.createGroup(
                            name = groupName,
                            description = groupDescription
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isCreateEnabled && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskFlowGreen,
                        disabledContainerColor = TaskFlowGreen.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Group 추가",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// --- 보조 컴포넌트 (TaskFlowInputBox, AddGroupTopBar, AddGroupBottomNavBar) ---

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
    ),
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
                    .background(Color(0xFFF7F7F7), RoundedCornerShape(10.dp))
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

@Composable
private fun AddGroupTopBar(
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "뒤로가기"
            )
        }
        Text(
            text = "TaskFlow",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}
