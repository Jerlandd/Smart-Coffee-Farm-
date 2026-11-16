package com.example.smartcoffeefarm.ui.main

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.smartcoffeefarm.databinding.ActivityLaporanBinding

class LaporanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLaporanBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLaporanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnExcel.setOnClickListener {
            Toast.makeText(this, "Laporan berhasil diekspor ke Excel", Toast.LENGTH_SHORT).show()
        }

        binding.btnPdf.setOnClickListener {
            Toast.makeText(this, "Laporan berhasil diekspor ke PDF", Toast.LENGTH_SHORT).show()
        }
    }
}
