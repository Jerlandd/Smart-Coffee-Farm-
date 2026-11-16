package com.example.smartcoffeefarm.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartcoffeefarm.data.CoffeeDatabase
import com.example.smartcoffeefarm.data.CoffeePlot
import com.example.smartcoffeefarm.data.CoffeeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CoffeeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CoffeeRepository
    private var currentUserId: Long = 1L

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> get() = _searchQuery

    val coffeePlots: LiveData<List<CoffeePlot>>

    init {
        val dao = CoffeeDatabase.getDatabase(application).coffeeDao()
        repository = CoffeeRepository(dao)

        coffeePlots = _searchQuery.flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllPlotsForUser(currentUserId)
            } else {
                repository.searchPlotsForUser(currentUserId, query)
            }
        }.asLiveData()
    }

    fun setUserId(userId: Long) {
        currentUserId = userId
        // trigger reload by resetting query or re-assigning
        val q = _searchQuery.value
        _searchQuery.value = ""
        _searchQuery.value = q
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertPlot(plot: CoffeePlot) {
        viewModelScope.launch {
            repository.insertPlot(plot.copy(userId = currentUserId))
        }
    }

    fun updatePlot(plot: CoffeePlot) {
        viewModelScope.launch {
            repository.updatePlot(plot)
        }
    }

    fun deletePlot(plot: CoffeePlot) {
        viewModelScope.launch {
            repository.deletePlot(plot)
        }
    }
}
