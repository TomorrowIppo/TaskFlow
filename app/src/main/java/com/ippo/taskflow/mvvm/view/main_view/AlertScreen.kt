package com.ippo.taskflow.mvvm.view.main_view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.mvvm.view_model.auth.AuthViewModel
import com.ippo.taskflow.mvvm.view_model.notification.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertScreen(
    authViewModel: AuthViewModel,
    notificationViewModel: NotificationViewModel,
    onBack: () -> Unit,

    // ✅ 하단바 네비게이션 추가
    onNavigateToMain: () -> Unit,
    onNavigateToGroups: () -> Unit,
    onNavigateToProfile: () -> Unit,
) {
    val uid = authViewModel.userId
    val state by notificationViewModel.state.collectAsState()

    LaunchedEffect(uid) {
        if (uid != null) notificationViewModel.start(uid)
    }

    Scaffold(
        containerColor = Color.White, // ✅ 배경 흰색
        topBar = {
            TopAppBar(
                title = { Text("알림", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            // ✅ Main/Setting과 동일 하단바
            TaskFlowBottomNavBar(
                onHomeClick = onNavigateToMain,
                onMainClick = onNavigateToGroups,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {

            if (uid == null) {
                Text("로그인이 필요합니다.", modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                return@Box
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.items) { n ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { notificationViewModel.markRead(uid, n.id) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = n.message,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = n.dateText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Divider(modifier = Modifier.padding(top = 12.dp))
                    }
                }
            }
        }
    }
}

// ----------------------
// Preview (VM 없이 확인용)
// ----------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertScreenPreviewContent() {
    val dummyNotifications = listOf(
        Triple("지수님이 오늘의 할 일을 모두 완료했습니다!", "2025. 3. 19.", false),
        Triple("“책 읽기” 완료일이 3일 남았습니다.", "2025. 3. 18.", false),
        Triple("준하님이 오늘의 할 일을 모두 완료했습니다!", "2025. 3. 15.", true)
    )

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("알림", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            TaskFlowBottomNavBar(
                onHomeClick = {},
                onMainClick = {},
                onProfileClick = {}
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(dummyNotifications) { (message, date, _) ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Divider(modifier = Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertScreenPreview() {
    MaterialTheme {
        AlertScreenPreviewContent()
    }
}