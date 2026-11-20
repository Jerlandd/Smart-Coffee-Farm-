package com.example.smartcoffeefarm.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CoffeeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM coffee_plots WHERE userId = :userId ORDER BY id DESC")
    fun getAllPlotsForUser(userId: Long): Flow<List<CoffeePlot>>

    @Query("SELECT * FROM coffee_plots WHERE userId = :userId AND (name LIKE '%' || :query || '%' OR variety LIKE '%' || :query || '%' OR healthStatus LIKE '%' || :query || '%') ORDER BY id DESC")
    fun searchPlotsForUser(userId: Long, query: String): Flow<List<CoffeePlot>>

    @Query("SELECT * FROM coffee_plots WHERE id = :plotId LIMIT 1")
    suspend fun getPlotById(plotId: Long): CoffeePlot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlot(plot: CoffeePlot)

    @Update
    suspend fun updatePlot(plot: CoffeePlot)

    @Delete
    suspend fun deletePlot(plot: CoffeePlot)

    @Query("SELECT * FROM harvest_records WHERE userId = :userId ORDER BY id DESC")
    fun getAllHarvestRecordsForUser(userId: Long): Flow<List<HarvestRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHarvestRecord(record: HarvestRecord)

    @Query("DELETE FROM harvest_records WHERE userId = :userId")
    suspend fun clearHarvestRecordsForUser(userId: Long)

    @Query("SELECT * FROM cost_records WHERE userId = :userId ORDER BY id DESC")
    fun getAllCostRecordsForUser(userId: Long): Flow<List<CostRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCostRecord(record: CostRecord)

    @Delete
    suspend fun deleteCostRecord(record: CostRecord)

    @Query("DELETE FROM cost_records WHERE userId = :userId")
    suspend fun clearCostRecordsForUser(userId: Long)
}
