package com.ippo.taskflow.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern // 🚨 정규식 패턴을 위한 Import 추가

// 이메일 유효성 검사용 정규식 패턴 (ViewModel 외부에서 정의)
private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

class AuthViewModel : ViewModel() {

    // 1. UI 상태 및 유효성 검사 상태 관리 추가
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isAuthenticated = MutableStateFlow(Firebase.auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // 💡 폼 유효성 에러 상태 추가
    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError

    val userId: String?
        get() = Firebase.auth.currentUser?.uid

    // 2. 비즈니스 로직: Email/PW 로그인 및 익명 로그인 폴백 통합
    fun signIn(email: String, password: String) {
        // 입력 유효성 검사 상태 초기화
        _emailError.value = null
        _passwordError.value = null

        // 🚨 1단계: 유효성 검사 (PM 결정: 둘 다 비어있을 때만 익명 로그인 허용)
        if (email.isBlank() && password.isBlank()) {
            // 둘 다 비어있으면 익명 로그인 폴백 호출
            signInAsGuest()
            return
        }

        // 🚨 2단계: Email/PW 형식 검사 및 에러 메시지 업데이트
        if (!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
            _emailError.value = "유효하지 않은 이메일 형식입니다."
            return
        }
        if (password.length < 6) {
            _passwordError.value = "비밀번호는 최소 6자 이상이어야 합니다."
            return
        }

        // 🚨 3단계: Firebase 이메일/PW 로그인 시도
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Firebase: 이메일과 비밀번호로 로그인 시도
                val result = Firebase.auth.signInWithEmailAndPassword(email, password).await()

                _isAuthenticated.value = result.user != null
                _isLoading.value = false

            } catch (e: Exception) {
                // 로그인 실패 (비밀번호 불일치, 사용자 없음 등)
                _error.value = "로그인 실패: 사용자 정보를 확인해주세요."
                _isAuthenticated.value = false
                _isLoading.value = false
            }
        }
    }

    // 3. 비즈니스 로직: 익명 로그인 처리 (기존 코드 유지)
    fun signInAsGuest() {
        _isLoading.value = true
        _error.value = null

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

    // 4. 로그아웃 로직 (기존 코드 유지)
    fun signOut() {
        Firebase.auth.signOut()
        _isAuthenticated.value = false
    }

    // 5. 오류 초기화 로직 (기존 코드 유지)
    fun clearError() {
        _error.value = null
    }
}