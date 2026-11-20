package com.example.smartcoffeefarm.data

import kotlinx.coroutines.flow.Flow

class CoffeeRepository(private val coffeeDao: CoffeeDao) {

    suspend fun registerUser(user: User): Long = coffeeDao.insertUser(user)

    suspend fun getUserByUsername(username: String): User? = coffeeDao.getUserByUsername(username)

    fun getAllPlotsForUser(userId: Long): Flow<List<CoffeePlot>> = coffeeDao.getAllPlotsForUser(userId)

    fun searchPlotsForUser(userId: Long, query: String): Flow<List<CoffeePlot>> = coffeeDao.searchPlotsForUser(userId, query)

    suspend fun getPlotById(plotId: Long): CoffeePlot? = coffeeDao.getPlotById(plotId)

    suspend fun insertPlot(plot: CoffeePlot) = coffeeDao.insertPlot(plot)

    suspend fun updatePlot(plot: CoffeePlot) = coffeeDao.updatePlot(plot)

    suspend fun deletePlot(plot: CoffeePlot) = coffeeDao.deletePlot(plot)

    fun getAllHarvestRecordsForUser(userId: Long): Flow<List<HarvestRecord>> = coffeeDao.getAllHarvestRecordsForUser(userId)

    suspend fun insertHarvestRecord(record: HarvestRecord) = coffeeDao.insertHarvestRecord(record)

    suspend fun clearHarvestRecordsForUser(userId: Long) = coffeeDao.clearHarvestRecordsForUser(userId)

    fun getAllCostRecordsForUser(userId: Long): Flow<List<CostRecord>> = coffeeDao.getAllCostRecordsForUser(userId)

    suspend fun insertCostRecord(record: CostRecord) = coffeeDao.insertCostRecord(record)

    suspend fun deleteCostRecord(record: CostRecord) = coffeeDao.deleteCostRecord(record)

    suspend fun clearCostRecordsForUser(userId: Long) = coffeeDao.clearCostRecordsForUser(userId)
}
