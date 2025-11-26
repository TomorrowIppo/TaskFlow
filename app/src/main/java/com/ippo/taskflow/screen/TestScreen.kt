// [새 파일] TestScreen.kt
package com.ippo.taskflow.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ippo.taskflow.group.GroupViewModel // 💡 GroupViewModel Import

@Composable
fun TestScreen(groupViewModel: GroupViewModel) {

    // 1. ViewModel 상태 관찰 (로그 표시용)
    val groups by groupViewModel.groupList.collectAsState()
    val isLoading by groupViewModel.isLoading.collectAsState()
    val error by groupViewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("테스트 스크린", style = MaterialTheme.typography.headlineMedium)

        if (isLoading) {
            Text("로딩 중...")
        } else if (error != null) {
            Text("에러 발생: $error", color = MaterialTheme.colorScheme.error)
        } else {
            Text("로드된 그룹 수: ${groups.size}")
            groups.forEach { group ->
                Text(" - ${group.name} (${group.groupId})")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 🚀 테스트 버튼 (메서드 호출)
        Button(onClick = {
            groupViewModel.loadGroups()
        }, enabled = !isLoading) {
            Text("1. 그룹 목록 로드 테스트")
        }

        Button(onClick = {
            groupViewModel.createGroup(
                name = "Test Group ${System.currentTimeMillis() % 1000}",
                description = "NavHost 테스트용 그룹"
            )
        }, enabled = !isLoading) {
            Text("2. 그룹 생성 테스트")
        }

        // TODO: TaskViewModel 테스트 버튼 추가 (TaskViewModel 통합 시)
    }
}