package com.example.campusrelay.ui.carpool

import com.example.campusrelay.util.categoryLabel
import com.example.campusrelay.util.conditionLabel
import com.example.campusrelay.util.vehicleLabel
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.campusrelay.data.model.RideOffer
import com.example.campusrelay.data.model.VehicleType
import com.example.campusrelay.databinding.ItemRideOfferBinding
import java.text.SimpleDateFormat
import java.util.Locale

/** Adapter for displaying ride offers in a RecyclerView. */
class RideOfferAdapter(
    private val onItemClick: (RideOffer) -> Unit
) : ListAdapter<RideOffer, RideOfferAdapter.ViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRideOfferBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemRideOfferBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(rideOffer: RideOffer) {
            binding.textRoute.text = "${rideOffer.originCity} → ${rideOffer.destinationCity}"

            val dateFormat = SimpleDateFormat("EEEE, HH:mm", Locale.getDefault())
            binding.textDepartureTime.text = dateFormat.format(rideOffer.departureTime)

            binding.textVehicleType.text = binding.root.context.vehicleLabel(rideOffer.vehicleType)
            binding.textSeatsAvailable.text = binding.root.context.getString(com.example.campusrelay.R.string.seats_available, rideOffer.availableSeats)
            binding.textPricePerSeat.text = binding.root.context.getString(com.example.campusrelay.R.string.ride_details_price_per_seat, rideOffer.pricePerSeat)

            // Set vehicle icon based on type
            val vehicleIcon = when (rideOffer.vehicleType) {
                VehicleType.CAR -> com.example.campusrelay.R.drawable.ic_carpool
                VehicleType.SUV -> com.example.campusrelay.R.drawable.ic_carpool
                VehicleType.VAN -> com.example.campusrelay.R.drawable.ic_carpool
            }
            binding.imageVehicleIcon.setImageResource(vehicleIcon)

            // Driver rating (placeholder - would be loaded from user data)
            binding.textDriverRating.text = "⭐ 4.8"

            binding.buttonViewRide.setOnClickListener { onItemClick(rideOffer) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<RideOffer>() {
            override fun areItemsTheSame(oldItem: RideOffer, newItem: RideOffer) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: RideOffer, newItem: RideOffer) =
                oldItem == newItem
        }
    }
}
