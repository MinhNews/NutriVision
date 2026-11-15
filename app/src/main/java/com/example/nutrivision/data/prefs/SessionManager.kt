package com.example.nutrivision.data.prefs

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Trang thai cuc bo tren may: da xem onboarding chua, da dong y dieu khoan phien ban nao.
 * Phien dang nhap KHONG con luu o day nua - Firebase Auth giu va tu khoi phuc khi mo lai app.
 */
class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING, false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _acceptedTermsVersion = MutableStateFlow(prefs.getInt(KEY_TERMS, 0))
    val acceptedTermsVersion: StateFlow<Int> = _acceptedTermsVersion.asStateFlow()

    fun completeOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING, true).apply()
        _onboardingCompleted.value = true
    }

    fun acceptTerms(version: Int) {
        prefs.edit().putInt(KEY_TERMS, version).apply()
        _acceptedTermsVersion.value = version
    }

    fun hasAcceptedTerms(version: Int): Boolean = _acceptedTermsVersion.value >= version

    companion object {
        private const val PREFS = "nutrivision_session"
        private const val KEY_ONBOARDING = "onboarding_completed"
        private const val KEY_TERMS = "accepted_terms_version"

        /** Tang so nay khi noi dung dieu khoan thay doi de bat nguoi dung dong y lai. */
        const val TERMS_VERSION = 1
    }
}
