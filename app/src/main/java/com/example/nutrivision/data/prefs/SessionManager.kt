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

    /** Lan dong bo Firestore thanh cong gan nhat (0 = chua tung dong bo tren may nay). */
    private val _lastSyncAt = MutableStateFlow(prefs.getLong(KEY_LAST_SYNC, 0L))
    val lastSyncAt: StateFlow<Long> = _lastSyncAt.asStateFlow()

    fun markSynced(at: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC, at).remove(KEY_LAST_SYNC_ERROR).apply()
        _lastSyncAt.value = at
        _lastSyncError.value = null
    }

    /** Lỗi đồng bộ tự động gần nhất - hiển thị ngay ở tab Cá nhân thay vì snackbar. */
    private val _lastSyncError = MutableStateFlow(prefs.getString(KEY_LAST_SYNC_ERROR, null))
    val lastSyncError: StateFlow<String?> = _lastSyncError.asStateFlow()

    fun markSyncFailed(message: String?) {
        prefs.edit().putString(KEY_LAST_SYNC_ERROR, message).apply()
        _lastSyncError.value = message
    }

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
        private const val KEY_LAST_SYNC = "last_sync_at"
        private const val KEY_LAST_SYNC_ERROR = "last_sync_error"

        /** Tang so nay khi noi dung dieu khoan thay doi de bat nguoi dung dong y lai. */
        const val TERMS_VERSION = 1
    }
}
