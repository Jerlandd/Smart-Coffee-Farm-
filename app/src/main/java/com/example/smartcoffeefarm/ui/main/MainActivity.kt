package com.example.smartcoffeefarm.ui.main

import android.content.ContentValues
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcoffeefarm.R
import com.example.smartcoffeefarm.data.CoffeePlot
import com.example.smartcoffeefarm.data.CostRecord
import com.example.smartcoffeefarm.data.HarvestRecord
import com.example.smartcoffeefarm.databinding.ActivityMainBinding
import com.example.smartcoffeefarm.ui.auth.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: CoffeeViewModel by viewModels()
    private var userId: Long = 1L
    private var username: String = ""
    private var selectedReportYear: Int = Calendar.getInstance().get(Calendar.YEAR)

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
            if (currentTab == R.id.nav_beranda) {
                val berandaView = binding.fragmentContainer.getChildAt(0)
                if (berandaView != null) {
                    updateBerandaStats(berandaView, plots, viewModel.harvestRecords.value)
                }
            }
        }

        viewModel.harvestRecords.observe(this) { records ->
            if (currentTab == R.id.nav_panen) {
                val calendar = Calendar.getInstance()
                val currentMonthIdx = calendar.get(Calendar.MONTH)
                val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
                val currentMonthName = monthNames[currentMonthIdx]
                val currentMonthRecords = records?.filter { it.date.contains(currentMonthName, ignoreCase = true) }
                renderHarvestRecords(currentMonthRecords)
            }
            if (currentTab == R.id.nav_beranda) {
                val berandaView = binding.fragmentContainer.getChildAt(0)
                if (berandaView != null) {
                    updateBerandaStats(berandaView, viewModel.coffeePlots.value, records)
                }
            }
        }

        viewModel.costRecords.observe(this) { costRecords ->
            if (currentTab == R.id.nav_beranda) {
                val berandaView = binding.fragmentContainer.getChildAt(0)
                if (berandaView != null) {
                    updateDashboardBiayaCard(berandaView, costRecords)
                }
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
        updateBerandaStats(view, viewModel.coffeePlots.value, viewModel.harvestRecords.value)
        updateDashboardBiayaCard(view, viewModel.costRecords.value)
    }

    private fun updateDashboardBiayaCard(view: View, records: List<CostRecord>?) {
        val tvBiayaDashboard = view.findViewById<TextView>(R.id.tvBiayaDashboard)
        val totalCost = records?.sumOf { it.amountRp } ?: 18500000.0
        tvBiayaDashboard?.text = formatRupiahJt(totalCost)
    }

    private fun updateBerandaStats(view: View, plots: List<CoffeePlot>?, records: List<HarvestRecord>?) {
        val tvLuasKebun = view.findViewById<TextView>(R.id.tvLuasKebun)
        val totalArea = plots?.sumOf { it.areaHa } ?: 0.0
        tvLuasKebun?.text = "${String.format(Locale.forLanguageTag("id"), "%.1f", totalArea)} ha"

        val tvPanenBulanIni = view.findViewById<TextView>(R.id.tvPanenBulanIni)
        val calendar = Calendar.getInstance()
        val currentMonthIdx = calendar.get(Calendar.MONTH)
        val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val shortMonthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")
        val currentMonthName = monthNames[currentMonthIdx]

        val currentMonthRecords = records?.filter { it.date.contains(currentMonthName, ignoreCase = true) }
        val currentMonthTotal = if (!currentMonthRecords.isNullOrEmpty()) {
            currentMonthRecords.sumOf { it.quantityKg }
        } else {
            records?.sumOf { it.quantityKg } ?: 0
        }

        tvPanenBulanIni?.text = "${NumberFormat.getInstance(Locale.forLanguageTag("id")).format(currentMonthTotal)} kg"

        val bars = listOf(
            view.findViewById<View>(R.id.bar1),
            view.findViewById<View>(R.id.bar2),
            view.findViewById<View>(R.id.bar3),
            view.findViewById<View>(R.id.bar4),
            view.findViewById<View>(R.id.bar5),
            view.findViewById<View>(R.id.bar6),
            view.findViewById<View>(R.id.bar7),
            view.findViewById<View>(R.id.bar8),
            view.findViewById<View>(R.id.bar9),
            view.findViewById<View>(R.id.bar10),
            view.findViewById<View>(R.id.bar11),
            view.findViewById<View>(R.id.bar12)
        )

        val monthLabels = listOf(
            view.findViewById<TextView>(R.id.tvMonth1),
            view.findViewById<TextView>(R.id.tvMonth2),
            view.findViewById<TextView>(R.id.tvMonth3),
            view.findViewById<TextView>(R.id.tvMonth4),
            view.findViewById<TextView>(R.id.tvMonth5),
            view.findViewById<TextView>(R.id.tvMonth6),
            view.findViewById<TextView>(R.id.tvMonth7),
            view.findViewById<TextView>(R.id.tvMonth8),
            view.findViewById<TextView>(R.id.tvMonth9),
            view.findViewById<TextView>(R.id.tvMonth10),
            view.findViewById<TextView>(R.id.tvMonth11),
            view.findViewById<TextView>(R.id.tvMonth12)
        )

        val valueLabels = listOf(
            view.findViewById<TextView>(R.id.tvValue1),
            view.findViewById<TextView>(R.id.tvValue2),
            view.findViewById<TextView>(R.id.tvValue3),
            view.findViewById<TextView>(R.id.tvValue4),
            view.findViewById<TextView>(R.id.tvValue5),
            view.findViewById<TextView>(R.id.tvValue6),
            view.findViewById<TextView>(R.id.tvValue7),
            view.findViewById<TextView>(R.id.tvValue8),
            view.findViewById<TextView>(R.id.tvValue9),
            view.findViewById<TextView>(R.id.tvValue10),
            view.findViewById<TextView>(R.id.tvValue11),
            view.findViewById<TextView>(R.id.tvValue12)
        )

        val monthlyTotals = IntArray(12)
        for (i in 0 until 12) {
            val monthOffsetIdx = (currentMonthIdx - (11 - i) + 1200) % 12
            val mName = monthNames[monthOffsetIdx]
            monthLabels[i]?.text = shortMonthNames[monthOffsetIdx]
            val totalForMonth = records?.filter { it.date.contains(mName, ignoreCase = true) }?.sumOf { it.quantityKg } ?: 0
            monthlyTotals[i] = totalForMonth
        }

        if (monthlyTotals.all { it == 0 } && !records.isNullOrEmpty()) {
            monthlyTotals[11] = records.sumOf { it.quantityKg }
        }

        val maxQty = maxOf(1, monthlyTotals.maxOrNull() ?: 1)
        val density = resources.displayMetrics.density

        for (i in 0 until 12) {
            val qty = monthlyTotals[i]
            if (qty > 0) {
                valueLabels[i]?.text = if (qty >= 1000) "${qty / 1000}k" else qty.toString()
            } else {
                valueLabels[i]?.text = ""
            }

            val heightDp = if (qty > 0) {
                ((qty.toFloat() / maxQty) * 90f).coerceIn(12f, 100f)
            } else {
                12f
            }
            val heightPx = (heightDp * density).toInt()
            bars[i]?.layoutParams?.height = heightPx
            bars[i]?.requestLayout()
        }
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

        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id"))
        etTanggal?.setText(dateFormat.format(Date()))

        val calendar = Calendar.getInstance()
        val currentMonthIdx = calendar.get(Calendar.MONTH)
        val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
        val currentMonthName = monthNames[currentMonthIdx]

        val currentMonthRecords = viewModel.harvestRecords.value?.filter { it.date.contains(currentMonthName, ignoreCase = true) }
        renderHarvestRecords(currentMonthRecords)

        btnReset?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Riwayat Panen Bulan Ini")
                .setMessage("Apakah Anda yakin ingin menghapus riwayat panen bulan ini?")
                .setPositiveButton("Hapus") { _, _ ->
                    viewModel.clearHarvestRecords()
                    Toast.makeText(this, "Riwayat panen bulan ini berhasil direset", Toast.LENGTH_SHORT).show()
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
                date = tanggal.ifEmpty { dateFormat.format(java.util.Date()) },
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

    private var customMarketPricePerKg: Double = 5690.0

    private fun formatRupiah(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        return "Rp ${formatter.format(amount)}"
    }

    private fun formatRupiahJt(amount: Double): String {
        if (amount >= 1000000) {
            val jt = amount / 1000000.0
            val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
                groupingSeparator = '.'
                decimalSeparator = ','
            }
            val formatter = DecimalFormat("#,##0.#", symbols)
            return "Rp ${formatter.format(jt)} jt"
        } else {
            return formatRupiah(amount)
        }
    }

    private fun loadBiayaProduksiSubTab() {
        binding.tvHeaderTitle.text = "Biaya Produksi"
        binding.tvHeaderSubtitle.text = "KebunKu"
        binding.fragmentContainer.removeAllViews()
        val view = LayoutInflater.from(this).inflate(R.layout.fragment_biaya_produksi, binding.fragmentContainer, false)
        binding.fragmentContainer.addView(view)

        val tvTotalCost = view.findViewById<TextView>(R.id.tvTotalCost)
        val tvCostPerKg = view.findViewById<TextView>(R.id.tvCostPerKg)
        val cardPerKg = view.findViewById<View>(R.id.cardPerKg)
        val containerPerKategori = view.findViewById<LinearLayout>(R.id.containerPerKategori)
        val containerRiwayatBiaya = view.findViewById<LinearLayout>(R.id.containerRiwayatBiaya)
        val btnTambahBiaya = view.findViewById<View>(R.id.btnTambahBiaya)

        val updateBiayaUI = { records: List<CostRecord>? ->
            val totalCost = records?.sumOf { it.amountRp } ?: 0.0
            tvTotalCost?.text = formatRupiahJt(totalCost)

            val totalYieldKg = viewModel.harvestRecords.value?.sumOf { it.quantityKg.toDouble() } ?: 0.0
            val perKg = if (totalYieldKg > 0) totalCost / totalYieldKg else 0.0
            tvCostPerKg?.text = formatRupiah(perKg)

            containerPerKategori?.removeAllViews()
            val categoryMap = records?.groupBy { it.category } ?: emptyMap()
            if (categoryMap.isEmpty()) {
                val tv = TextView(this).apply {
                    text = "Belum ada kategori biaya"
                    setTextColor(resources.getColor(R.color.text_secondary, theme))
                    textSize = 13f
                }
                containerPerKategori?.addView(tv)
            } else {
                for ((category, items) in categoryMap) {
                    val catTotal = items.sumOf { it.amountRp }
                    val percent = if (totalCost > 0) ((catTotal / totalCost) * 100).toInt() else 0

                    val catLayout = LinearLayout(this).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply { setMargins(0, 8, 0, 8) }
                    }

                    val tvCatInfo = TextView(this).apply {
                        text = "$category (${formatRupiah(catTotal)})"
                        setTextColor(resources.getColor(R.color.text_secondary, theme))
                        textSize = 13f
                    }

                    val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            8
                        ).apply { setMargins(0, 4, 0, 0) }
                        progress = percent
                        progressTintList = ColorStateList.valueOf(Color.parseColor("#E65100"))
                    }

                    catLayout.addView(tvCatInfo)
                    catLayout.addView(progressBar)
                    containerPerKategori?.addView(catLayout)
                }
            }

            containerRiwayatBiaya?.removeAllViews()
            if (records.isNullOrEmpty()) {
                val tv = TextView(this).apply {
                    text = "Belum ada riwayat biaya"
                    setTextColor(resources.getColor(R.color.text_secondary, theme))
                    textSize = 14f
                }
                containerRiwayatBiaya?.addView(tv)
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
                        text = "${record.category} · ${record.title}"
                        setTextColor(resources.getColor(R.color.text_primary, theme))
                        textSize = 14f
                        typeface = Typeface.DEFAULT_BOLD
                        layoutParams = LinearLayout.LayoutParams(
                            0,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1f
                        )
                    }

                    val tvAmount = TextView(this).apply {
                        text = formatRupiah(record.amountRp)
                        setTextColor(resources.getColor(R.color.text_primary, theme))
                        textSize = 14f
                        typeface = Typeface.DEFAULT_BOLD
                    }

                    rowLayout.addView(tvInfo)
                    rowLayout.addView(tvAmount)

                    rowLayout.setOnLongClickListener {
                        AlertDialog.Builder(this)
                            .setTitle("Hapus Biaya")
                            .setMessage("Hapus ${record.title}?")
                            .setPositiveButton("Hapus") { _, _ ->
                                viewModel.deleteCostRecord(record)
                                Toast.makeText(this, "Biaya dihapus", Toast.LENGTH_SHORT).show()
                            }
                            .setNegativeButton("Batal", null)
                            .show()
                        true
                    }

                    containerRiwayatBiaya?.addView(rowLayout)

                    if (index < records.size - 1) {
                        val divider = View(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                1
                            ).apply {
                                setMargins(0, 12, 0, 12)
                            }
                            setBackgroundColor(Color.parseColor("#E0E0E0"))
                        }
                        containerRiwayatBiaya?.addView(divider)
                    }
                }
            }
        }

        updateBiayaUI(viewModel.costRecords.value)

        viewModel.costRecords.observe(this) { records ->
            if (binding.tvHeaderTitle.text.toString() == "Biaya Produksi") {
                updateBiayaUI(records)
            }
        }

        btnTambahBiaya?.setOnClickListener {
            showAddCostDialog()
        }

        val btnResetRiwayatBiaya = view.findViewById<View>(R.id.btnResetRiwayatBiaya)
        btnResetRiwayatBiaya?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Reset Riwayat Biaya")
                .setMessage("Apakah Anda yakin ingin menghapus semua riwayat biaya produksi?")
                .setPositiveButton("Hapus") { _, _ ->
                    viewModel.clearCostRecords()
                    Toast.makeText(this, "Riwayat biaya berhasil direset", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }

    private fun showAddCostDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val categories = arrayOf(
            "Pupuk & Nutrisi",
            "Tenaga Kerja / Upah Harian",
            "Pestisida & Obat Tanaman",
            "Bibit & Peremajaan",
            "Alat & Pemeliharaan Mesin",
            "Lain-lain"
        )

        val spinnerCategory = Spinner(this).apply {
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, categories)
            setPadding(0, 16, 0, 16)
        }

        val etTitle = EditText(this).apply {
            hint = "Keterangan (cth: Pupuk Urea Blok A)"
            setPadding(0, 16, 0, 16)
        }
        val etAmount = EditText(this).apply {
            hint = "Jumlah Biaya (Rp, cth: 2500000)"
            inputType = InputType.TYPE_CLASS_NUMBER
            setPadding(0, 16, 0, 16)
        }

        val tvCatLabel = TextView(this).apply {
            text = "Pilih Kategori Biaya:"
            setTextColor(resources.getColor(R.color.text_primary, theme))
            textSize = 13f
        }

        layout.addView(tvCatLabel)
        layout.addView(spinnerCategory)
        layout.addView(etTitle)
        layout.addView(etAmount)

        AlertDialog.Builder(this)
            .setTitle("Tambah Biaya Produksi")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val category = spinnerCategory.selectedItem.toString()
                val title = etTitle.text.toString().trim()
                val amountStr = etAmount.text.toString().trim()

                if (title.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(this, "Keterangan dan jumlah biaya harus diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id"))
                val record = CostRecord(
                    userId = userId,
                    category = category,
                    title = title,
                    amountRp = amount,
                    date = dateFormat.format(Date())
                )

                viewModel.insertCostRecord(record)
                Toast.makeText(this, "Biaya berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
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

        val etPeriode = view.findViewById<TextInputEditText>(R.id.etPeriodeLaporan)
        etPeriode?.setText("Periode: Tahun $selectedReportYear")
        etPeriode?.setOnClickListener {
            val years = arrayOf("2025", "2026", "2027", "2028", "2029", "2030")
            AlertDialog.Builder(this)
                .setTitle("Pilih Tahun Laporan")
                .setItems(years) { _, which ->
                    selectedReportYear = years[which].toInt()
                    etPeriode.setText("Periode: Tahun $selectedReportYear")
                    if (binding.tvHeaderTitle.text.toString() == "Laporan") {
                        loadLaporanSubTab()
                    }
                }
                .show()
        }

        val updateLaporan = {
            val tvTotalPanen = view.findViewById<TextView>(R.id.tvLaporanTotalPanen)
            val tvTotalBiaya = view.findViewById<TextView>(R.id.tvLaporanTotalBiaya)
            val tvBersih = view.findViewById<TextView>(R.id.tvLaporanBersih)
            val tvBiayaHasil = view.findViewById<TextView>(R.id.tvLaporanBiayaHasil)

            val yearStr = selectedReportYear.toString()
            val yearHarvests = viewModel.harvestRecords.value?.filter { it.date.contains(yearStr) } ?: emptyList()
            val yearCosts = viewModel.costRecords.value?.filter { it.date.contains(yearStr) } ?: emptyList()

            val totalPanenKg = yearHarvests.sumOf { it.quantityKg }
            val totalBiayaRp = yearCosts.sumOf { it.amountRp }

            val hargaJualPerKg = 35000.0
            val totalPendapatan = totalPanenKg * hargaJualPerKg
            val bersihRp = totalPendapatan - totalBiayaRp
            val biayaHasilRatio = if (totalPendapatan > 0) ((totalBiayaRp / totalPendapatan) * 100).toInt() else 0

            tvTotalPanen?.text = "${NumberFormat.getInstance(Locale.forLanguageTag("id")).format(totalPanenKg)} kg"
            tvTotalBiaya?.text = formatRupiahJt(totalBiayaRp)
            tvBersih?.text = formatRupiahJt(bersihRp)
            tvBiayaHasil?.text = "$biayaHasilRatio%"

            val calendar = Calendar.getInstance()
            val monthNames = listOf("Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember")
            val shortMonthNames = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Ags", "Sep", "Okt", "Nov", "Des")

            val panenBars = listOf(
                view.findViewById<View>(R.id.barPanen1), view.findViewById<View>(R.id.barPanen2),
                view.findViewById<View>(R.id.barPanen3), view.findViewById<View>(R.id.barPanen4),
                view.findViewById<View>(R.id.barPanen5), view.findViewById<View>(R.id.barPanen6),
                view.findViewById<View>(R.id.barPanen7), view.findViewById<View>(R.id.barPanen8),
                view.findViewById<View>(R.id.barPanen9), view.findViewById<View>(R.id.barPanen10),
                view.findViewById<View>(R.id.barPanen11), view.findViewById<View>(R.id.barPanen12)
            )
            val biayaBars = listOf(
                view.findViewById<View>(R.id.barBiaya1), view.findViewById<View>(R.id.barBiaya2),
                view.findViewById<View>(R.id.barBiaya3), view.findViewById<View>(R.id.barBiaya4),
                view.findViewById<View>(R.id.barBiaya5), view.findViewById<View>(R.id.barBiaya6),
                view.findViewById<View>(R.id.barBiaya7), view.findViewById<View>(R.id.barBiaya8),
                view.findViewById<View>(R.id.barBiaya9), view.findViewById<View>(R.id.barBiaya10),
                view.findViewById<View>(R.id.barBiaya11), view.findViewById<View>(R.id.barBiaya12)
            )
            val monthLabels = listOf(
                view.findViewById<TextView>(R.id.tvLaporanMonth1), view.findViewById<TextView>(R.id.tvLaporanMonth2),
                view.findViewById<TextView>(R.id.tvLaporanMonth3), view.findViewById<TextView>(R.id.tvLaporanMonth4),
                view.findViewById<TextView>(R.id.tvLaporanMonth5), view.findViewById<TextView>(R.id.tvLaporanMonth6),
                view.findViewById<TextView>(R.id.tvLaporanMonth7), view.findViewById<TextView>(R.id.tvLaporanMonth8),
                view.findViewById<TextView>(R.id.tvLaporanMonth9), view.findViewById<TextView>(R.id.tvLaporanMonth10),
                view.findViewById<TextView>(R.id.tvLaporanMonth11), view.findViewById<TextView>(R.id.tvLaporanMonth12)
            )

            val monthlyPendapatan = DoubleArray(12)
            val monthlyBiaya = DoubleArray(12)

            for (i in 0 until 12) {
                val mName = monthNames[i]
                monthLabels[i]?.text = shortMonthNames[i]

                val panenForMonth = yearHarvests.filter { it.date.contains(mName, ignoreCase = true) }.sumOf { it.quantityKg }
                val biayaForMonth = yearCosts.filter { it.date.contains(mName, ignoreCase = true) }.sumOf { it.amountRp }

                monthlyPendapatan[i] = panenForMonth * 35000.0
                monthlyBiaya[i] = biayaForMonth
            }

            val allValues = monthlyPendapatan + monthlyBiaya
            val maxVal = maxOf(1.0, allValues.maxOrNull() ?: 1.0).toFloat()
            val density = resources.displayMetrics.density

            for (i in 0 until 12) {
                val pVal = monthlyPendapatan[i]
                val bVal = monthlyBiaya[i]

                val pHeightDp = if (pVal > 0) ((pVal.toFloat() / maxVal) * 85f).coerceIn(8f, 95f) else 8f
                val bHeightDp = if (bVal > 0) ((bVal.toFloat() / maxVal) * 85f).coerceIn(8f, 95f) else 8f

                panenBars[i]?.layoutParams?.height = (pHeightDp * density).toInt()
                panenBars[i]?.requestLayout()

                biayaBars[i]?.layoutParams?.height = (bHeightDp * density).toInt()
                biayaBars[i]?.requestLayout()
            }
        }

        updateLaporan()

        viewModel.harvestRecords.observe(this) {
            if (binding.tvHeaderTitle.text.toString() == "Laporan") {
                updateLaporan()
            }
        }

        viewModel.costRecords.observe(this) {
            if (binding.tvHeaderTitle.text.toString() == "Laporan") {
                updateLaporan()
            }
        }

        view.findViewById<View>(R.id.btnExcel)?.setOnClickListener {
            exportReport("EXCEL")
        }

        view.findViewById<View>(R.id.btnPdf)?.setOnClickListener {
            exportReport("PDF")
        }
    }

    private fun exportReport(format: String) {
        try {
            val totalPanenKg = viewModel.harvestRecords.value?.sumOf { it.quantityKg } ?: 0
            val totalBiayaRp = viewModel.costRecords.value?.sumOf { it.amountRp } ?: 0.0
            val hargaJualPerKg = customMarketPricePerKg * 4.0
            val totalPendapatan = totalPanenKg * hargaJualPerKg
            val bersihRp = totalPendapatan - totalBiayaRp

            val reportContent = buildString {
                append("=== LAPORAN KEBUNKU (SMART COFFEE FARM) ===\n")
                append("Tanggal Ekspor: ${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())}\n\n")
                append("RINGKASAN KEUANGAN & PRODUKSI:\n")
                append("- Total Panen: $totalPanenKg kg\n")
                append("- Total Biaya: ${formatRupiah(totalBiayaRp)}\n")
                append("- Estimasi Pendapatan: ${formatRupiah(totalPendapatan)}\n")
                append("- Laba Bersih (Profit): ${formatRupiah(bersihRp)}\n\n")
                append("RIWAYAT PANEN:\n")
                viewModel.harvestRecords.value?.forEach {
                    append("- ${it.date} | Blok: ${it.blockName} | Jumlah: ${it.quantityKg} kg | Kualitas: ${it.quality}\n")
                } ?: append("Tidak ada riwayat panen.\n")
                append("\nRIWAYAT BIAYA PRODUKSI:\n")
                viewModel.costRecords.value?.forEach {
                    append("- ${it.date} | Kategori: ${it.category} | ${it.title} | Biaya: ${formatRupiah(it.amountRp)}\n")
                } ?: append("Tidak ada riwayat biaya.\n")
            }

            val ext = if (format == "EXCEL") "csv" else "txt"
            val fileName = "Laporan_KebunKu_${System.currentTimeMillis()}.$ext"

            val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, if (format == "EXCEL") "text/csv" else "text/plain")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(reportContent.toByteArray())
                    }
                }
                File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val targetFile = File(downloadsDir, fileName)
                FileOutputStream(targetFile).use { fos ->
                    fos.write(reportContent.toByteArray())
                }
                targetFile
            }

            Toast.makeText(this, "Berhasil didownload ke folder Download:\n$fileName", Toast.LENGTH_LONG).show()

            // Open file intent
            val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, if (format == "EXCEL") "text/csv" else "text/plain")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                startActivity(intent)
            } catch (_: Exception) {}

        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mendownload laporan: ${e.message}", Toast.LENGTH_SHORT).show()
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
