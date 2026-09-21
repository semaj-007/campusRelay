package com.example.campusrelay.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrelay.data.model.DeliveryFeedItem
import com.example.campusrelay.databinding.ItemNearbyTaskBinding

class NearbyTaskAdapter(
    private val onItemClick: (DeliveryFeedItem) -> Unit
) : ListAdapter<DeliveryFeedItem, NearbyTaskAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNearbyTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemNearbyTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DeliveryFeedItem) {
            binding.textItemTitle.text = item.itemDescription
            binding.textItemRoute.text = "${item.pickupBuildingName} \u2192 ${item.dropoffBuildingName}"
            binding.textItemDistance.text = binding.root.context.getString(
                com.example.campusrelay.R.string.task_distance_label, item.distanceKm
            )
            binding.textItemReward.text = binding.root.context.getString(
                com.example.campusrelay.R.string.task_reward_label, item.rewardAmount
            )
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DeliveryFeedItem>() {
            override fun areItemsTheSame(oldItem: DeliveryFeedItem, newItem: DeliveryFeedItem) =
                oldItem.deliveryId == newItem.deliveryId

            override fun areContentsTheSame(oldItem: DeliveryFeedItem, newItem: DeliveryFeedItem) =
                oldItem == newItem
        }
    }
}
