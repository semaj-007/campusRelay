package com.example.campusrelay.ui.carpool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.campusrelay.CampusRelayApp
import com.example.campusrelay.R
import com.example.campusrelay.databinding.FragmentRideDetailsBinding
import com.example.campusrelay.ui.common.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/** Fragment for displaying ride offer details (REQ-CAR-2). */
class RideDetailsFragment : Fragment() {

    private var binding: FragmentRideDetailsBinding? = null
    private val args: RideDetailsFragmentArgs by navArgs()

    private val viewModel: RideDetailsViewModel by viewModels {
        val app = requireActivity().application as CampusRelayApp
        ViewModelFactory {
            RideDetailsViewModel(app.serviceLocator.carpoolRepository, args.rideId)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRideDetailsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return

        b.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        b.buttonRequestSeat.setOnClickListener {
            // TODO: Implement request seat functionality
            // This would create a booking request for the ride
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state, b) }
            }
        }
    }

    private fun render(state: RideDetailsUiState, b: FragmentRideDetailsBinding) {
        when (state) {
            is RideDetailsUiState.Loading -> {
                // Show loading state
            }
            is RideDetailsUiState.Success -> {
                val rideOffer = state.rideOffer
                b.textOrigin.text = rideOffer.originCity
                b.textDestination.text = rideOffer.destinationCity

                val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                b.textDate.text = dateFormat.format(rideOffer.departureTime)
                b.textTime.text = timeFormat.format(rideOffer.departureTime)

                b.textVehicleName.text = rideOffer.vehicleType.name
                b.textVehicleType.text = rideOffer.vehicleType.name
                b.textSeatsAvailable.text = rideOffer.availableSeats.toString()
                b.textPricePerSeat.text = "R${"%.2f".format(rideOffer.pricePerSeat)} / seat"

                // TODO: Load driver info (name, rating, profile picture)
                b.textDriverName.text = "Thabo"
                b.textDriverRating.text = "⭐ 4.8"
            }
            is RideDetailsUiState.Error -> {
                // Show error state
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
