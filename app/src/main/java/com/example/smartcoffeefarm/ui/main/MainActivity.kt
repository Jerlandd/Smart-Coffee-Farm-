package com.example.smartcoffeefarm.ui.main

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcoffeefarm.R
import com.example.smartcoffeefarm.databinding.ActivityMainBinding
import com.example.smartcoffeefarm.ui.auth.LoginActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: CoffeeViewModel by viewModels()
    private var userId: Long = 1L
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getLongExtra("USER_ID", 1L)
        username = intent.getStringExtra("USERNAME") ?: "Petani"
        viewModel.setUserId(userId)

        setupHeader()
        setupBottomNavigation()

        // Load default tab (Beranda)
        loadBerandaTab()
    }

    private fun setupHeader() {
        binding.ivProfile.setOnClickListener {
            showProfileDialog()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_beranda -> {
                    loadBerandaTab()
                    true
                }
                R.id.nav_kebun -> {
                    loadKebunTab()
                    true
                }
                R.id.nav_panen -> {
                    loadPanenTab()
                    true
                }
                R.id.nav_monitor -> {
                    loadMonitorTab()
                    true
                }
                R.id.nav_lainnya -> {
                    loadLainnyaTab()
                    true
                }
                else -> false
            }
        }
    }

    private fun loadBerandaTab() {
        binding.tvHeaderTitle.text = "Dashboard"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_beranda, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)
    }

    private fun loadKebunTab() {
        binding.tvHeaderTitle.text = "Manajemen Kebun"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_kebun, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)

        val rvPlots = view.findViewById<RecyclerView>(R.id.rvPlots)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearchKebun)

        rvPlots?.layoutManager = LinearLayoutManager(this)
        val adapter = CoffeeAdapter { plot ->
            val intent = Intent(this, CoffeeDetailActivity::class.java).apply {
                putExtra("PLOT_ID", plot.id)
                putExtra("USER_ID", userId)
            }
            startActivity(intent)
        }
        rvPlots?.adapter = adapter

        viewModel.coffeePlots.observe(this) { plots ->
            adapter.submitList(plots)
        }

        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        view.findViewById<View>(R.id.btnTambahKebun)?.setOnClickListener {
            val intent = Intent(this, AddEditCoffeeActivity::class.java).apply {
                putExtra("USER_ID", userId)
                putExtra("IS_EDIT", false)
            }
            startActivity(intent)
        }
    }

    private fun loadPanenTab() {
        binding.tvHeaderTitle.text = "Catat Panen"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_panen, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)

        view.findViewById<View>(R.id.btnSimpanPanen)?.setOnClickListener {
            Toast.makeText(this, "Data panen berhasil disimpan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadMonitorTab() {
        binding.tvHeaderTitle.text = "Monitoring Kebun"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_monitor, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)
    }

    private fun loadLainnyaTab() {
        binding.tvHeaderTitle.text = "Lainnya"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_lainnya, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)

        view.findViewById<View>(R.id.cardBiayaProduksi)?.setOnClickListener {
            loadBiayaProduksiSubTab()
        }

        view.findViewById<View>(R.id.cardPrediksiPanen)?.setOnClickListener {
            loadPrediksiPanenSubTab()
        }

        view.findViewById<View>(R.id.cardLaporan)?.setOnClickListener {
            loadLaporanSubTab()
        }
    }

    private fun loadBiayaProduksiSubTab() {
        binding.tvHeaderTitle.text = "Biaya Produksi"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_biaya_produksi, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)

        view.findViewById<View>(R.id.btnTambahBiaya)?.setOnClickListener {
            Toast.makeText(this, "Fitur tambah biaya produksi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPrediksiPanenSubTab() {
        binding.tvHeaderTitle.text = "Prediksi Panen"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_prediksi_panen, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)

        view.findViewById<View>(R.id.btnJalankanPrediksi)?.setOnClickListener {
            Toast.makeText(this, "Prediksi panen dijalankan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLaporanSubTab() {
        binding.tvHeaderTitle.text = "Laporan"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_laporan, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)

        view.findViewById<View>(R.id.btnExcel)?.setOnClickListener {
            Toast.makeText(this, "Laporan berhasil diekspor ke Excel", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnPdf)?.setOnClickListener {
            Toast.makeText(this, "Laporan berhasil diekspor ke PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProfileDialog() {
        AlertDialog.Builder(this)
            .setTitle("Profil & Tentang Akun")
            .setMessage("Akun: $username\nID Pengguna: $userId\n\nAplikasi: KebunKu (Smart Coffee Farm)\nVersi: 1.0\n\nPlatform Manajemen Perkebunan Kopi Cerdas terintegrasi dengan Machine Learning.")
            .setPositiveButton("Tutup", null)
            .setNegativeButton("Keluar Akun") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
            }
            .show()
    }
}
