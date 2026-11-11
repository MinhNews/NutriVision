package com.example.nutrivision.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Trạng thái phiên chạy trên máy: đã xem onboarding chưa, đang đăng nhập chưa.
 * Tuần 3 chưa có Firebase Auth nên đây chỉ là cờ điều hướng, không phải dữ liệu dinh dưỡng.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING, false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _loggedIn = MutableStateFlow(prefs.getBoolean(KEY_LOGGED_IN, false))
    val loggedIn: StateFlow<Boolean> = _loggedIn.asStateFlow()

    fun completeOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
        _onboardingCompleted.value = true
    }

    fun setLoggedIn(value: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, value).apply()
        _loggedIn.value = value
    }

    companion object {
        private const val PREFS = "nutrivision_session"
        private const val KEY_ONBOARDING = "onboarding_completed"
        private const val KEY_LOGGED_IN = "logged_in"
    }
}
