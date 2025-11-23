package com.ippo.taskflow.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// 💡 여기서는 Repository 패턴을 생략하고 ViewModel에서 Firebase를 직접 호출함
//    (나중에는 AuthRepository를 만들어야 더 깔끔함)

class AuthViewModel : ViewModel() {

    // 1. UI 상태: 로딩 중인지, 사용자 ID는 무엇인지 등을 관리
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isAuthenticated = MutableStateFlow(Firebase.auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 초기 사용자 ID 설정
    val userId: String?
        get() = Firebase.auth.currentUser?.uid


    // 2. 비즈니스 로직: 익명 로그인 처리
    fun signInAsGuest() {
        _isLoading.value = true
        _error.value = null

        // 코루틴으로 비동기 작업 처리
        viewModelScope.launch {
            try {
                val result = Firebase.auth.signInAnonymously().await()
                _isAuthenticated.value = result.user != null
                _isLoading.value = false

            } catch (e: Exception) {
                _error.value = e.message
                _isAuthenticated.value = false
                _isLoading.value = false
            }
        }
    }

    // 3. 로그아웃 로직
    fun signOut() {
        Firebase.auth.signOut()
        _isAuthenticated.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
// 🚨 주의: .await()를 사용하려면 Firebase-ktx 라이브러리 추가가 필요함!