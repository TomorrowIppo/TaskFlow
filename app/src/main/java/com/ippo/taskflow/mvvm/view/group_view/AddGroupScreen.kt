package com.ippo.taskflow.mvvm.view.group_view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.activity.ui.theme.TaskFlowGreen
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel

@Composable
fun AddGroupScreen(
    groupViewModel: GroupViewModel,
    authViewModel: AuthViewModel,
    onGroupCreated: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }

    var inviteEmail by remember { mutableStateOf("") }
    var emailList by remember { mutableStateOf(emptyList<String>()) }
    var emailError by remember { mutableStateOf<String?>(null) }

    val isLoading by groupViewModel.isLoading.collectAsState()
    val groupError by groupViewModel.error.collectAsState()
    val isSuccess by groupViewModel.groupCreationSuccess.collectAsState()

    // [추가] 그룹 생성 직후 groupId 받아오기 (초대 처리용)
    val createdGroupId by groupViewModel.createdGroupId.collectAsState()

    // [수정] 그룹 생성 성공 + groupId 확보되면, emailList를 실제 memberUids에 추가
    LaunchedEffect(isSuccess, createdGroupId) {
        if (isSuccess && !createdGroupId.isNullOrBlank()) {
            val gid = createdGroupId!!

            // [추가] 초대 이메일들을 실제 멤버로 추가
            emailList.forEach { email ->
                groupViewModel.inviteMemberByEmail(gid, email)
            }

            onGroupCreated()
            groupViewModel.resetGroupCreationStatus()
        }
    }

    val canCreate = groupName.trim().isNotBlank() && !isLoading

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
                TopBar(onBackClick = onNavigateBack)

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "새로운 Group 추가하기",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Group 이름
                Text(
                    text = "Group 이름",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                InputBox(
                    value = groupName,
                    onValueChange = { groupName = it },
                    placeholder = "Group 이름을 입력하세요."
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Group 설명
                Text(
                    text = "설명",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                InputBox(
                    value = groupDescription,
                    onValueChange = { groupDescription = it },
                    placeholder = "Group 설명을 입력하세요.",
                    minHeight = 100.dp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 멤버 초대
                Text(
                    text = "멤버 이메일로 초대",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InputBox(
                        value = inviteEmail,
                        onValueChange = { inviteEmail = it },
                        placeholder = "이메일을 입력하세요.",
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    // 이메일로 멤버 초대 관련 로직 AI 도움
                    Button(
                        onClick = {
                            val email = inviteEmail.trim()
                            if (email.isBlank()) return@Button

                            authViewModel.getUidByEmail(email) { uid ->
                                if (uid != null) {
                                    if (!emailList.contains(email)) {
                                        emailList = emailList + email
                                    }
                                    inviteEmail = ""
                                    emailError = null
                                } else {
                                    emailError = "해당 이메일의 사용자를 찾을 수 없습니다."
                                }
                            }
                        },
                        enabled = inviteEmail.trim().isNotBlank() && !isLoading,
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

                if (emailList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "추가된 멤버 (${emailList.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(emailList, key = { it }) { email ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = email,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(
                                        onClick = {
                                            emailList = emailList.filterNot { it == email }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "삭제",
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!groupError.isNullOrBlank()) {
                    Text(
                        text = groupError!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        groupViewModel.createGroup(
                            name = groupName.trim(),
                            description = groupDescription.trim()
                        )
                        // 이제는 "생성 성공 후" LaunchedEffect에서 emailList를 실제 멤버로 추가함
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = canCreate,
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

@Composable
private fun TopBar(
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
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
private fun InputBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
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
        textStyle = textStyle,
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .background(Color(0xFFF7F7F7), RoundedCornerShape(10.dp))
                    .then(
                        if (minHeight > 0.dp)
                            Modifier.heightIn(min = minHeight)
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
