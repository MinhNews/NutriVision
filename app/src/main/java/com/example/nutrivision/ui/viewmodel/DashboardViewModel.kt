package com.example.nutrivision.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.repository.MealRepository
import com.example.nutrivision.data.repository.UserRepository
import com.example.nutrivision.domain.calc.DateUtils
import com.example.nutrivision.domain.model.MealType
import com.example.nutrivision.domain.model.MealWithItems
import com.example.nutrivision.domain.model.Nutrition
import com.example.nutrivision.domain.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val loading: Boolean = true,
    val date: String = DateUtils.todayKey(),
    val profile: UserProfile? = null,
    val meals: List<MealWithItems> = emptyList(),
    val nutrition: Nutrition = Nutrition.ZERO,
) {
    fun mealOf(type: MealType): MealWithItems? = meals.firstOrNull { it.meal.type == type }

    val itemCount: Int get() = meals.sumOf { it.items.size }

    val hasData: Boolean get() = nutrition.calories > 0.0
}

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    userRepository: UserRepository,
    mealRepository: MealRepository,
) : ViewModel() {

    private val userId = UserRepository.LOCAL_USER_ID

    val uiState: StateFlow<DashboardUiState> = DateUtils.todayFlow()
        .flatMapLatest { today ->
            combine(
                userRepository.observeProfile(userId),
                mealRepository.observeDay(userId, today),
                mealRepository.observeDayNutrition(userId, today),
            ) { profile, meals, nutrition ->
                DashboardUiState(
                    loading = false,
                    date = today,
                    profile = profile,
                    meals = meals,
                    nutrition = nutrition,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(),
        )
}
