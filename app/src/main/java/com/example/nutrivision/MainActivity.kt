package com.example.nutrivision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.example.nutrivision.ui.viewmodel.AuthState

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
            // Dat ngon ngu truoc khi ve cay UI de moi chuoi duoc dung ngay tu khung hinh dau.
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
 * Dieu huong goc cua app - khong dung navigation-compose.
 *
 * Thu tu trang thai:
 * 1. Chua xem onboarding   -> OnboardingScreen
 * 2. Firebase dang khoi phuc phien -> Splash
 * 3. Chua dang nhap        -> AuthScreen (dang nhap / dang ky that)
 * 4. Da dang nhap, chua co ho so -> ProfileSetupScreen
 * 5. Da co ho so           -> MainShell (5 tab)
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
                    authRepository = container.authRepository,
                    localDataStore = container.localDataStore,
                    foodRepository = container.foodRepository,
                )
            }
        }
    )

    val onboardingCompleted by appViewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val authState by appViewModel.authState.collectAsStateWithLifecycle()
    val profile by appViewModel.profile.collectAsStateWithLifecycle()
    val profileLoaded by appViewModel.profileLoaded.collectAsStateWithLifecycle()
    val busy by appViewModel.busy.collectAsStateWithLifecycle()
    val message by appViewModel.message.collectAsStateWithLifecycle()
    val emailVerified by appViewModel.emailVerified.collectAsStateWithLifecycle()

    if (!onboardingCompleted) {
        SystemBarsAppearance(useDarkIcons = false)
        OnboardingScreen(
            onFinish = { appViewModel.completeOnboarding() },
        )
        return
    }

    when (val state = authState) {
        AuthState.Loading -> {
            SystemBarsAppearance(useDarkIcons = false)
            SplashScreen()
        }

        AuthState.SignedOut -> {
            SystemBarsAppearance(useDarkIcons = !darkTheme)
            AuthScreen(
                busy = busy,
                message = message,
                onClearMessage = appViewModel::clearMessage,
                onLogin = appViewModel::login,
                onRegister = appViewModel::register,
                onForgotPassword = appViewModel::resetPassword,
            )
        }

        is AuthState.SignedIn -> {
            val currentProfile = profile
            when {
                !profileLoaded -> {
                    SystemBarsAppearance(useDarkIcons = false)
                    SplashScreen()
                }

                currentProfile == null -> {
                    SystemBarsAppearance(useDarkIcons = !darkTheme)
                    // Back o buoc nay = dang xuat, de nguoi dung doi tai khoan khac.
                    BackHandler { appViewModel.logout() }
                    ProfileSetupScreen(
                        initial = null,
                        draftEmail = state.email,
                        draftName = state.displayName.orEmpty(),
                        canGoBack = true,
                        onBack = { appViewModel.logout() },
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
                                onSaved = {},
                            )
                        },
                    )
                }

                else -> {
                    MainShell(
                        container = container,
                        profile = currentProfile,
                        accountEmail = state.email,
                        emailVerified = emailVerified,
                        message = message,
                        onClearMessage = appViewModel::clearMessage,
                        onResendVerification = appViewModel::resendVerification,
                        onRefreshVerification = appViewModel::refreshVerification,
                        onLogout = appViewModel::logout,
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
            }
        }
    }
}

/** Man cho rat ngan trong luc Firebase khoi phuc phien va Room mo lan dau. */
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
                tr("Đang mở dữ liệu dinh dưỡng...", "Opening nutrition data..."),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(20.dp))
            DotsLoading(color = Color.White, dotSize = 8.dp)
        }
    }
}
