package com.ippo.taskflow

import android.os.Bundle
import android.widget.Toast // Toast import 추가
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.ippo.taskflow.ui.theme.TaskFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TaskFlowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Greeting 대신 AuthScreen 컴포저블 사용
                    AuthScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * 익명 로그인을 처리하고 상태에 따라 다른 화면을 보여주는 컴포저블
 */
@Composable
fun AuthScreen(modifier: Modifier = Modifier) {
    // Firebase Auth 인스턴스 가져오기
    val auth = Firebase.auth
    val context = LocalContext.current

    // 로그인 상태를 관리하는 State. null이면 로그아웃, 아니면 사용자 ID
    var userId by remember { mutableStateOf(auth.currentUser?.uid) }

    // 로딩 상태를 관리하는 State
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (userId == null) {
            // 로그아웃 상태일 때: 게스트 로그인 버튼 표시
            Text(text = "게스트로 로그인하여 시작하세요.")
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator() // 로딩 중 표시
            } else {
                Button(onClick = {
                    isLoading = true // 버튼 클릭 시 로딩 시작
                    auth.signInAnonymously()
                        .addOnCompleteListener { task ->
                            isLoading = false // 작업 완료 시 로딩 종료
                            if (task.isSuccessful) {
                                // 로그인 성공
                                userId = auth.currentUser?.uid
                                Toast.makeText(context, "익명 로그인 성공!", Toast.LENGTH_SHORT).show()
                            } else {
                                // 로그인 실패
                                userId = null
                                Toast.makeText(context, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }) {
                    Text("게스트 로그인")
                }
            }
        } else {
            // 로그인 상태일 때: 환영 메시지와 로그아웃 버튼 표시
            Greeting(name = "Guest ($userId)", modifier = Modifier.padding(16.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                auth.signOut() // 로그아웃 처리
                userId = null
                Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
            }) {
                Text("로그아웃")
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "환영합니다, $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    TaskFlowTheme {
        AuthScreen()
    }
}