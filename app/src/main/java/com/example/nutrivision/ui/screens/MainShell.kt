package com.example.nutrivision.ui.screens

import com.example.nutrivision.domain.text.tr
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.nutrivision.data.repository.UserRepository
import com.example.nutrivision.di.AppContainer
import com.example.nutrivision.domain.model.ActivityLevel
import com.example.nutrivision.domain.model.Goal
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.domain.model.UserProfile
import com.example.nutrivision.ui.components.AppBackground
import com.example.nutrivision.ui.navigation.AppTab
import com.example.nutrivision.ui.navigation.Route
import com.example.nutrivision.ui.theme.ForestDeep
import com.example.nutrivision.ui.theme.HeroGradientEnd
import com.example.nutrivision.ui.theme.HeroGradientStart
import com.example.nutrivision.ui.theme.LocalIsDarkTheme
import com.example.nutrivision.ui.theme.MintMid
import com.example.nutrivision.ui.theme.PrimaryGreen
import com.example.nutrivision.ui.theme.SurfaceLight
import com.example.nutrivision.ui.theme.TextMuted
import com.example.nutrivision.ui.theme.TextSecondary
import com.example.nutrivision.ui.util.SystemBarsAppearance
import com.example.nutrivision.ui.viewmodel.DashboardViewModel
import com.example.nutrivision.ui.viewmodel.DiaryViewModel
import com.example.nutrivision.ui.viewmodel.MealDetailViewModel
import com.example.nutrivision.ui.viewmodel.SearchViewModel
import com.example.nutrivision.ui.viewmodel.StatsViewModel

