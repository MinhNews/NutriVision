package com.example.nutrivision.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nutrivision.data.repository.FoodRepository
import com.example.nutrivision.domain.model.Food
import com.example.nutrivision.domain.text.tr
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class FoodFilter(private val labelVi: String, private val labelEn: String, val kind: String?) {
    ALL("Tất cả", "All", null),
    DISH("Món ăn", "Dishes", Food.KIND_DISH),
    DRINK("Đồ uống", "Drinks", Food.KIND_DRINK);

    val label: String get() = tr(labelVi, labelEn)
}

data class SearchUiState(
    val query: String = "",
    val filter: FoodFilter = FoodFilter.ALL,
    val foods: List<Food> = emptyList(),
    val loading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModel(
    private val foodRepository: FoodRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow(FoodFilter.ALL)
    val filter: StateFlow<FoodFilter> = _filter.asStateFlow()

    val uiState: StateFlow<SearchUiState> = combine(_query, _filter) { query, filter ->
        query to filter
    }.flatMapLatest { (query, filter) ->
        foodRepository.search(query, filter.kind).map { foods ->
            SearchUiState(
                query = query,
                filter = filter,
                foods = foods,
                loading = false,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState(),
    )

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setFilter(filter: FoodFilter) {
        _filter.value = filter
    }
}
