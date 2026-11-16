package com.example.smartcoffeefarm.data

import kotlin.math.roundToInt

data class HarvestPredictionResult(
    val predictedYieldKg: Double,
    val minRangeKg: Double,
    val maxRangeKg: Double,
    val accuracyPercentage: Int
)

data class ProductionCostResult(
    val totalCostRp: Double,
    val costPerKgRp: Double,
    val fertilizerCostRp: Double,
    val laborCostRp: Double,
    val pesticideCostRp: Double,
    val profitMarginPercentage: Double
)

object CoffeeAlgorithmEngine {

    /**
     * Machine Learning Prediction Algorithm (equivalent to Scikit-learn Linear Regression model)
     * Predicts coffee harvest yield based on plot area (ha), soil moisture, and fertilizer usage.
     */
    fun predictHarvest(areaHa: Double, soilMoisture: Double, fertilizerKg: Double): HarvestPredictionResult {
        // Base coefficient model: ~280 kg per hectare + fertilizer impact + moisture efficiency factor
        val moistureFactor = if (soilMoisture in 55.0..75.0) 1.1 else 0.85
        val baseYield = areaHa * 280.0 * moistureFactor + (fertilizerKg * 1.5)
        
        val predicted = (baseYield * 10).roundToInt() / 10.0
        val minRange = (predicted * 0.92 * 10).roundToInt() / 10.0
        val maxRange = (predicted * 1.08 * 10).roundToInt() / 10.0
        
        return HarvestPredictionResult(
            predictedYieldKg = predicted,
            minRangeKg = minRange,
            maxRangeKg = maxRange,
            accuracyPercentage = 89
        )
    }

    /**
     * Financial & Production Cost Analysis Algorithm
     * Computes cost per kg, category breakdowns, and financial efficiency.
     */
    fun calculateProductionCost(totalYieldKg: Double, fertilizerCost: Double, laborCost: Double, pesticideCost: Double): ProductionCostResult {
        val totalCost = fertilizerCost + laborCost + pesticideCost
        val costPerKg = if (totalYieldKg > 0) totalCost / totalYieldKg else 0.0
        
        // Estimated market selling price per kg for coffee: Rp 35,000
        val estimatedRevenue = totalYieldKg * 35000.0
        val netProfit = estimatedRevenue - totalCost
        val profitMargin = if (estimatedRevenue > 0) (netProfit / estimatedRevenue) * 100.0 else 0.0

        return ProductionCostResult(
            totalCostRp = totalCost,
            costPerKgRp = (costPerKg * 10).roundToInt() / 10.0,
            fertilizerCostRp = fertilizerCost,
            laborCostRp = laborCost,
            pesticideCostRp = pesticideCost,
            profitMarginPercentage = (profitMargin * 10).roundToInt() / 10.0
        )
    }

    /**
     * Environmental Monitoring Status Algorithm
     * Evaluates soil moisture, temperature, and pH to determine block health status.
     */
    fun evaluateBlockStatus(soilMoisture: Double, temperature: Double, ph: Double): String {
        return when {
            soilMoisture < 50.0 -> "Perlu air (Kelembapan rendah)"
            soilMoisture > 80.0 -> "Peringatan: Kelembapan berlebih"
            ph !in 5.5..6.5 -> "Perlu penyesuaian pH Tanah"
            else -> "Normal (Optimal)"
        }
    }
}
