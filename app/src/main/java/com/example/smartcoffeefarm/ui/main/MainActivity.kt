package com.example.smartcoffeefarm.ui.main

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcoffeefarm.R
import com.example.smartcoffeefarm.data.HarvestRecord
import com.example.smartcoffeefarm.databinding.ActivityMainBinding
import com.example.smartcoffeefarm.ui.auth.LoginActivity
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: CoffeeViewModel by viewModels()
    private var userId: Long = 1L
    private var username: String = ""

    private lateinit var coffeeAdapter: CoffeeAdapter
    private var currentTab: Int = R.id.nav_beranda

    private var rvPlots: RecyclerView? = null
    private var containerRiwayat: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getLongExtra("USER_ID", 1L)
        username = intent.getStringExtra("USERNAME") ?: "Petani"
        viewModel.setUserId(userId)

        setupAdapter()
        setupObservers()
        setupHeader()
        setupBottomNavigation()

        loadBerandaTab()
    }

    private fun setupAdapter() {
        coffeeAdapter = CoffeeAdapter { plot ->
            val intent = Intent(this, CoffeeDetailActivity::class.java).apply {
                putExtra("PLOT_ID", plot.id)
                putExtra("USER_ID", userId)
            }
            startActivity(intent)
        }
    }

    private fun setupObservers() {
        viewModel.coffeePlots.observe(this) { plots ->
            if (currentTab == R.id.nav_kebun) {
                rvPlots?.adapter = coffeeAdapter
                coffeeAdapter.submitList(plots)
            }
        }

        viewModel.harvestRecords.observe(this) { records ->
            if (currentTab == R.id.nav_panen) {
                renderHarvestRecords(records)
            }
        }
    }

    private fun setupHeader() {
        binding.ivProfile.setOnClickListener {
            showProfileDialog()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            currentTab = item.itemId
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
        currentTab = R.id.nav_beranda
        binding.tvHeaderTitle.text = "Dashboard"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_beranda, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)
    }

    private fun loadKebunTab() {
        currentTab = R.id.nav_kebun
        binding.tvHeaderTitle.text = "Manajemen Kebun"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_kebun, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)

        rvPlots = view.findViewById(R.id.rvPlots)
        val etSearch = view.findViewById<TextInputEditText>(R.id.etSearchKebun)

        rvPlots?.layoutManager = LinearLayoutManager(this)
        rvPlots?.adapter = coffeeAdapter
        coffeeAdapter.submitList(viewModel.coffeePlots.value)

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
        currentTab = R.id.nav_panen
        binding.tvHeaderTitle.text = "Catat Panen"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_panen, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)

        val etTanggal = view.findViewById<EditText>(R.id.etTanggal)
        val etBlok = view.findViewById<EditText>(R.id.etBlok)
        val etJumlah = view.findViewById<EditText>(R.id.etJumlah)
        val etKualitas = view.findViewById<EditText>(R.id.etKualitas)
        val btnSimpan = view.findViewById<View>(R.id.btnSimpanPanen)
        val btnReset = view.findViewById<View>(R.id.btnResetRiwayat)
        containerRiwayat = view.findViewById(R.id.containerRiwayat)

        etTanggal?.setText("07 November 2026")
        renderHarvestRecords(viewModel.harvestRecords.value)

        btnReset?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Riwayat Panen")
                .setMessage("Apakah Anda yakin ingin menghapus semua riwayat panen?")
                .setPositiveButton("Hapus") { _, _ ->
                    viewModel.clearHarvestRecords()
                    Toast.makeText(this, "Riwayat panen berhasil direset", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Batal", null)
                .show()
        }

        btnSimpan?.setOnClickListener {
            val tanggal = etTanggal?.text?.toString()?.trim().orEmpty()
            val blok = etBlok?.text?.toString()?.trim().orEmpty()
            val jumlahStr = etJumlah?.text?.toString()?.trim().orEmpty()
            val kualitas = etKualitas?.text?.toString()?.trim().orEmpty()

            if (blok.isEmpty()) {
                etBlok?.error = "Nama blok tidak boleh kosong"
                return@setOnClickListener
            }

            val jumlahKg = jumlahStr.toIntOrNull() ?: 0
            if (jumlahKg <= 0) {
                etJumlah?.error = "Jumlah panen harus lebih dari 0"
                return@setOnClickListener
            }

            val record = HarvestRecord(
                userId = userId,
                date = tanggal.ifEmpty { "07 November 2026" },
                blockName = blok,
                quantityKg = jumlahKg,
                quality = kualitas.ifEmpty { "Grade A" }
            )

            viewModel.insertHarvestRecord(record)
            Toast.makeText(this, "Data panen berhasil disimpan", Toast.LENGTH_SHORT).show()

            etBlok?.setText("")
            etJumlah?.setText("0")
            etKualitas?.setText("")
        }
    }

    private fun renderHarvestRecords(records: List<HarvestRecord>?) {
        containerRiwayat?.removeAllViews()
        if (records.isNullOrEmpty()) {
            val tv = TextView(this).apply {
                text = "Belum ada riwayat panen"
                setTextColor(resources.getColor(R.color.text_secondary, theme))
                textSize = 14f
            }
            containerRiwayat?.addView(tv)
        } else {
            for ((index, record) in records.withIndex()) {
                val rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                val tvInfo = TextView(this).apply {
                    text = "${record.date} · ${record.blockName} (${record.quality})"
                    setTextColor(resources.getColor(R.color.text_primary, theme))
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                }

                val tvQty = TextView(this).apply {
                    text = "${record.quantityKg} kg"
                    setTextColor(resources.getColor(R.color.text_primary, theme))
                    textSize = 14f
                    typeface = Typeface.DEFAULT_BOLD
                }

                rowLayout.addView(tvInfo)
                rowLayout.addView(tvQty)
                containerRiwayat?.addView(rowLayout)

                if (index < records.size - 1) {
                    val divider = View(this).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                        ).apply {
                            setMargins(0, 16, 0, 16)
                        }
                        setBackgroundColor(Color.parseColor("#E0E0E0"))
                    }
                    containerRiwayat?.addView(divider)
                }
            }
        }
    }

    private fun loadMonitorTab() {
        currentTab = R.id.nav_monitor
        binding.tvHeaderTitle.text = "Monitoring Kebun"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_monitor, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)
    }

    private fun loadLainnyaTab() {
        currentTab = R.id.nav_lainnya
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
