package com.example.nutrivision.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.auth.AuthRepository
import com.example.nutrivision.data.auth.AuthResult
import com.example.nutrivision.data.local.LocalDataStore
import com.example.nutrivision.data.local.NutriVisionDatabase
import com.example.nutrivision.data.prefs.ReminderSettings
import com.example.nutrivision.data.prefs.ReminderState
import com.example.nutrivision.data.prefs.SessionManager
import com.example.nutrivision.data.remote.FirestoreSync
import com.example.nutrivision.data.repository.FoodRepository
import com.example.nutrivision.data.repository.UserRepository
import com.example.nutrivision.domain.calc.NutritionCalculator
import com.example.nutrivision.domain.model.ActivityLevel
import com.example.nutrivision.domain.model.Goal
import com.example.nutrivision.domain.model.UserProfile
import com.example.nutrivision.domain.text.tr
import com.example.nutrivision.notifications.MealSlot
import com.example.nutrivision.notifications.ReminderScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

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
    private val database: NutriVisionDatabase,
    private val firestoreSync: FirestoreSync,
    private val reminderSettings: ReminderSettings,
    private val reminderScheduler: ReminderScheduler,
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

    /** Đang chạy đồng bộ Firestore - hiện "Đang đồng bộ…" ở tab Cá nhân. */
    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    /** Lần đồng bộ thành công gần nhất, hiện ở tab Cá nhân. */
    val lastSyncAt: StateFlow<Long> = sessionManager.lastSyncAt

    /** Lỗi đồng bộ tự động gần nhất (null nếu lần cuối thành công). */
    val lastSyncError: StateFlow<String?> = sessionManager.lastSyncError

    /** Giờ nhắc người dùng đã chọn cho từng bữa. */
    val reminders: StateFlow<ReminderState> = reminderSettings.state

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
                    // Đồng bộ xong rồi mới phát dữ liệu: máy thứ hai đăng nhập lần đầu sẽ thấy
                    // ngay hồ sơ cũ thay vì bắt người dùng nhập lại rồi mới bị ghi đè.
                    // Giới hạn 6 giây để mở app không bị treo khi mạng chậm.
                    val outcome = withTimeoutOrNull(SYNC_ON_START_TIMEOUT_MS) {
                        runCatching { firestoreSync.sync(user.uid) }.getOrNull()
                    }
                    if (outcome?.success == true) {
                        sessionManager.markSynced(outcome.finishedAt)
                    } else if (outcome != null) {
                        sessionManager.markSyncFailed(outcome.message)
                    }
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
                if (user == null) {
                    _busy.value = false
                } else {
                    // Mỗi lần mở app: bảo đảm lịch nhắc còn đúng (WorkManager giữ lịch qua reboot).
                    runCatching { reminderScheduler.sync() }
                }
            }
        }
        startAutoSync()
    }

    /**
     * Đồng bộ tự động: không có nút bấm tay.
     *
     * Nghe "vân tay" (số dòng + updated_at mới nhất) của hồ sơ, bữa và món trong Room.
     * Gộp các thao tác liên tiếp bằng debounce 2 giây rồi đẩy lên Firestore. Việc đồng bộ
     * chỉ đổi `sync_status` chứ không đổi `updated_at` nên không tự kích hoạt lại.
     * Lần phát đầu tiên sau khi đăng nhập bị bỏ qua vì đã có đồng bộ lúc mở app.
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun startAutoSync() {
        viewModelScope.launch {
            authRepository.observeAuthState()
                .map { it?.uid }
                .distinctUntilChanged()
                .flatMapLatest { userId ->
                    if (userId == null) {
                        flowOf(null)
                    } else {
                        merge(
                            database.mealDao().observeMealFingerprint(userId),
                            database.mealDao().observeItemFingerprint(userId),
                            database.userDao().observeFingerprint(userId),
                        )
                            .distinctUntilChanged()
                            .debounce(SYNC_DEBOUNCE_MS)
                            .drop(1)
                            .map { userId }
                    }
                }
                .filterNotNull()
                .collect { userId -> sync(userId) }
        }
    }

    /** Gọi khi app quay lại foreground: dữ liệu máy khác có thể đã đổi. */
    fun onAppForeground() {
        val userId = authRepository.currentUserId ?: return
        val sinceLast = System.currentTimeMillis() - sessionManager.lastSyncAt.value
        if (sinceLast < FOREGROUND_SYNC_INTERVAL_MS) return
        viewModelScope.launch { sync(userId) }
    }

    private suspend fun sync(userId: String) {
        if (_syncing.value) return
        _syncing.value = true
        val outcome = runCatching { firestoreSync.sync(userId) }.getOrNull()
        _syncing.value = false
        if (outcome?.success == true) {
            sessionManager.markSynced(outcome.finishedAt)
        } else {
            sessionManager.markSyncFailed(
                outcome?.message ?: tr(
                    "Không kết nối được máy chủ đồng bộ.",
                    "Could not reach the sync server.",
                )
            )
        }
    }

    fun completeOnboarding() = sessionManager.completeOnboarding()

    fun clearMessage() {
        _message.value = null
    }

    /** Bật/tắt nhắc cho một bữa; WorkManager giữ lịch kể cả khi tắt app. */
    fun setReminderEnabled(slot: MealSlot, enabled: Boolean) {
        reminderSettings.setEnabled(slot, enabled)
        reminderScheduler.sync()
    }

    /** Đổi giờ nhắc của một bữa. */
    fun setReminderTime(slot: MealSlot, hour: Int, minute: Int) {
        reminderSettings.setTime(slot, hour, minute)
        reminderScheduler.sync()
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

    private companion object {
        /** Không để màn hình chờ phụ thuộc vào mạng: quá 6 giây thì vào app bằng dữ liệu máy. */
        const val SYNC_ON_START_TIMEOUT_MS = 6_000L

        /** Gộp các thao tác liên tiếp (thêm 3 món = 1 lần đồng bộ) trước khi đẩy lên Firestore. */
        const val SYNC_DEBOUNCE_MS = 2_000L

        /** Quay lại app sau 5 phút thì kéo dữ liệu máy khác về. */
        const val FOREGROUND_SYNC_INTERVAL_MS = 5 * 60_000L
    }
}

private fun AuthResult.Failure.text(): String = message
