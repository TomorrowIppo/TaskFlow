package com.ippo.taskflow.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.R // R 파일 경로는 프로젝트 설정에 따라 달라질 수 있습니다.
import com.ippo.taskflow.auth.AuthViewModel // ViewModel이 필요하다면 Import
import com.ippo.taskflow.activity.ui.theme.TaskFlowTheme // 테마 Import

// 임시 아이콘 사용을 위한 Import (실제 프로젝트 아이콘으로 교체 필요)
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person

/**
 * 프로필 화면 컴포저블.
 * @param authViewModel 사용자 인증 상태 관리를 위한 ViewModel (필요시)
 * @param onNavigateToSettings 프로필 편집 버튼 클릭 시 호출될 액션
 */
@Composable
fun ProfileScreen(
    // ViewModel은 현재 화면 디자인에 직접적인 상태 관리가 없으므로 주석 처리하거나 필요에 따라 사용
    // authViewModel: AuthViewModel,
    onNavigateToSettings: () -> Unit = {} // 프로필 편집 버튼 액션
) {
    Scaffold(
        topBar = { ProfileTopBar() },
        bottomBar = { ProfileBottomBar() },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        ProfileContent(
            modifier = Modifier.padding(paddingValues),
            onEditProfileClick = onNavigateToSettings
        )
    }
}

// -------------------------------------------------------------
// 상단 앱 바
// -------------------------------------------------------------

@Composable
fun ProfileTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기 버튼
        IconButton(onClick = { /* 뒤로가기 액션 */ }) {
            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
        }
        Spacer(modifier = Modifier.width(8.dp))
        // 제목
        Text(
            text = "프로필",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
    // 구분선 (옵션)
    // Divider(color = Color.LightGray.copy(alpha = 0.5f))
}

// -------------------------------------------------------------
// 하단 내비게이션 바 (3개 버튼)
// -------------------------------------------------------------

@Composable
fun ProfileBottomBar() {
    // 배경색: 디자인에 따라 연한 녹색/흰색으로 설정
    val barColor = Color(0xFFC8E6C9) // 예시 연한 녹색 (Material Color: Light Green 200)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(barColor),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: 실제 프로젝트의 아이콘 및 클릭 액션으로 교체하세요.
        BottomNavItem(icon = Icons.Default.Home, label = "홈", onClick = { /* 홈 이동 */ })
        BottomNavItem(icon = Icons.Default.Settings, label = "설정", onClick = { /* 설정 이동 */ })
        BottomNavItem(icon = Icons.Default.Person, label = "프로필", onClick = { /* 현재 화면 */ }, isSelected = true)
    }
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, onClick: () -> Unit, isSelected: Boolean = false) {
    val tintColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray

    // 디자인을 최대한 유사하게 구현하기 위해 Icon과 Modifier를 사용
    IconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(28.dp)
        )
    }
}


// -------------------------------------------------------------
// 프로필 내용 영역 (스트릭 제거)
// -------------------------------------------------------------

@Composable
fun ProfileContent(modifier: Modifier = Modifier, onEditProfileClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // 1. 프로필 이미지
//        ProfileImage(
//            imageResId = R.drawable.profile_placeholder, // R.drawable.profile_placeholder는 사용자가 직접 추가해야 합니다.
//            contentDescription = "사용자 프로필 사진"
//        )

        Spacer(Modifier.height(24.dp))

        // 2. 사용자 이름
        Text(
            text = "김연아",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(Modifier.height(4.dp))

        // 3. 상태 메시지
        Text(
            text = "안녕!",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(24.dp))

        // 4. 프로필 편집 버튼
        EditProfileButton(onClick = onEditProfileClick)

        // 스트릭(달력) 기능이 제거되므로, 아래에 공간을 추가하여 균형을 맞춥니다.
        Spacer(Modifier.height(48.dp))

        // 🚨 스트릭 달력 영역 제거됨
    }
}

// -------------------------------------------------------------
// 하위 컴포넌트
// -------------------------------------------------------------

@Composable
fun ProfileImage(imageResId: Int, contentDescription: String) {
    // 테두리를 위한 Modifier
    val borderModifier = Modifier
        .size(160.dp)
        .clip(CircleShape)
        .border(
            width = 3.dp,
            color = Color.LightGray.copy(alpha = 0.5f), // 테두리 색상
            shape = CircleShape
        )

    // 실제 이미지 (R.drawable.profile_placeholder 이미지를 프로젝트에 추가해야 합니다)
    // 현재 R.drawable.profile_placeholder가 없으므로 임시로 Material Icon 사용을 가정합니다.
    // **실제로는 painterResource(id = imageResId)**를 사용해야 합니다.
    Box(modifier = borderModifier) {
        //
        // TODO: 실제 프로젝트의 R.drawable.image_name으로 교체하세요.
        Image(
            painter = painterResource(id = imageResId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun EditProfileButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp), // 둥근 모서리
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)), // 연한 녹색
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Icon(
            Icons.Default.Edit,
            contentDescription = "프로필 편집",
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("프로필 편집", color = Color.White, fontWeight = FontWeight.SemiBold)
    }
}

// -------------------------------------------------------------
// 프리뷰
// -------------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    // ⚠️ 경고: R.drawable.profile_placeholder는 Preview에서 미리 볼 수 없습니다.
    // 해당 리소스를 프로젝트에 추가해야 에러 없이 동작합니다.
    TaskFlowTheme {
        ProfileScreen()
    }
}