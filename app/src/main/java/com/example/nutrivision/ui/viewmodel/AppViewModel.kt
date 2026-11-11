package com.example.nutrivision.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.prefs.SessionManager
import com.example.nutrivision.data.repository.FoodRepository
import com.example.nutrivision.data.repository.UserRepository
import com.example.nutrivision.domain.calc.NutritionCalculator
import com.example.nutrivision.domain.model.ActivityLevel
import com.example.nutrivision.domain.model.Goal
import com.example.nutrivision.domain.model.UserProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager,
    foodRepository: FoodRepository,
) : ViewModel() {

    val onboardingCompleted: StateFlow<Boolean> = sessionManager.onboardingCompleted
    val loggedIn: StateFlow<Boolean> = sessionManager.loggedIn

    val profile: StateFlow<UserProfile?> = userRepository.observeProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** true sau lần đọc DB đầu tiên — tránh nháy màn hình thiết lập khi hồ sơ đã có. */
    val profileLoaded: StateFlow<Boolean> = userRepository.observeProfile()
        .map { true }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Số món thật trong DB — dùng cho onboarding, không phải số trang trí. */
    val foodCount: StateFlow<Int> = foodRepository.observeAll()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun completeOnboarding() = sessionManager.completeOnboarding()

    fun login() = sessionManager.setLoggedIn(true)

    fun logout() = sessionManager.setLoggedIn(false)

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
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val existing = userRepository.getProfile()
            val bmr = NutritionCalculator.bmr(gender, age, heightCm, weightKg)
            val tdee = NutritionCalculator.tdee(bmr, activityLevel)
            val targets = NutritionCalculator.targets(gender, age, heightCm, weightKg, activityLevel, goal)
            val profile = UserProfile(
                userId = UserRepository.LOCAL_USER_ID,
                email = email.trim(),
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
            sessionManager.setLoggedIn(true)
            onSaved()
        }
    }
}
