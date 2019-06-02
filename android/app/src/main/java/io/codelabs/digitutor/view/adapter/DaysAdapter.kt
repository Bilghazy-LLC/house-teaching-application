package io.codelabs.digitutor.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import io.codelabs.digitutor.R
import io.codelabs.digitutor.core.util.OnClickListener
import io.codelabs.digitutor.data.model.DateTime
import io.codelabs.digitutor.databinding.ItemDaysBinding

class DaysAdapter(private val ctx: Context, private val listener: OnClickListener<DateTime>) :
    RecyclerView.Adapter<DaysAdapter.DaysViewHolder>() {
    private val dataSource = mutableListOf<DateTime>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaysViewHolder {
        return DaysViewHolder(DataBindingUtil.inflate(LayoutInflater.from(ctx), R.layout.item_days, parent, false))
    }

    override fun getItemCount(): Int = dataSource.size

    override fun onBindViewHolder(holder: DaysViewHolder, position: Int) {
        val dateTime = dataSource[position]
        holder.bind(dateTime)
        holder.itemView.setOnClickListener { listener.onClick(dateTime, false) }
        holder.itemView.setOnLongClickListener {
            listener.onClick(dateTime, true)
            true
        }
    }


    class DaysViewHolder(private val binding: ItemDaysBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(dateTime: DateTime) {
            binding.dateTime = dateTime
        }
    }
}