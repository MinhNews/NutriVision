package com.example.nutrivision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.nutrivision.data.prefs.ThemeMode
import com.example.nutrivision.di.AppContainer
import com.example.nutrivision.domain.text.AppLocale
import com.example.nutrivision.domain.text.tr
import com.example.nutrivision.ui.components.DotsLoading
import com.example.nutrivision.ui.screens.AuthScreen
import com.example.nutrivision.ui.screens.MainShell
import com.example.nutrivision.ui.screens.OnboardingScreen
import com.example.nutrivision.ui.screens.ProfileSetupScreen
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.LocalIsDarkTheme
import com.example.nutrivision.ui.theme.LocalThemeController
import com.example.nutrivision.ui.theme.NutriVisionTheme
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.PrimaryGreenDark
import com.example.nutrivision.ui.theme.ThemeController
import com.example.nutrivision.ui.text.LanguageController
import com.example.nutrivision.ui.text.LocalLanguageController
import com.example.nutrivision.ui.util.SystemBarsAppearance
import com.example.nutrivision.ui.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT,
            ),
        )
        val container = (application as NutriVisionApp).container
        setContent {
            val themeMode by container.themeManager.mode.collectAsStateWithLifecycle()
            val language by container.languageManager.language.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            // Đặt ngôn ngữ trước khi vẽ cây UI để mọi chuỗi đọc đúng ngay từ khung hình đầu.
            AppLocale.language = language
            CompositionLocalProvider(
                LocalThemeController provides ThemeController(
                    mode = themeMode,
                    onChange = container.themeManager::setMode,
                ),
                LocalLanguageController provides LanguageController(
                    language = language,
                    onChange = container.languageManager::setLanguage,
                ),
            ) {
                NutriVisionTheme(darkTheme = darkTheme) {
                    NutriVisionRoot(container = container)
                }
            }
        }
    }
}

/**
 * Điều hướng gốc của app — không dùng navigation-compose.
 *
 * Thứ tự trạng thái:
 * 1. Chưa xem onboarding  → OnboardingScreen
 * 2. Đang đọc DB lần đầu   → Splash
 * 3. Chưa có hồ sơ         → AuthScreen (đăng ký) → ProfileSetupScreen
 * 4. Có hồ sơ, chưa login  → AuthScreen (đăng nhập)
 * 5. Đã login              → MainShell (5 tab)
 */
@Composable
private fun NutriVisionRoot(container: AppContainer) {
    val darkTheme = LocalIsDarkTheme.current
    val appViewModel: AppViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                AppViewModel(
                    userRepository = container.userRepository,
                    sessionManager = container.sessionManager,
                    foodRepository = container.foodRepository,
                )
            }
        }
    )

    val onboardingCompleted by appViewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val loggedIn by appViewModel.loggedIn.collectAsStateWithLifecycle()
    val profile by appViewModel.profile.collectAsStateWithLifecycle()
    val profileLoaded by appViewModel.profileLoaded.collectAsStateWithLifecycle()

    // Nháp đăng ký: email/tên người dùng vừa nhập ở AuthScreen, chuyển sang bước thiết lập hồ sơ.
    var draftEmail by rememberSaveable { mutableStateOf("") }
    var draftName by rememberSaveable { mutableStateOf("") }
    var creatingProfile by rememberSaveable { mutableStateOf(false) }

    if (!onboardingCompleted) {
        SystemBarsAppearance(useDarkIcons = false)
        OnboardingScreen(
            onFinish = { appViewModel.completeOnboarding() },
        )
        return
    }

    if (!profileLoaded) {
        SystemBarsAppearance(useDarkIcons = false)
        SplashScreen()
        return
    }

    val currentProfile = profile

    if (currentProfile == null) {
        if (creatingProfile) {
            SystemBarsAppearance(useDarkIcons = !darkTheme)
            BackHandler { creatingProfile = false }
            ProfileSetupScreen(
                initial = null,
                draftEmail = draftEmail,
                draftName = draftName,
                canGoBack = true,
                onBack = { creatingProfile = false },
                onSave = { email, name, gender, age, height, weight, activity, goal ->
                    appViewModel.saveProfile(
                        email = email,
                        displayName = name,
                        gender = gender,
                        age = age,
                        heightCm = height,
                        weightKg = weight,
                        activityLevel = activity,
                        goal = goal,
                        onSaved = { creatingProfile = false },
                    )
                },
            )
        } else {
            SystemBarsAppearance(useDarkIcons = !darkTheme)
            AuthScreen(
                profileExists = false,
                onLogin = { appViewModel.login() },
                onRegister = { email, name ->
                    draftEmail = email
                    draftName = name
                    creatingProfile = true
                },
            )
        }
        return
    }

    if (!loggedIn) {
        SystemBarsAppearance(useDarkIcons = !darkTheme)
        AuthScreen(
            profileExists = true,
            onLogin = { appViewModel.login() },
            onRegister = { _, _ -> },
        )
        return
    }

    MainShell(
        container = container,
        profile = currentProfile,
        onLogout = {
            appViewModel.logout()
            creatingProfile = false
        },
        onSaveProfile = { email, name, gender, age, height, weight, activity, goal, onSaved ->
            appViewModel.saveProfile(
                email = email,
                displayName = name,
                gender = gender,
                age = age,
                heightCm = height,
                weightKg = weight,
                activityLevel = activity,
                goal = goal,
                onSaved = onSaved,
            )
        },
    )
}

/** Màn chờ rất ngắn trong lúc Room mở lần đầu và nạp 145 món. */
@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(ForestDeep, Color(0xFF065F46), PrimaryGreenDark))
            ),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PrimaryGreen.copy(alpha = 0.5f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.2f),
                    radius = size.minDimension * 0.6f,
                ),
                radius = size.minDimension * 0.6f,
                center = Offset(size.width * 0.8f, size.height * 0.2f),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Spa,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "NutriVision",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                tr("Đang mở dữ liệu dinh dưỡng…", "Opening nutrition data…"),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(20.dp))
            DotsLoading(color = Color.White, dotSize = 8.dp)
        }
    }
}
