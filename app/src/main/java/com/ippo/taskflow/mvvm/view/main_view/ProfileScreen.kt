package com.ippo.taskflow.mvvm.view.main_view

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
import androidx.compose.runtime.* // remember, collectAsState, LaunchedEffect Import
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ippo.taskflow.view_model.auth.AuthViewModel // AuthViewModel Import
import com.ippo.taskflow.mvvm.model.User // User 데이터 모델 Import

// 임시 아이콘 Import (실제 프로젝트 아이콘으로 교체 필요)
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Person

/**
 * 프로필 화면 컴포저블.
 * AuthViewModel을 통해 사용자 데이터를 로드하고 편집 액션을 연결합니다.
 * @param authViewModel 사용자 인증 상태 관리를 위한 ViewModel
 * @param onNavigateToSettings 프로필 편집 버튼 클릭 시 호출될 액션
 * @param onNavigateBack 뒤로가기 버튼 클릭 시 호출될 액션
 */
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel, // ✅ ViewModel 주입
    onNavigateToSettings: () -> Unit = {},
    onNavigateBack: () -> Unit = {} // ✅ 뒤로가기 액션 추가
) {
    // 1. ViewModel의 StateFlow를 관찰하여 Composable State로 변환
    val userProfile by authViewModel.profile.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()

    // 2. 에러 메시지 처리를 위한 LaunchedEffect (옵션)
    val error by authViewModel.error.collectAsState()
    LaunchedEffect(error) {
        if (error != null) {
            // Snackbar 등을 통해 사용자에게 에러를 보여줄 수 있습니다.
            // 예시: Log.e("ProfileScreen", "Error: $error")
            authViewModel.clearError() // 에러를 본 후 초기화
        }
    }


    Scaffold(
        topBar = { ProfileTopBar(onNavigateBack = onNavigateBack) }, // ✅ 뒤로가기 액션 전달
        bottomBar = { ProfileBottomBar() },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        ProfileContent(
            modifier = Modifier.padding(paddingValues),
            userProfile = userProfile, // ✅ 동적 사용자 데이터 전달
            onEditProfileClick = onNavigateToSettings,
            isLoading = isLoading
        )
    }
}

// -------------------------------------------------------------
// 상단 앱 바
// -------------------------------------------------------------

@Composable
fun ProfileTopBar(onNavigateBack: () -> Unit) { // ✅ onNavigateBack 인자 추가
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기 버튼 액션 연결
        IconButton(onClick = onNavigateBack) { // ✅ 액션 연결
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
}

// -------------------------------------------------------------
// 하단 내비게이션 바 (변경 없음)
// -------------------------------------------------------------
@Composable
fun ProfileBottomBar() {
    // ... (기존 코드 유지)
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
    // ... (기존 코드 유지)
    val tintColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray

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
// 프로필 내용 영역 (ViewModel 데이터 반영)
// -------------------------------------------------------------

@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    userProfile: User?, // ✅ 사용자 프로필 데이터
    onEditProfileClick: () -> Unit,
    isLoading: Boolean // ✅ 로딩 상태
) {
    if (isLoading && userProfile == null) {
        // 데이터 로딩 중일 때 로딩 인디케이터 표시
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // 사용자 이름 및 상태 메시지 설정
    val displayName = userProfile?.name ?: "Guest User"
    val statusMessage = userProfile?.statusMsg ?: "상태 메시지를 설정해주세요."

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // 1. 프로필 이미지
        // TODO: userProfile에서 실제 프로필 이미지 URL/ID를 가져와야 합니다.
        // 현재는 Placeholder 사용을 가정합니다.
//        ProfileImage(
//            imageResId = R.drawable.profile_placeholder, // R.drawable.profile_placeholder는 프로젝트에 추가해야 합니다.
//            contentDescription = "사용자 프로필 사진"
//        )

        Spacer(Modifier.height(24.dp))

        // 2. 사용자 이름 (ViewModel 데이터 반영)
        Text(
            text = displayName, // ✅ 동적 데이터
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(Modifier.height(4.dp))

        // 3. 상태 메시지 (ViewModel 데이터 반영)
        Text(
            text = statusMessage, // ✅ 동적 데이터
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(Modifier.height(24.dp))

        // 4. 프로필 편집 버튼
        EditProfileButton(onClick = onEditProfileClick)

        Spacer(Modifier.height(48.dp))
    }
}

// -------------------------------------------------------------
// 하위 컴포넌트 (변경 없음)
// -------------------------------------------------------------

@Composable
fun ProfileImage(imageResId: Int, contentDescription: String) {
    // ... (기존 코드 유지: 실제 이미지 리소스로 교체 필요)
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

@Composable
fun EditProfileButton(onClick: () -> Unit) {
    // ... (기존 코드 유지)
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784)),
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
// 프리뷰 (ViewModel 주입을 위해 수정이 필요할 수 있으나, 현재는 제거)
// -------------------------------------------------------------
/*
@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    // ViewModel 주입이 필요 없는 더미 데이터로 Preview를 생성해야 합니다.
    // 현재는 AuthViewModel 인스턴스가 없으므로 Preview에서 ProfileScreen을 직접 호출할 수 없습니다.
    // TaskFlowTheme {
    //     // ProfileScreen() // 이 부분에 오류 발생
    // }
}
*/