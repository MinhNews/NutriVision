package com.example.nutrivision.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.repository.MealRepository
import com.example.nutrivision.data.repository.UserRepository
import com.example.nutrivision.domain.calc.DateUtils
import com.example.nutrivision.domain.model.DayTotal
import com.example.nutrivision.domain.model.Nutrition
import com.example.nutrivision.domain.model.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class StatsRange { WEEK, MONTH }

data class StatsUiState(
    val loading: Boolean = true,
    val range: StatsRange = StatsRange.WEEK,
    val days: List<DayTotal> = emptyList(),
    val nutrition: Nutrition = Nutrition.ZERO,
    val profile: UserProfile? = null,
    val canGoNextMonth: Boolean = false,
) {
    val fromDate: String? get() = days.firstOrNull()?.date
    val toDate: String? get() = days.lastOrNull()?.date
    val hasData: Boolean get() = days.any { it.calories > 0.0 }
    val totalCalories: Double get() = days.sumOf { it.calories }
    val loggedDays: Int get() = days.count { it.calories > 0.0 }
    val dayCount: Int get() = days.size
    val averagePerLoggedDay: Int?
        get() = if (loggedDays > 0) (totalCalories / loggedDays).roundToInt() else null
    val bestDay: DayTotal?
        get() = days.filter { it.calories > 0.0 }.maxByOrNull { it.calories }
}

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    userRepository: UserRepository,
    mealRepository: MealRepository,
) : ViewModel() {

    private val userId = UserRepository.LOCAL_USER_ID
    private val _range = MutableStateFlow(StatsRange.WEEK)
    private val _month = MutableStateFlow(DateUtils.firstOfMonth(DateUtils.todayKey()))
    private val _today = MutableStateFlow(DateUtils.todayKey())

    init {
        viewModelScope.launch {
            var lastToday = DateUtils.todayKey()
            while (isActive) {
                delay(30_000L)
                val now = DateUtils.todayKey()
                if (now != lastToday) {
                    _today.value = now
                    // Nếu đang xem tháng hiện tại thì tự chuyển sang tháng mới.
                    if (_month.value == DateUtils.firstOfMonth(lastToday)) {
                        _month.value = DateUtils.firstOfMonth(now)
                    }
                    lastToday = now
                }
            }
        }
    }

    val uiState: StateFlow<StatsUiState> = combine(_range, _month, _today) { range, month, today ->
        Triple(range, month, today)
    }.flatMapLatest { (range, month, today) ->
        val days = when (range) {
            StatsRange.WEEK -> DateUtils.lastNDays(7, today)
            StatsRange.MONTH -> DateUtils.daysOfMonth(month)
        }
        val fromDate = days.first()
        val toDate = days.last()
        combine(
            mealRepository.observeDailyTotals(userId, fromDate, toDate),
            mealRepository.observeRangeNutrition(userId, fromDate, toDate),
            userRepository.observeProfile(userId),
        ) { totals, nutrition, profile ->
            val byDate = totals.associateBy { it.date }
            StatsUiState(
                loading = false,
                range = range,
                days = days.map { date -> DayTotal(date = date, calories = byDate[date]?.calories ?: 0.0) },
                nutrition = nutrition,
                profile = profile,
                canGoNextMonth = DateUtils.firstOfMonth(month) < DateUtils.firstOfMonth(today),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(
            days = DateUtils.lastNDays(7).map { DayTotal(it, 0.0) },
        ),
    )

    fun selectRange(range: StatsRange) {
        _range.value = range
    }

    fun previousMonth() {
        _month.value = DateUtils.addMonths(_month.value, -1)
    }

    fun nextMonth() {
        val next = DateUtils.addMonths(_month.value, 1)
        if (DateUtils.firstOfMonth(next) <= DateUtils.firstOfMonth(_today.value)) {
            _month.value = next
        }
    }
}
