package com.example.nutrivision.di

import android.content.Context
import com.example.nutrivision.data.auth.AuthRepository
import com.example.nutrivision.data.ai.FoodClassifier
import com.example.nutrivision.data.local.LocalDataStore
import com.example.nutrivision.data.local.NutriVisionDatabase
import com.example.nutrivision.data.prefs.LanguageManager
import com.example.nutrivision.data.prefs.ReminderSettings
import com.example.nutrivision.data.prefs.SessionManager
import com.example.nutrivision.data.prefs.ThemeManager
import com.example.nutrivision.data.remote.FirestoreSync
import com.example.nutrivision.data.remote.GeminiFoodService
import com.example.nutrivision.data.remote.PhotoArchive
import com.example.nutrivision.data.repository.FoodRepository
import com.example.nutrivision.data.repository.MealRepository
import com.example.nutrivision.data.repository.UserRepository
import com.example.nutrivision.notifications.ReminderScheduler

/**
 * Manual Dependency Injection: mot cho duy nhat tao DB, DAO, Repository.
 * Khong dung Hilt de tranh them plugin thu hai do voi AGP 9.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val database: NutriVisionDatabase by lazy { NutriVisionDatabase.build(appContext) }

    val sessionManager: SessionManager by lazy { SessionManager(appContext) }

    val themeManager: ThemeManager by lazy { ThemeManager(appContext) }

    val languageManager: LanguageManager by lazy { LanguageManager(appContext) }

    val reminderSettings: ReminderSettings by lazy { ReminderSettings(appContext) }

    /** Dùng chung [reminderSettings] với UI, nếu không lịch nhắc sẽ đọc phải giá trị cũ. */
    val reminderScheduler: ReminderScheduler by lazy { ReminderScheduler(appContext, reminderSettings) }

    val authRepository: AuthRepository by lazy { AuthRepository() }

    val foodClassifier: FoodClassifier by lazy { FoodClassifier(appContext) }

    val geminiFoodService: GeminiFoodService by lazy { GeminiFoodService() }

    val localDataStore: LocalDataStore by lazy { LocalDataStore(database) }

    val photoArchive: PhotoArchive by lazy { PhotoArchive(appContext) }

    val firestoreSync: FirestoreSync by lazy { FirestoreSync(database, photoArchive = photoArchive) }

    val userRepository: UserRepository by lazy { UserRepository(database.userDao()) }

    val foodRepository: FoodRepository by lazy { FoodRepository(database.foodDao()) }

    val mealRepository: MealRepository by lazy { MealRepository(database, database.mealDao()) }
}
