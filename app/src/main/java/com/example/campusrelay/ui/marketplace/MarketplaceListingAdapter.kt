package com.example.campusrelay.ui.marketplace

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.campusrelay.data.model.MarketplaceListing
import com.example.campusrelay.databinding.ItemMarketplaceListingBinding

/** Adapter for displaying marketplace listings in a RecyclerView. */
class MarketplaceListingAdapter(
    private val onItemClick: (MarketplaceListing) -> Unit
) : ListAdapter<MarketplaceListing, MarketplaceListingAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMarketplaceListingBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemMarketplaceListingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(listing: MarketplaceListing) {
            binding.textItemTitle.text = listing.title
            binding.textItemPrice.text = "R${"%.2f".format(listing.price)}"
            binding.textItemCondition.text = listing.condition.name.replace("_", " ")
            binding.textItemCategory.text = listing.category.name.replace("_", " ")

            // Load first photo if available
            if (listing.photoUrls.isNotEmpty()) {
                Glide.with(binding.imageItemPhoto.context)
                    .load(listing.photoUrls.first())
                    .centerCrop()
                    .placeholder(android.R.color.light_gray)
                    .into(binding.imageItemPhoto)
            }

            binding.root.setOnClickListener { onItemClick(listing) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MarketplaceListing>() {
            override fun areItemsTheSame(oldItem: MarketplaceListing, newItem: MarketplaceListing) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MarketplaceListing, newItem: MarketplaceListing) =
                oldItem == newItem
        }
    }
}
