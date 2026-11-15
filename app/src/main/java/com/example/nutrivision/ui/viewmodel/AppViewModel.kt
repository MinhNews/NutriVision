package com.example.nutrivision.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.auth.AuthRepository
import com.example.nutrivision.data.auth.AuthResult
import com.example.nutrivision.data.local.LocalDataStore
import com.example.nutrivision.data.prefs.SessionManager
import com.example.nutrivision.data.repository.FoodRepository
import com.example.nutrivision.data.repository.UserRepository
import com.example.nutrivision.domain.calc.NutritionCalculator
import com.example.nutrivision.domain.model.ActivityLevel
import com.example.nutrivision.domain.model.Goal
import com.example.nutrivision.domain.model.UserProfile
import com.example.nutrivision.domain.text.tr
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Trang thai phien dang nhap. Firebase la nguon su that, khong con co "logged_in" gia. */
sealed interface AuthState {
    data object Loading : AuthState
    data object SignedOut : AuthState
    data class SignedIn(
        val userId: String,
        val email: String,
        val displayName: String?,
    ) : AuthState
}

data class AuthMessage(
    val text: String,
    val isError: Boolean,
)

class AppViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    private val authRepository: AuthRepository,
    private val localDataStore: LocalDataStore,
    foodRepository: FoodRepository,
) : ViewModel() {

    val onboardingCompleted: StateFlow<Boolean> = sessionManager.onboardingCompleted

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _emailVerified = MutableStateFlow(false)
    val emailVerified: StateFlow<Boolean> = _emailVerified.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val busy: StateFlow<Boolean> = _busy.asStateFlow()

    private val _message = MutableStateFlow<AuthMessage?>(null)
    val message: StateFlow<AuthMessage?> = _message.asStateFlow()

    private data class UserData(val profile: UserProfile?, val loaded: Boolean)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val userData: Flow<UserData> = authRepository.observeAuthState()
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(UserData(profile = null, loaded = true))
            } else {
                flow {
                    // Chuyen du lieu "local" cua tuan 3 sang UID that truoc lan doc dau tien.
                    runCatching { localDataStore.adoptLegacyData(user.uid, user.email.orEmpty()) }
                    // Ho so cu co the mang email khac (vi du email go tay o tuan 3). Tu tuan 4,
                    // email cua tai khoan Firebase la nguon su that nen dong bo lai mot lan.
                    healProfileEmail(user.uid, user.email.orEmpty())
                    emitAll(userRepository.observeProfile(user.uid).map { UserData(it, true) })
                }
            }
        }

    val profile: StateFlow<UserProfile?> = userData
        .map { it.profile }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** true sau lan doc DB dau tien cua tai khoan hien tai. */
    val profileLoaded: StateFlow<Boolean> = userData
        .map { it.loaded }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** So mon that trong DB - dung cho onboarding, khong phai so trang tri. */
    val foodCount: StateFlow<Int> = foodRepository.observeAll()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch {
            authRepository.observeAuthState().collect { user ->
                _authState.value = if (user == null) {
                    AuthState.SignedOut
                } else {
                    AuthState.SignedIn(
                        userId = user.uid,
                        email = user.email.orEmpty(),
                        displayName = user.displayName,
                    )
                }
                _emailVerified.value = user?.isEmailVerified == true
                if (user == null) _busy.value = false
            }
        }
    }

    fun completeOnboarding() = sessionManager.completeOnboarding()

    fun clearMessage() {
        _message.value = null
    }

    /** Dong bo email trong ho so ve email cua tai khoan Firebase (mot lan, khi email khac nhau). */
    private suspend fun healProfileEmail(userId: String, accountEmail: String) {
        if (accountEmail.isBlank()) return
        val existing = runCatching { userRepository.getProfile(userId) }.getOrNull() ?: return
        if (existing.email.equals(accountEmail, ignoreCase = true)) return
        runCatching {
            userRepository.saveProfile(
                existing.copy(email = accountEmail, updatedAt = System.currentTimeMillis())
            )
        }
    }

    fun hasAcceptedTerms(): Boolean = sessionManager.hasAcceptedTerms(SessionManager.TERMS_VERSION)

    fun login(email: String, password: String) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            _message.value = null
            when (val result = authRepository.login(email.trim(), password)) {
                is AuthResult.Success -> Unit
                is AuthResult.Failure -> _message.value = AuthMessage(result.text(), isError = true)
            }
            _busy.value = false
        }
    }

    fun register(email: String, password: String, displayName: String) {
        if (_busy.value) return
        viewModelScope.launch {
            _busy.value = true
            _message.value = null
            when (val result = authRepository.register(email.trim(), password, displayName.trim())) {
                is AuthResult.Success -> {
                    sessionManager.acceptTerms(SessionManager.TERMS_VERSION)
                    _message.value = AuthMessage(
                        text = tr(
                            "Đã gửi link xác thực tới email của bạn.",
                            "We sent a verification link to your email.",
                        ),
                        isError = false,
                    )
                }
                is AuthResult.Failure -> _message.value = AuthMessage(result.text(), isError = true)
            }
            _busy.value = false
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _message.value = AuthMessage(
                tr(
                    "Nhập email trước khi lấy lại mật khẩu.",
                    "Enter your email before resetting the password.",
                ),
                isError = true,
            )
            return
        }
        viewModelScope.launch {
            _busy.value = true
            when (val result = authRepository.resetPassword(email.trim())) {
                is AuthResult.Success -> _message.value = AuthMessage(
                    tr(
                        "Đã gửi email đặt lại mật khẩu. Kiểm tra hộp thư.",
                        "Password reset email sent. Check your inbox.",
                    ),
                    isError = false,
                )
                is AuthResult.Failure -> _message.value = AuthMessage(result.text(), isError = true)
            }
            _busy.value = false
        }
    }

    fun resendVerification() {
        viewModelScope.launch {
            _busy.value = true
            when (val result = authRepository.resendVerification()) {
                is AuthResult.Success -> _message.value = AuthMessage(
                    tr(
                        "Đã gửi lại link xác thực.",
                        "Verification link sent again.",
                    ),
                    isError = false,
                )
                is AuthResult.Failure -> _message.value = AuthMessage(result.text(), isError = true)
            }
            _busy.value = false
        }
    }

    fun refreshVerification() {
        viewModelScope.launch {
            _emailVerified.value = authRepository.reloadUser()
        }
    }

    fun logout() {
        authRepository.signOut()
        _message.value = null
    }

    fun saveProfile(
        email: String,
        displayName: String?,
        gender: String,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activityLevel: ActivityLevel,
        goal: Goal,
        onSaved: () -> Unit,
    ) {
        val userId = authRepository.currentUserId ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = userRepository.getProfile(userId)
            // Email luu trong ho so luon la email cua tai khoan Firebase, khong phai chuoi go tay.
            val accountEmail = authRepository.currentUser?.email.orEmpty()
            val bmr = NutritionCalculator.bmr(gender, age, heightCm, weightKg)
            val tdee = NutritionCalculator.tdee(bmr, activityLevel)
            val targets = NutritionCalculator.targets(gender, age, heightCm, weightKg, activityLevel, goal)
            val profile = UserProfile(
                userId = userId,
                email = accountEmail.ifBlank { email.trim().ifBlank { existing?.email.orEmpty() } },
                displayName = displayName?.trim()?.ifBlank { null },
                gender = gender,
                age = age,
                heightCm = heightCm,
                weightKg = weightKg,
                activityLevel = activityLevel,
                goal = goal,
                bmr = bmr,
                tdee = tdee,
                targetCalories = targets.calories,
                targetProtein = targets.proteinG,
                targetCarbs = targets.carbsG,
                targetFat = targets.fatG,
                createdAt = existing?.createdAt ?: now,
                updatedAt = now,
            )
            userRepository.saveProfile(profile)
            onSaved()
        }
    }
}

private fun AuthResult.Failure.text(): String = message
