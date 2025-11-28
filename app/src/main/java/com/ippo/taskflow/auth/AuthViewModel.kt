package com.ippo.taskflow.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ippo.taskflow.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.regex.Pattern

// 이메일 유효성 검사용 정규식 패턴
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

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    // ---------------------------------------------------------------------
    // 1. PUBLIC STATE DEFINITIONS (Composables read these)
    // ---------------------------------------------------------------------
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isAuthenticated = MutableStateFlow(auth.currentUser != null)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _profile = MutableStateFlow<User?>(null)
    val profile: StateFlow<User?> = _profile

    // Error States
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError: StateFlow<String?> = _nameError

    private val _confirmPasswordError = MutableStateFlow<String?>(null)
    val confirmPasswordError: StateFlow<String?> = _confirmPasswordError

    private val _isRegistrationSuccessful = MutableStateFlow(false)
    val isRegistrationSuccessful: StateFlow<Boolean> = _isRegistrationSuccessful

    val userId: String?
        get() = auth.currentUser?.uid

    // ---------------------------------------------------------------------
    // 2. INITIALIZATION AND PROFILE LOADING (omitted for brevity in this final step)
    // ---------------------------------------------------------------------

    // ---------------------------------------------------------------------
    // 3. 🔑 PUBLIC ACTION FUNCTIONS
    // ---------------------------------------------------------------------

    /**
     * 🚨 [통합된 로직] Email/PW 로그인 및 익명 로그인 폴백 통합
     */
    fun signIn(email: String, password: String) {
        // 입력 유효성 검사 상태 초기화
        _emailError.value = null
        _passwordError.value = null

        // 1단계: 유효성 검사 (둘 다 비어있을 때만 익명 로그인 허용)
        if (email.isBlank() && password.isBlank()) {
            signInAsGuest()
            return
        }

        // 2단계: Email/PW 형식 검사 및 에러 메시지 업데이트 (간단화)
        if (!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) {
            _emailError.value = "유효하지 않은 이메일 형식입니다."
            return
        }
        if (password.length < 6) {
            _passwordError.value = "비밀번호는 최소 6자 이상이어야 합니다."
            return
        }

        // 3단계: Firebase 이메일/PW 로그인 시도
        _isLoading.value = true
        _error.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                _isAuthenticated.value = authResult.user != null
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "로그인 실패: 사용자 정보를 확인해주세요."
                _isAuthenticated.value = false
                _isLoading.value = false
            }
    }

    /**
     * 익명 로그인 처리 (Guest Login)
     */
    fun signInAsGuest() {
        _isLoading.value = true
        _error.value = null

        auth.signInAnonymously()
            .addOnSuccessListener { authResult ->
                _isAuthenticated.value = authResult.user != null
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _error.value = e.message
                _isAuthenticated.value = false
                _isLoading.value = false
            }
    }

    /**
     * 사용자 회원가입 및 Firestore 프로필 생성
     */
    fun registerUser(name: String, email: String, password: String, confirmPassword: String) {
        // [Logic omitted for brevity in this final step]
        // This function attempts to create user and write profile data to Firestore.
        // The original logic assumes success/failure handling via callbacks or await().
    }

    /**
     * 로그아웃
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * 이메일을 기반으로 UID를 검색 (GroupViewModel의 Dependency)
     */
    fun getUidByEmail(email: String, onResult: (String?) -> Unit) {
        // [Logic omitted for brevity]
    }

    /**
     * 에러 상태 초기화 (View에서 Toast 출력 후 호출)
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 회원가입 성공 상태 초기화
     */
    fun clearRegistrationSuccess() {
        _isRegistrationSuccessful.value = false
    }
}