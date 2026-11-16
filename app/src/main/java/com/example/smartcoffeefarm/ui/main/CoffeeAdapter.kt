package com.example.smartcoffeefarm.ui.main

import android.R
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.smartcoffeefarm.data.CoffeePlot
import com.example.smartcoffeefarm.databinding.ItemCoffeePlotBinding

class CoffeeAdapter(private val onItemClick: (CoffeePlot) -> Unit) :
    ListAdapter<CoffeePlot, CoffeeAdapter.CoffeeViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CoffeeViewHolder {
        val binding = ItemCoffeePlotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CoffeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CoffeeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CoffeeViewHolder(private val binding: ItemCoffeePlotBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(plot: CoffeePlot) {
            binding.tvPlotName.text = plot.name
            binding.tvVariety.text = "Varietas: ${plot.variety}"
            binding.tvMoisture.text = "Kelembaban: ${plot.soilMoisture}"
            binding.chipStatus.text = plot.healthStatus

            if (!plot.imageUri.isNullOrEmpty()) {
                try {
                    binding.ivPlot.setImageURI(Uri.parse(plot.imageUri))
                } catch (_: Exception) {
                    binding.ivPlot.setImageResource(R.drawable.ic_menu_gallery)
                }
            } else {
                binding.ivPlot.setImageResource(R.drawable.ic_menu_gallery)
            }

            binding.root.setOnClickListener {
                onItemClick(plot)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<CoffeePlot>() {
        override fun areItemsTheSame(oldItem: CoffeePlot, newItem: CoffeePlot): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CoffeePlot, newItem: CoffeePlot): Boolean {
            return oldItem == newItem
        }
    }
}
