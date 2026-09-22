package com.example.campusrelay.ui.carpool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.campusrelay.CampusRelayApp
import com.example.campusrelay.R
import com.example.campusrelay.data.model.RecurrenceRule
import com.example.campusrelay.data.model.RideOfferStatus
import com.example.campusrelay.data.model.VehicleType
import com.example.campusrelay.databinding.FragmentCreateRideOfferBinding
import com.example.campusrelay.ui.common.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Fragment for creating a new ride offer (REQ-CAR-1). */
class CreateRideOfferFragment : Fragment() {

    private var binding: FragmentCreateRideOfferBinding? = null

    private val viewModel: CreateRideOfferViewModel by viewModels {
        val app = requireActivity().application as CampusRelayApp
        ViewModelFactory {
            CreateRideOfferViewModel(app.serviceLocator.carpoolRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateRideOfferBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return

        // Setup vehicle type dropdown
        val vehicleTypes = VehicleType.values().map { 
            getVehicleTypeDisplayName(it) 
        }
        val vehicleTypeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, vehicleTypes)
        b.dropdownVehicleType.setAdapter(vehicleTypeAdapter)

        // Setup recurrence dropdown
        val recurrenceTypes = listOf("Weekly", "Monthly")
        val recurrenceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, recurrenceTypes)
        b.dropdownRepeat.setAdapter(recurrenceAdapter)

        // Toggle recurring options visibility
        b.switchRecurring.setOnCheckedChangeListener { _, isChecked ->
            b.layoutRecurringOptions.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        b.buttonCreateRide.setOnClickListener {
            val origin = b.editOrigin.text?.toString().orEmpty()
            val destination = b.editDestination.text?.toString().orEmpty()
            val dateText = b.editDepartureDate.text?.toString().orEmpty()
            val timeText = b.editDepartureTime.text?.toString().orEmpty()
            val seatsText = b.editSeats.text?.toString().orEmpty()
            val vehicleTypeIndex = b.dropdownVehicleType.text?.toString()
            val priceText = b.editPricePerSeat.text?.toString().orEmpty()
            val isRecurring = b.switchRecurring.isChecked

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            val date = try { dateFormat.parse(dateText) } catch (e: Exception) { null }
            val time = try { timeFormat.parse(timeText) } catch (e: Exception) { null }

            val seats = try { seatsText.toInt() } catch (e: NumberFormatException) { 0 }
            val price = try { priceText.toDouble() } catch (e: NumberFormatException) { 0.0 }
            val vehicleType = vehicleTypes.indexOf(vehicleTypeIndex).takeIf { it != -1 }?.let { VehicleType.values()[it] }

            if (origin.isBlank() || destination.isBlank() || date == null || time == null || 
                seats <= 0 || vehicleType == null || price <= 0) {
                Toast.makeText(requireContext(), R.string.create_ride_validation_error, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Combine date and time
            val departureTime = Date(date.time + time.time - dateFormat.parse("00/00/0000").time)

            var recurrenceRule: String? = null
            if (isRecurring) {
                val repeatIndex = recurrenceTypes.indexOf(b.dropdownRepeat.text?.toString())
                val untilText = b.editUntil.text?.toString().orEmpty()
                val untilDate = try { dateFormat.parse(untilText) } catch (e: Exception) { null }

                recurrenceRule = if (repeatIndex != -1 && untilDate != null) {
                    RecurrenceRule.WEEKLY.toString() // Simplified for now
                } else {
                    null
                }
            }

            viewModel.createRideOffer(
                originCity = origin,
                destinationCity = destination,
                departureTime = departureTime,
                availableSeats = seats,
                vehicleType = vehicleType,
                pricePerSeat = price,
                isRecurring = isRecurring,
                recurrenceRule = recurrenceRule
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitState.collect { state -> render(state, b) }
            }
        }
    }

    private fun render(state: SubmitState, b: FragmentCreateRideOfferBinding) {
        when (state) {
            SubmitState.Idle -> Unit
            SubmitState.Loading -> b.buttonCreateRide.isEnabled = false
            is SubmitState.Success -> {
                b.buttonCreateRide.isEnabled = true
                Toast.makeText(requireContext(), R.string.create_ride_success, Toast.LENGTH_LONG).show()
                clearForm(b)
                viewModel.resetState()
                findNavController().navigate(R.id.carpoolFragment)
            }
            is SubmitState.Error -> {
                b.buttonCreateRide.isEnabled = true
                Toast.makeText(requireContext(), R.string.create_ride_error, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
        }
    }

    private fun getVehicleTypeDisplayName(vehicleType: VehicleType): String {
        return when (vehicleType) {
            VehicleType.CAR -> getString(R.string.vehicle_car)
            VehicleType.SUV -> getString(R.string.vehicle_suv)
            VehicleType.VAN -> getString(R.string.vehicle_van)
        }
    }

    private fun clearForm(b: FragmentCreateRideOfferBinding) {
        b.editOrigin.setText("")
        b.editDestination.setText("")
        b.editDepartureDate.setText("")
        b.editDepartureTime.setText("")
        b.editSeats.setText("")
        b.dropdownVehicleType.setText("", false)
        b.editPricePerSeat.setText("")
        b.switchRecurring.isChecked = false
        b.layoutRecurringOptions.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}

sealed class SubmitState {
    object Idle : SubmitState()
    object Loading : SubmitState()
    object Success : SubmitState()
    data class Error(val message: String) : SubmitState()
}
