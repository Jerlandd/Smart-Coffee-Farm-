package com.example.smartcoffeefarm.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcoffeefarm.data.CoffeeAlgorithmEngine
import com.example.smartcoffeefarm.databinding.ActivityBiayaProduksiBinding

class BiayaProduksiActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBiayaProduksiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBiayaProduksiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val totalYieldKg = 3480.0
        val fertilizerCost = 2400000.0
        val laborCost = 3100000.0
        val pesticideCost = 1200000.0

        val costResult = CoffeeAlgorithmEngine.calculateProductionCost(totalYieldKg, fertilizerCost, laborCost, pesticideCost)

        val totalCostJt = costResult.totalCostRp / 1000000.0
        binding.tvTotalCost.text = String.format("Rp %.1f jt", totalCostJt)
        binding.tvCostPerKg.text = String.format("Rp %,d", costResult.costPerKgRp.toInt())

        binding.btnTambahBiaya.setOnClickListener {
            Toast.makeText(this, "Biaya produksi berhasil ditambahkan", Toast.LENGTH_SHORT).show()
        }
    }
}
