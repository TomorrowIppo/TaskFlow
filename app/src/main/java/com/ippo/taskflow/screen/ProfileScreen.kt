package com.ippo.taskflow.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.R
import com.ippo.taskflow.activity.ui.theme.TaskFlowTheme
import com.ippo.taskflow.auth.AuthViewModel
import com.ippo.taskflow.data.User
import com.ippo.taskflow.task.TaskViewModel
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth

private val TaskFlowGreen = Color(0xFF41CC67)
private val TaskFlowLightGreen = Color(0xFFACFFC1)
private val ScreenBg = Color(0xFFFFFFFF)

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    taskViewModel: TaskViewModel,
    onNavigateToSettings: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onNavigateToMain: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    val userProfile by authViewModel.profile.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    val month = remember { YearMonth.now() }
    val monthlyMap by taskViewModel.monthlyCompletion.collectAsState(initial = emptyMap())

    LaunchedEffect(userProfile?.uid, month) {
        val uid = userProfile?.uid ?: return@LaunchedEffect
        taskViewModel.loadMonthlyCompletionForUser(uid, month)
    }

    val error by authViewModel.error.collectAsState()
    LaunchedEffect(error) {
        if (error != null) authViewModel.clearError()
    }

    Scaffold(
        bottomBar = {
            MainBottomNavBar(
                onHomeClick = onNavigateToMain,
                onGroupsClick = onNavigateToGroups,
                onProfileClick = onNavigateToProfile
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        ProfileContent(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(ScreenBg)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            userProfile = userProfile,
            onNavigateBack = onNavigateBack,
            onEditProfileClick = onNavigateToSettings,
            isLoading = isLoading,
            month = month,
            completionByDate = monthlyMap
        )
    }
}

// -------------------------------------------------------------
// 프로필 내용 영역
// -------------------------------------------------------------
@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    userProfile: User?,
    onNavigateBack: () -> Unit,
    onEditProfileClick: () -> Unit,
    isLoading: Boolean,
    month: YearMonth,
    completionByDate: Map<LocalDate, Float>
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Color.Black
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "프로필",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading && userProfile == null) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return
        }

        val displayName = userProfile?.name ?: "Guest User"

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(TaskFlowGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.firstOrNull()?.toString() ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onEditProfileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TaskFlowGreen,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "프로필 편집",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "프로필 편집",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ✅ 월간 수행률 히트맵
        MonthlyTaskHeatmapCard(
            month = month,
            completionByDate = completionByDate
        )

        Spacer(modifier = Modifier.height(18.dp))
    }
}

// -------------------------------------------------------------
// 기존 하위 컴포넌트 (유지 가능)
// -------------------------------------------------------------
@Composable
fun ProfileImage(imageResId: Int, contentDescription: String) {
    val borderModifier = Modifier
        .size(160.dp)
        .clip(CircleShape)
        .border(
            width = 3.dp,
            color = Color.LightGray.copy(alpha = 0.5f),
            shape = CircleShape
        )

    Box(modifier = borderModifier) {
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * ✅ MainScreen과 동일 하단 네비게이션 바 (PNG 아이콘)
 */
@Composable
private fun MainBottomNavBar(
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
                Image(
                    painter = painterResource(R.drawable.ic_home),
                    contentDescription = "Home",
                    modifier = Modifier.size(50.dp)
                )
            }

            IconButton(onClick = onGroupsClick) {
                Image(
                    painter = painterResource(R.drawable.ic_taskflow),
                    contentDescription = "Groups",
                    modifier = Modifier.size(50.dp)
                )
            }

            IconButton(onClick = onProfileClick) {
                Image(
                    painter = painterResource(R.drawable.ic_profile),
                    contentDescription = "Profile",
                    modifier = Modifier.size(50.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Heatmap UI (파일 내 포함 버전)
// -------------------------------------------------------------
@Composable
fun MonthlyTaskHeatmapCard(
    month: YearMonth,
    completionByDate: Map<LocalDate, Float>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "${month.month.name.take(3)} ${month.year}",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            MonthlyTaskHeatmapGrid(
                month = month,
                completionByDate = completionByDate
            )
        }
    }
}

@Composable
private fun MonthlyTaskHeatmapGrid(
    month: YearMonth,
    completionByDate: Map<LocalDate, Float>
) {
    val firstDay = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()
    val firstDowIndex = ((firstDay.dayOfWeek.value + 6) % 7) // MON=0 ... SUN=6

    val weekLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = Color.Gray,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val totalCells = firstDowIndex + daysInMonth
        val rows = ((totalCells + 6) / 7)

        var dayNum = 1

        repeat(rows) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val isEmpty = cellIndex < firstDowIndex || dayNum > daysInMonth

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isEmpty) {
                            DayDot(Color.Transparent)
                        } else {
                            val date = month.atDay(dayNum)
                            val rate = completionByDate[date] ?: -1f
                            DayDot(rateToColor(rate))
                            dayNum++
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayDot(color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .clip(CircleShape)
            .background(color)
    )
}

private fun rateToColor(rate: Float): Color {
    return when {
        rate < 0f -> Color.LightGray.copy(alpha = 0.25f) // 데이터 없음
        rate < 0.25f -> Color.LightGray.copy(alpha = 0.45f)
        rate < 0.50f -> Color(0xFFFFE08A)
        rate < 0.75f -> Color(0xFFFFC24A)
        rate < 1.0f -> Color(0xFF9EF3A8)
        else -> TaskFlowGreen
    }
}

// -------------------------------------------------------------
// Preview
// -------------------------------------------------------------
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview_Full() {
    TaskFlowTheme {
        Scaffold(
            bottomBar = {
                MainBottomNavBar(
                    onHomeClick = {},
                    onGroupsClick = {},
                    onProfileClick = {}
                )
            }
        ) { padding ->
            ProfileContent(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(ScreenBg)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                userProfile = User(
                    uid = "preview_uid",
                    name = "희주",
                    statusMsg = "오늘도 TaskFlow로 정리하는 중"
                ),
                onNavigateBack = {},
                onEditProfileClick = {},
                isLoading = false,
                month = YearMonth.now(),
                completionByDate = previewHeatMapStrong()
            )
        }
    }
}

private fun previewHeatMapStrong(): Map<LocalDate, Float> {
    val ym = YearMonth.now()
    return mapOf(
        ym.atDay(1) to 1.0f,
        ym.atDay(2) to 1.0f,
        ym.atDay(3) to 0.7f,
        ym.atDay(4) to 0.4f,
        ym.atDay(5) to 0.2f,
        ym.atDay(6) to 1.0f,
        ym.atDay(9) to 0.9f,
        ym.atDay(12) to 0.6f,
        ym.atDay(18) to 1.0f
    )
}