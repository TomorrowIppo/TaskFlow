package com.ippo.taskflow.mvvm.view.group_view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.activity.ui.theme.TaskFlowGreen
import com.ippo.taskflow.activity.ui.theme.TaskFlowLightGreen
import com.ippo.taskflow.mvvm.model.Group
import com.ippo.taskflow.mvvm.view_model.group.GroupViewModel


@Composable
fun GroupTaskScreen(
    groupViewModel: GroupViewModel,
    onNavigateToMain: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToAddGroup: () -> Unit,
    onNavigateToGroupDetail: (groupId: String) -> Unit,
) {
    // ViewModel State Observe
    val groupList by groupViewModel.groupList.collectAsState()
    val isLoading by groupViewModel.isLoading.collectAsState()
    val errorMessage by groupViewModel.error.collectAsState()

    // 첫 진입 시 그룹 목록 로드
    LaunchedEffect(Unit) {
        groupViewModel.loadGroups()
    }

    Scaffold(
        bottomBar = {
            GroupBottomNavBar(
                onHomeClick = onNavigateToMain,
                onGroupsClick = { /* 현재 화면 → No-op */ },
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
                GroupTopBar(
                    onBackClick = onNavigateToMain // 상단 뒤로가기 → 메인으로
                )

                Spacer(modifier = Modifier.height(8.dp))

                // "내 그룹" 헤더 + 새 그룹 텍스트 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "내 그룹",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = "+ 새 그룹",
                        color = TaskFlowGreen,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onNavigateToAddGroup() }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 에러 표시
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 그룹 목록 + "새로운 그룹 만들기" 카드
                GroupListSection(
                    groups = groupList,
                    isLoading = isLoading,
                    onGroupClick = { groupId ->
                        onNavigateToGroupDetail(groupId)
                    },
                    onAddGroupClick = onNavigateToAddGroup
                )
            }
        }
    }
}

@Composable
private fun GroupTopBar(
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
private fun GroupListSection(
    groups: List<Group>,
    isLoading: Boolean,
    onGroupClick: (String) -> Unit,
    onAddGroupClick: () -> Unit,
) {
    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "그룹 목록을 불러오는 중...", color = Color.Gray)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        items(groups, key = { it.groupId }) { group ->
            GroupCard(
                group = group,
                onClick = { onGroupClick(group.groupId) }
            )
        }

        // 하단 "새로운 그룹 만들기" 카드
        item {
            Spacer(modifier = Modifier.height(8.dp))
            AddGroupCard(
                onClick = onAddGroupClick
            )
        }
    }
}

@Composable
private fun GroupCard(
    group: Group,
    onClick: () -> Unit,
) {
    // 🔸 현재는 Task 통계 계산 로직이 ViewModel에 없으므로 placeholder 값 사용
    val memberCount = group.memberUids.size
    val inProgressCount = 0        // TODO: ViewModel에서 실제 진행 Task 개수 연동
    val completionRatio = 0f       // TODO: ViewModel에서 완료율(0f~1f) 연동

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // 상단: 그룹 이름 + 멤버 수
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = group.name.ifBlank { "이름 없는 그룹" },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = "멤버 수",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = memberCount.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 중간: 진행 Task 정보 (placeholder)
            Text(
                text = "${inProgressCount}개 진행 중",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 진행률 바
            LinearProgressIndicator(
                progress = completionRatio,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = TaskFlowGreen,
                trackColor = Color(0xFFE5E5E5)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 완료율 텍스트 (오른쪽 정렬 느낌)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${(completionRatio * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun AddGroupCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(TaskFlowLightGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = TaskFlowGreen,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "새로운 그룹 만들기",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFF7A7A7A)
                    )
                )
            }
        }
    }
}

@Composable
private fun GroupBottomNavBar(
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
                    tint = TaskFlowGreen // 현재 선택된 탭
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


