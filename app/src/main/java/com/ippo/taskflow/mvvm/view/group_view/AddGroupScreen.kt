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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
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

// 메인 색상 (다른 화면과 통일)
private val TaskFlowGreen = Color(0xFF1E8A3B)
private val TaskFlowLightGreen = Color(0xFFE0FFE8)

@Composable
fun AddGroupScreen(
    groupViewModel: com.ippo.taskflow.mvvm.view_model.group.GroupViewModel,
    authViewModel: com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel,
    onGroupAdded: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToMain: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    // UI 상태 (이름/설명/이메일 입력값 등)는 ViewModel 말고 화면 로컬 상태로 관리
    var groupName by rememberSaveable { mutableStateOf("") }
    var groupDescription by rememberSaveable { mutableStateOf("") }

    var inviteEmail by rememberSaveable { mutableStateOf("") }
    var invitedEmails by rememberSaveable { mutableStateOf(listOf<String>()) }
    var emailError by rememberSaveable { mutableStateOf<String?>(null) }

    val isLoading by groupViewModel.isLoading.collectAsState()
    val groupError by groupViewModel.error.collectAsState()

    val isCreateEnabled = groupName.isNotBlank() // 생성자 본인은 자동 멤버이므로 이것만 체크

    Scaffold(
        bottomBar = {
            AddGroupBottomNavBar(
                onHomeClick = onNavigateToMain,
                onGroupsClick = onNavigateToGroups,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
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
                    placeholder = "Task 이름을 입력하세요.",
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

                // 멤버 이메일 추가 영역 (UI + authViewModel.getUidByEmail 활용)
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

                            // 이메일로 UID 검색 → 성공하면 invitedEmails 목록에 추가
                            authViewModel.getUidByEmail(email) { uid ->
                                if (uid != null) {
                                    // 이미 추가된 이메일이면 중복 추가 방지
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
                        // 현재 GroupViewModel.createGroup(name, description)은
                        // 생성자 본인만 멤버로 추가하므로, 초대한 이메일들은
                        // 추후 GroupDetailScreen 등에서 inviteMemberByEmail로 처리 예정.
                        groupViewModel.createGroup(
                            name = groupName,
                            description = groupDescription
                        )

                        // 현재 구조상 성공 콜백이 없어서, 우선 즉시 돌아가게 처리
                        onGroupAdded()
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

/**
 * Figma 스타일에 맞춘 공통 입력 박스 컴포넌트
 * - BasicTextField 기반
 * - 배경 연회색, 라운드, 포커스 시 스타일 변화 없음
 * - placeholder 직접 구현
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
    ),
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = textStyle,
        modifier = modifier
            .fillMaxWidth(),
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
                        style = textStyle.copy(
                            color = Color.Gray
                        )
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

@Composable
private fun AddGroupBottomNavBar(
    onHomeClick: () -> Unit,
    onGroupsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TaskFlowLightGreen)
                .padding(horizontal = 40.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHomeClick) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Home",
                    tint = Color.Black
                )
            }
            IconButton(onClick = onGroupsClick) {
                Icon(
                    imageVector = Icons.Filled.Group,
                    contentDescription = "Groups",
                    tint = TaskFlowGreen   // Group 탭 강조
                )
            }
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Profile",
                    tint = Color.Black
                )
            }
        }
    }
}
