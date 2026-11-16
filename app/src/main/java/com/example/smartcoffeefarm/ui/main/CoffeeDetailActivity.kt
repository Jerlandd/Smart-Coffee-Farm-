package com.example.smartcoffeefarm.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartcoffeefarm.R
import com.example.smartcoffeefarm.data.CoffeeDatabase
import com.example.smartcoffeefarm.data.CoffeePlot
import com.example.smartcoffeefarm.databinding.ActivityCoffeeDetailBinding
import kotlinx.coroutines.launch

class CoffeeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCoffeeDetailBinding
    private val viewModel: CoffeeViewModel by viewModels()

    private var plotId: Long = -1L
    private var userId: Long = 1L
    private var currentPlot: CoffeePlot? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCoffeeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        plotId = intent.getLongExtra("PLOT_ID", -1L)
        userId = intent.getLongExtra("USER_ID", 1L)

        viewModel.setUserId(userId)

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topAppBar.setNavigationOnClickListener { finish() }

        loadPlotDetails()
    }

    override fun onResume() {
        super.onResume()
        loadPlotDetails()
    }

    private fun loadPlotDetails() {
        if (plotId == -1L) return
        lifecycleScope.launch {
            val plot = CoffeeDatabase.getDatabase(this@CoffeeDetailActivity).coffeeDao().getPlotById(plotId)
            currentPlot = plot
            if (plot != null) {
                binding.tvDetailName.text = plot.name
                binding.tvDetailVariety.text = plot.variety
                binding.tvDetailArea.text = "${plot.areaHa} ha (${(plot.areaHa * 10000).toInt()} m²)"
                binding.tvDetailAddress.text = "${plot.address.ifEmpty { "Jl. Perkebunan Kopi No. 1" }}\nKoordinat: ${plot.latitude}, ${plot.longitude}"
                binding.btnOpenMaps.setOnClickListener {
                    val gmmIntentUri = Uri.parse("geo:${plot.latitude},${plot.longitude}?q=${plot.latitude},${plot.longitude}(${plot.name})")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try {
                        startActivity(mapIntent)
                    } catch (_: Exception) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=${plot.latitude},${plot.longitude}"))
                        startActivity(browserIntent)
                    }
                }
                binding.tvDetailDate.text = plot.plantingDate
                binding.tvDetailMoisture.text = plot.soilMoisture
                binding.chipDetailStatus.text = plot.healthStatus
                binding.tvDetailNotes.text = plot.notes.ifEmpty { "Tidak ada catatan." }

                if (!plot.imageUri.isNullOrEmpty()) {
                    try {
                        binding.ivDetailPhoto.setImageURI(Uri.parse(plot.imageUri))
                    } catch (_: Exception) {
                        binding.ivDetailPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
                    }
                } else {
                    binding.ivDetailPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                currentPlot?.let { plot ->
                    val intent = Intent(this, AddEditCoffeeActivity::class.java).apply {
                        putExtra("USER_ID", userId)
                        putExtra("PLOT_ID", plot.id)
                        putExtra("IS_EDIT", true)
                    }
                    startActivity(intent)
                }
                true
            }
            R.id.action_delete -> {
                currentPlot?.let { plot ->
                    AlertDialog.Builder(this)
                        .setTitle("Hapus Blok Kopi")
                        .setMessage("Apakah Anda yakin ingin menghapus ${plot.name}?")
                        .setPositiveButton("Hapus") { _, _ ->
                            viewModel.deletePlot(plot)
                            Toast.makeText(this, "Berhasil menghapus data", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
