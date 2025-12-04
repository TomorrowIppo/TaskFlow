package com.ippo.taskflow.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ippo.taskflow.data.User // User 데이터 모델 Import
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
    // 1. PUBLIC STATE DEFINITIONS (Observed by Composables)
    // ---------------------------------------------------------------------
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isAuthenticated = MutableStateFlow(false) // 초기값은 init에서 설정됨
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

    // ✅ 프로필 업데이트 상태
    private val _isProfileUpdating = MutableStateFlow(false)
    val isProfileUpdating: StateFlow<Boolean> = _isProfileUpdating

    val userId: String?
        get() = auth.currentUser?.uid

    // ---------------------------------------------------------------------
    // 2. INITIALIZATION AND LIFECYCLE MANAGEMENT
    // ---------------------------------------------------------------------

    init {
        // 앱 실행 시 상태 확인 및 익명 사용자 로그아웃 처리
        initializeAuthState()

        // 인증 상태 리스너: 로그인/로그아웃 이벤트 발생 시 상태 업데이트
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            _isAuthenticated.value = user != null

            if (user != null) {
                loadUserProfile(user.uid)
            } else {
                _profile.value = null
            }
        }
    }

    /**
     * 앱 시작 시 인증 상태를 검사하고 익명 사용자를 자동 로그아웃 처리합니다.
     */
    private fun initializeAuthState() {
        val user = auth.currentUser

        if (user != null) {
            if (user.isAnonymous) {
                // 익명 사용자일 경우, 앱 시작 시 세션을 즉시 해제하여 LoginScreen으로 이동시킵니다.
                auth.signOut()
                _isAuthenticated.value = false
            } else {
                _isAuthenticated.value = true
            }
        } else {
            _isAuthenticated.value = false
        }
    }

    private fun loadUserProfile(uid: String) {
        usersCollection.document(uid).get()
            .addOnSuccessListener { snapshot ->
                _profile.value = snapshot.toObject(User::class.java)
            }
            .addOnFailureListener { e ->
                _error.value = "프로필 로드 실패: ${e.message}"
            }
    }


    // ---------------------------------------------------------------------
    // 3. AUTHENTICATION ACTIONS
    // ---------------------------------------------------------------------

    /**
     * Email/PW 로그인 및 익명 로그인 폴백 통합
     */
    fun signIn(email: String, password: String) {
        _emailError.value = null
        _passwordError.value = null

        if (email.isBlank() && password.isBlank()) {
            signInAsGuest()
            return
        }

        // [Validation logic]
        if (!EMAIL_ADDRESS_PATTERN.matcher(email).matches()) { _emailError.value = "유효하지 않은 이메일 형식입니다."; return }
        if (password.length < 6) { _passwordError.value = "비밀번호는 최소 6자 이상이어야 합니다."; return }

        _isLoading.value = true
        _error.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _error.value = "로그인 실패: 사용자 정보를 확인해주세요."
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
            .addOnSuccessListener { _isLoading.value = false }
            .addOnFailureListener { e ->
                _error.value = e.message
                _isLoading.value = false
            }
    }

    /**
     * 사용자 회원가입 및 Firestore 프로필 생성 (statusMsg 초기값 반영)
     */
    fun registerUser(name: String, email: String, password: String, confirmPassword: String) {
        // [Validation logic omitted]
        _error.value = null
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val uid = authResult.user?.uid
                if (uid != null) {
                    val newUser = User(uid = uid, email = email, name = name, statusMsg = null, createdAt = Date())

                    // Firestore users 컬렉션에 프로필 저장
                    usersCollection.document(uid).set(newUser)
                        .addOnSuccessListener { _isRegistrationSuccessful.value = true }
                        .addOnFailureListener { e ->
                            _error.value = "프로필 저장 실패: ${e.localizedMessage}"
                        }
                }
            }
            .addOnFailureListener { e ->
                _error.value = "회원가입 실패: ${e.localizedMessage}"
            }
    }

    /**
     * 로그아웃
     */
    fun signOut() {
        auth.signOut()
    }

    // ---------------------------------------------------------------------
    // 4. PROFILE MANAGEMENT ACTIONS
    // ---------------------------------------------------------------------

    /**
     * [리팩토링 통합] 사용자의 이름(name)과 상태 메시지(statusMsg)를 동시에 업데이트합니다.
     */
    fun updateProfile(name: String, statusMessage: String) {
        val uid = userId ?: run {
            _error.value = "로그인이 필요합니다."
            return
        }

        // 기본적인 유효성 검사
        if (name.isBlank()) {
            _error.value = "사용자 이름은 공백일 수 없습니다."
            return
        }
        if (statusMessage.length > 100) {
            _error.value = "상태 메시지는 100자를 초과할 수 없습니다."
            return
        }

        _isProfileUpdating.value = true
        _error.value = null

        // 업데이트할 필드를 Map으로 정의합니다.
        val updates = mapOf(
            "name" to name,
            "statusMsg" to statusMessage
        )

        usersCollection.document(uid).update(updates)
            .addOnSuccessListener {
                // 로컬 ViewModel 상태(profile)를 업데이트하여 UI에 즉시 반영되도록 합니다.
                _profile.value = _profile.value?.copy(name = name, statusMsg = statusMessage)
                _isProfileUpdating.value = false
            }
            .addOnFailureListener { e ->
                _error.value = "프로필 업데이트 실패: ${e.message}"
                _isProfileUpdating.value = false
            }
    }


    // ---------------------------------------------------------------------
    // 5. UTILITY FUNCTIONS
    // ---------------------------------------------------------------------

    /**
     * 이메일을 기반으로 UID를 검색 (GroupViewModel의 Dependency)
     */
    fun getUidByEmail(email: String, onResult: (String?) -> Unit) {
        if (email.isBlank()) { onResult(null); return }

        db.collection("users")
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val user = querySnapshot.documents.first().toObject(User::class.java)
                    onResult(user?.uid)
                } else { onResult(null) }
            }
            .addOnFailureListener { e ->
                _error.value = "사용자 검색 실패: ${e.message}"
                onResult(null)
            }
    }

    /**
     * 에러 상태 초기화 (Utility)
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 회원가입 성공 상태 초기화 (Utility)
     */
    fun clearRegistrationSuccess() {
        _isRegistrationSuccessful.value = false
    }
}