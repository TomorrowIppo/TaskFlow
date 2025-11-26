package com.ippo.taskflow.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore // 🚨 [추가 1] Firestore Import
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.regex.Pattern

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

    // 1. UI 상태 및 유효성 검사 상태 관리 (기존 유지)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isAuthenticated = MutableStateFlow(Firebase.auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError

    // 🚨 [추가 2] 회원가입용 폼 에러 상태 및 성공 상태
    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError

    private val _isRegistrationSuccessful = MutableStateFlow(false)
    val isRegistrationSuccessful: StateFlow<Boolean> = _isRegistrationSuccessful

    // 🚨 [추가 3] 회원가입 성공 상태 초기화 함수 (RegisterScreen에서 사용 후 호출)
    fun clearRegistrationSuccess() {
        _isRegistrationSuccessful.value = false
    }

    val userId: String?
        get() = Firebase.auth.currentUser?.uid

    // 2. 비즈니스 로직: Email/PW 로그인 및 익명 로그인 폴백 통합 (기존 유지)
    fun signIn(email: String, password: String) {
        // ... (기존 signIn 로직) ...
    }

    // 3. 비즈니스 로직: 익명 로그인 처리 (기존 유지)
    fun signInAsGuest() {
        // ... (기존 signInAsGuest 로직) ...
    }

    // 4. 로그아웃 로직 (기존 유지)
    fun signOut() {
        // ... (기존 signOut 로직) ...
    }

    // 5. 오류 초기화 로직 (기존 유지)
    fun clearError() {
        _error.value = null
    }

    // 🚨 [추가 4] 회원가입 로직: 이름, 이메일, 비밀번호, 비밀번호 확인 처리
    fun registerUser(name: String, email: String, password: String, confirmPassword: String) {
        // 유효성 검사 상태 초기화
        _nameError.value = null
        _emailError.value = null
        _passwordError.value = null
        _confirmPasswordError.value = null

        // 1단계: 이름 유효성 검사
        if (name.isBlank()) {
            _nameError.value = "이름을 입력해주세요."
            return
        }

        // 2단계: 이메일 형식 검사
        if (!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
            _emailError.value = "유효하지 않은 이메일 형식입니다."
            return
        }

        // 3단계: 비밀번호 길이 검사
        if (password.length < 6) {
            _passwordError.value = "비밀번호는 최소 6자 이상이어야 합니다."
            return
        }

        // 4단계: 비밀번호 일치 검사
        if (password != confirmPassword) {
            _confirmPasswordError.value = "비밀번호가 일치하지 않습니다."
            return
        }

        // 유효성 검사 통과 -> Firebase 등록 시도
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                // Firebase Auth: 1. 계정 생성
                val authResult = Firebase.auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: throw IllegalStateException("Firebase UID 생성 실패.")

                // Firebase Firestore: 2. 사용자 프로필 저장 (이름 포함)
                val userProfile = hashMapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "createdAt" to System.currentTimeMillis()
                )
                Firebase.firestore.collection("users").document(uid).set(userProfile).await()

                // 3. 성공 상태 업데이트
                _isAuthenticated.value = true // 자동 로그인 상태로 전환
                _isRegistrationSuccessful.value = true // 회원가입 성공 알림
                _isLoading.value = false

            } catch (e: Exception) {
                // 회원가입 실패 (예: 이미 사용 중인 이메일)
                _error.value = "회원가입 실패: ${e.message}"
                _isAuthenticated.value = false
                _isLoading.value = false
            }
        }
    }
}