@Composable
fun MainShell(
    container: AppContainer,
    profile: UserProfile,
    onLogout: () -> Unit,
    onSaveProfile: (
        email: String,
        name: String,
        gender: String,
        age: Int,
        heightCm: Double,
        weightKg: Double,
        activity: ActivityLevel,
        goal: Goal,
        onSaved: () -> Unit,
    ) -> Unit,
) {
    var tabName by rememberSaveable { mutableStateOf(AppTab.HOME.name) }
    val tab = AppTab.valueOf(tabName)
    var overlay by remember { mutableStateOf<Route?>(null) }

    val isDark = LocalIsDarkTheme.current
    SystemBarsAppearance(
        useDarkIcons = !isDark && tab != AppTab.CAMERA && overlay == null,
    )

    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DashboardViewModel(container.userRepository, container.mealRepository) }
        }
    )
    val diaryViewModel: DiaryViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DiaryViewModel(container.userRepository, container.mealRepository) }
        }
    )
    val statsViewModel: StatsViewModel = viewModel(
        factory = viewModelFactory {
            initializer { StatsViewModel(container.userRepository, container.mealRepository) }
        }
    )

    val dashboardState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    val diaryState by diaryViewModel.uiState.collectAsStateWithLifecycle()
    val statsState by statsViewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = overlay != null) { overlay = null }

    Box(modifier = Modifier.fillMaxSize()) {
        AppBackground {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    BottomNavBar(
                        selected = tab,
                        onSelect = { selected ->
                            tabName = selected.name
                            overlay = null
                        },
                    )
                },
            ) { padding ->
                AnimatedContent(
                    targetState = tab,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    transitionSpec = {
                        (fadeIn(tween(240)) + scaleIn(tween(260), initialScale = 0.985f)) togetherWith
                            fadeOut(tween(120))
                    },
                    label = "tabContent",
                ) { current ->
                    when (current) {
                        AppTab.HOME -> DashboardScreen(
                            uiState = dashboardState,
                            onOpenProfile = { tabName = AppTab.PROFILE.name },
                            onOpenDiary = { tabName = AppTab.DIARY.name },
                            onAddMeal = { type -> overlay = Route.Search(type) },
                        )

                        AppTab.DIARY -> DiaryScreen(
                            viewModel = diaryViewModel,
                            uiState = diaryState,
                            onAddMeal = { type -> overlay = Route.Search(type) },
                            onEditItem = { item, type ->
                                overlay = Route.MealDetail(item.foodId, item.id, type)
                            },
                        )

                        AppTab.CAMERA -> CameraScreen(
                            onSearchClick = { overlay = Route.Search(MealType.LUNCH) },
                        )

                        AppTab.STATS -> StatsScreen(
                            uiState = statsState,
                            onGoToDiary = { tabName = AppTab.DIARY.name },
                            onSelectRange = statsViewModel::selectRange,
                            onPreviousMonth = statsViewModel::previousMonth,
                            onNextMonth = statsViewModel::nextMonth,
                        )

                        AppTab.PROFILE -> ProfileScreen(
                            profile = profile,
                            onEditProfile = { overlay = Route.EditProfile },
                            onLogout = onLogout,
                        )
                    }
                }
            }
        }

        AnimatedContent(
            targetState = overlay,
            transitionSpec = {
                if (targetState != null) {
                    (slideInHorizontally(tween(320)) { it } + fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(260)) { -it / 4 } + fadeOut(tween(160)))
                } else {
                    (fadeIn(tween(220))) togetherWith
                        (slideOutHorizontally(tween(280)) { it } + fadeOut(tween(180)))
                }
            },
            label = "overlay",
        ) { route ->
            if (route != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(com.example.nutrivision.ui.theme.BackgroundLight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { }
                ) {
                    when (route) {
                        is Route.Search -> {
                            val searchViewModel: SearchViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer { SearchViewModel(container.foodRepository) }
                                }
                            )
                            val searchState by searchViewModel.uiState.collectAsStateWithLifecycle()
                            SearchScreen(
                                viewModel = searchViewModel,
                                uiState = searchState,
                                mealType = route.mealType,
                                onBack = { overlay = null },
                                onPick = { food ->
                                    overlay = Route.MealDetail(food.id, null, route.mealType)
                                },
                            )
                        }

                        is Route.MealDetail -> {
                            val detailViewModel: MealDetailViewModel = viewModel(
                                key = "meal-${route.foodId}-${route.itemId}-${route.mealType.key}",
                                factory = viewModelFactory {
                                    initializer {
                                        MealDetailViewModel(
                                            foodRepository = container.foodRepository,
                                            mealRepository = container.mealRepository,
                                            foodId = route.foodId,
                                            itemId = route.itemId,
                                            initialMealType = route.mealType,
                                        )
                                    }
                                },
                            )
                            val detailState by detailViewModel.uiState.collectAsStateWithLifecycle()
                            MealDetailScreen(
                                viewModel = detailViewModel,
                                uiState = detailState,
                                onBack = { overlay = null },
                                onSaved = {
                                    overlay = null
                                    tabName = AppTab.DIARY.name
                                },
                                onDeleted = {
                                    overlay = null
                                    tabName = AppTab.DIARY.name
                                },
                            )
                        }

                        Route.EditProfile -> {
                            ProfileSetupScreen(
                                initial = profile,
                                draftEmail = profile.email,
                                draftName = profile.displayName.orEmpty(),
                                canGoBack = true,
                                onBack = { overlay = null },
                                onSave = { email, name, gender, age, height, weight, activity, goal ->
                                    onSaveProfile(email, name, gender, age, height, weight, activity, goal) {
                                        overlay = null
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NavItem(val tab: AppTab, val icon: ImageVector)

private val navItems = listOf(
    NavItem(AppTab.HOME, Icons.Rounded.Home),
    NavItem(AppTab.DIARY, Icons.Rounded.MenuBook),
    NavItem(AppTab.CAMERA, Icons.Rounded.CameraAlt),
    NavItem(AppTab.STATS, Icons.Rounded.BarChart),
    NavItem(AppTab.PROFILE, Icons.Rounded.Person),
)

@Composable
private fun BottomNavBar(
    selected: AppTab,
    onSelect: (AppTab) -> Unit,
) {
    Surface(
        color = SurfaceLight.copy(alpha = 0.97f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        shadowElevation = 22.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            navItems.forEach { item ->
                if (item.tab == AppTab.CAMERA) {
                    CameraNavItem(
                        selected = selected == AppTab.CAMERA,
                        onClick = { onSelect(AppTab.CAMERA) },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    NavItemView(
                        item = item,
                        selected = selected == item.tab,
                        onClick = { onSelect(item.tab) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavItemView(
    item: NavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint by animateColorAsState(
        if (selected) PrimaryGreen else TextMuted,
        label = "navTint",
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) MintMid.copy(alpha = 0.65f) else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                item.icon,
                contentDescription = item.tab.label,
                tint = tint,
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            item.tab.label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = tint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}

@Composable
private fun CameraNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(10.dp, CircleShape, ambientColor = com.example.nutrivision.ui.theme.ShadowGreen, spotColor = com.example.nutrivision.ui.theme.ShadowGreen)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(HeroGradientStart, HeroGradientEnd)))
                .then(
                    if (selected) {
                        Modifier.background(Color.Transparent)
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.CameraAlt,
                contentDescription = tr("Chụp món", "Scan food"),
                tint = Color.White,
                modifier = Modifier.size(23.dp),
            )
        }
        Spacer(Modifier.height(3.dp))
        Text(
            AppTab.CAMERA.label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            color = if (selected) PrimaryGreen else TextMuted,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
        )
    }
}
