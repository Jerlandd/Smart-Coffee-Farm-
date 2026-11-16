package com.example.smartcoffeefarm.ui.main

import android.R
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.smartcoffeefarm.data.CoffeeDatabase
import com.example.smartcoffeefarm.data.CoffeePlot
import com.example.smartcoffeefarm.databinding.ActivityAddEditCoffeeBinding
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AddEditCoffeeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddEditCoffeeBinding
    private val viewModel: CoffeeViewModel by viewModels()

    private var userId: Long = 1L
    private var isEdit: Boolean = false
    private var plotId: Long = -1L
    private var currentImageUri: String? = null

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            if (imageBitmap != null) {
                val uri = saveBitmapToFile(imageBitmap)
                if (uri != null) {
                    currentImageUri = uri.toString()
                    binding.ivPlantPhoto.setImageURI(uri)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditCoffeeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getLongExtra("USER_ID", 1L)
        isEdit = intent.getBooleanExtra("IS_EDIT", false)
        plotId = intent.getLongExtra("PLOT_ID", -1L)

        viewModel.setUserId(userId)

        setupDropdowns()
        setupListeners()

        if (isEdit) {
            binding.tvHeaderTitle.text = "Ubah Blok Kopi"
            loadPlotData()
        } else {
            binding.tvHeaderTitle.text = "Tambah Blok Kopi"
        }
    }

    private fun setupDropdowns() {
        val varieties = arrayOf("Arabika", "Robusta", "Liberika")
        val varietyAdapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, varieties)
        binding.actvVariety.setAdapter(varietyAdapter)

        val statuses = arrayOf("Sehat", "Perawatan Khusus", "Sakit")
        val statusAdapter = ArrayAdapter(this, R.layout.simple_dropdown_item_1line, statuses)
        binding.actvStatus.setAdapter(statusAdapter)
    }

    private fun setupListeners() {
        binding.btnCapturePhoto.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                cameraLauncher.launch(takePictureIntent)
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(this, "Kamera tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSave.setOnClickListener {
            savePlot()
        }
    }

    private fun loadPlotData() {
        lifecycleScope.launch {
            val plot = CoffeeDatabase.getDatabase(this@AddEditCoffeeActivity).coffeeDao().getPlotById(plotId)
            if (plot != null) {
                binding.etName.setText(plot.name)
                binding.actvVariety.setText(plot.variety, false)
                binding.etDate.setText(plot.plantingDate)
                binding.etMoisture.setText(plot.soilMoisture)
                binding.actvStatus.setText(plot.healthStatus, false)
                binding.etNotes.setText(plot.notes)
                currentImageUri = plot.imageUri
                if (!currentImageUri.isNullOrEmpty()) {
                    try {
                        binding.ivPlantPhoto.setImageURI(Uri.parse(currentImageUri))
                    } catch (_: Exception) {}
                }
            }
        }
    }

    private fun savePlot() {
        val name = binding.etName.text.toString().trim()
        val variety = binding.actvVariety.text.toString().trim()
        val date = binding.etDate.text.toString().trim()
        val moisture = binding.etMoisture.text.toString().trim()
        val status = binding.actvStatus.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilName.error = "Nama blok tidak boleh kosong"
            return
        } else {
            binding.tilName.error = null
        }

        if (variety.isEmpty()) {
            binding.tilVariety.error = "Pilih varietas kopi"
            return
        } else {
            binding.tilVariety.error = null
        }

        if (date.isEmpty()) {
            binding.tilDate.error = "Tanggal tanam tidak boleh kosong"
            return
        } else {
            binding.tilDate.error = null
        }

        if (moisture.isEmpty()) {
            binding.tilMoisture.error = "Kelembaban tidak boleh kosong"
            return
        } else {
            binding.tilMoisture.error = null
        }

        if (status.isEmpty()) {
            binding.tilStatus.error = "Pilih status kesehatan"
            return
        } else {
            binding.tilStatus.error = null
        }

        if (isEdit && plotId != -1L) {
            val updatedPlot = CoffeePlot(
                id = plotId,
                userId = userId,
                name = name,
                variety = variety,
                plantingDate = date,
                soilMoisture = moisture,
                healthStatus = status,
                notes = notes,
                imageUri = currentImageUri
            )
            viewModel.updatePlot(updatedPlot)
            Toast.makeText(this, "Berhasil memperbarui data", Toast.LENGTH_SHORT).show()
        } else {
            val newPlot = CoffeePlot(
                userId = userId,
                name = name,
                variety = variety,
                plantingDate = date,
                soilMoisture = moisture,
                healthStatus = status,
                notes = notes,
                imageUri = currentImageUri
            )
            viewModel.insertPlot(newPlot)
            Toast.makeText(this, "Berhasil menambahkan data", Toast.LENGTH_SHORT).show()
        }
        finish()
    }

    private fun saveBitmapToFile(bitmap: Bitmap): Uri? {
        return try {
            val file = File(filesDir, "coffee_${UUID.randomUUID()}.jpg")
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
            Uri.fromFile(file)
        } catch (_: Exception) {
            null
        }
    }
}
