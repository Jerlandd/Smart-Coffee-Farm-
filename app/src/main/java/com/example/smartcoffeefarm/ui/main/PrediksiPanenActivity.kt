package com.example.smartcoffeefarm.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcoffeefarm.data.CoffeeAlgorithmEngine
import com.example.smartcoffeefarm.databinding.ActivityPrediksiPanenBinding

class PrediksiPanenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrediksiPanenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrediksiPanenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnJalankanPrediksi.setOnClickListener {
            val areaHa = 1.25
            val soilMoisture = 68.0
            val fertilizerKg = 160.0

            val result = CoffeeAlgorithmEngine.predictHarvest(areaHa, soilMoisture, fertilizerKg)

            binding.tvPredictedYield.text = "${result.predictedYieldKg} kg"
            binding.tvPredictionDetails.text = "Rentang ${result.minRangeKg}-${result.maxRangeKg} kg · Akurasi ${result.accuracyPercentage}%"

            Toast.makeText(this, "Prediksi panen berhasil dijalankan", Toast.LENGTH_SHORT).show()
        }
    }
}
