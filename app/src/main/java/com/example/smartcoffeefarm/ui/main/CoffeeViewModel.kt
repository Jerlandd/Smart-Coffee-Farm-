package com.example.smartcoffeefarm.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.smartcoffeefarm.data.CoffeeDatabase
import com.example.smartcoffeefarm.data.CoffeePlot
import com.example.smartcoffeefarm.data.CoffeeRepository
import com.example.smartcoffeefarm.data.HarvestRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CoffeeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CoffeeRepository
    private var currentUserId: Long = 1L

    private val _userIdFlow = MutableStateFlow(1L)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> get() = _searchQuery

    val coffeePlots: LiveData<List<CoffeePlot>>
    val harvestRecords: LiveData<List<HarvestRecord>>

    init {
        val dao = CoffeeDatabase.getDatabase(application).coffeeDao()
        repository = CoffeeRepository(dao)

        coffeePlots = combine(_userIdFlow, _searchQuery) { userId, query ->
            Pair(userId, query)
        }.flatMapLatest { (userId, query) ->
            if (query.isBlank()) {
                repository.getAllPlotsForUser(userId)
            } else {
                repository.searchPlotsForUser(userId, query)
            }
        }.asLiveData()

        harvestRecords = _userIdFlow.flatMapLatest { userId ->
            repository.getAllHarvestRecordsForUser(userId)
        }.asLiveData()
    }

    fun setUserId(userId: Long) {
        currentUserId = userId
        _userIdFlow.value = userId
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

    fun insertHarvestRecord(record: HarvestRecord) {
        viewModelScope.launch {
            repository.insertHarvestRecord(record.copy(userId = currentUserId))
        }
    }

    fun clearHarvestRecords() {
        viewModelScope.launch {
            repository.clearHarvestRecordsForUser(currentUserId)
        }
    }
}
