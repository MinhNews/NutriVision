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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DiaryUiState(
    val loading: Boolean = true,
    val date: String = DateUtils.todayKey(),
    val today: String = DateUtils.todayKey(),
    val profile: UserProfile? = null,
    val meals: List<MealWithItems> = emptyList(),
    val nutrition: Nutrition = Nutrition.ZERO,
) {
    fun mealOf(type: MealType): MealWithItems? = meals.firstOrNull { it.meal.type == type }
    val hasData: Boolean get() = nutrition.calories > 0.0
}

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryViewModel(
    private val userRepository: UserRepository,
    private val mealRepository: MealRepository,
) : ViewModel() {

    private val userId = UserRepository.LOCAL_USER_ID
    private val _selectedDate = MutableStateFlow(DateUtils.todayKey())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()
    private val _today = MutableStateFlow(DateUtils.todayKey())

    init {
        // Nếu người dùng đang xem "hôm nay" mà đồng hồ sang ngày mới thì tự
        // chuyển theo; còn đang xem ngày cũ thì giữ nguyên để không mất ngữ cảnh.
        viewModelScope.launch {
            var lastToday = DateUtils.todayKey()
            while (isActive) {
                delay(30_000L)
                val now = DateUtils.todayKey()
                if (now != lastToday) {
                    _today.value = now
                    if (_selectedDate.value == lastToday) _selectedDate.value = now
                    lastToday = now
                }
            }
        }
    }

    val uiState: StateFlow<DiaryUiState> = combine(_selectedDate, _today) { date, today ->
        date to today
    }.flatMapLatest { (date, today) ->
        combine(
            userRepository.observeProfile(userId),
            mealRepository.observeDay(userId, date),
            mealRepository.observeDayNutrition(userId, date),
        ) { profile, meals, nutrition ->
            DiaryUiState(
                loading = false,
                date = date,
                today = today,
                profile = profile,
                meals = meals,
                nutrition = nutrition,
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DiaryUiState(),
        )

    fun selectDate(dateKey: String) {
        _selectedDate.value = dateKey
    }

    fun previousDay() {
        _selectedDate.value = DateUtils.addDays(_selectedDate.value, -1)
    }

    fun nextDay() {
        val next = DateUtils.addDays(_selectedDate.value, 1)
        if (!DateUtils.isFuture(next)) _selectedDate.value = next
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch { mealRepository.deleteItem(itemId) }
    }

    fun deleteMeal(mealId: String) {
        viewModelScope.launch { mealRepository.deleteMeal(mealId) }
    }
}
