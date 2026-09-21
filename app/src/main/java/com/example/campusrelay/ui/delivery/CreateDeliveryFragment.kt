package com.example.campusrelay.ui.delivery

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
import com.example.campusrelay.common.Constants
import com.example.campusrelay.databinding.FragmentCreateDeliveryBinding
import com.example.campusrelay.ui.common.ViewModelFactory
import com.example.campusrelay.util.CampusBuildings
import kotlinx.coroutines.launch

/** Delivery Request Creation Wizard (REQ-DEL-1 and the wizard UI spec from Part 1). */
class CreateDeliveryFragment : Fragment() {

    private var binding: FragmentCreateDeliveryBinding? = null

    private val viewModel: CreateDeliveryViewModel by viewModels {
        val app = requireActivity().application as CampusRelayApp
        ViewModelFactory { CreateDeliveryViewModel(app.serviceLocator.deliveryRepository) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateDeliveryBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return

        val buildingAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, CampusBuildings.NAMES)
        b.dropdownPickup.setAdapter(buildingAdapter)
        b.dropdownDropoff.setAdapter(buildingAdapter)

        updateTipLabel(b, b.sliderTip.value)
        b.sliderTip.addOnChangeListener { _, value, _ -> updateTipLabel(b, value) }

        b.buttonSubmit.setOnClickListener {
            val weightCategory = when (b.radioGroupWeight.checkedRadioButtonId) {
                R.id.radio_weight_medium -> Constants.WEIGHT_CATEGORY_MEDIUM
                R.id.radio_weight_large -> Constants.WEIGHT_CATEGORY_LARGE
                else -> Constants.WEIGHT_CATEGORY_SMALL
            }
            viewModel.submit(
                pickupBuilding = b.dropdownPickup.text?.toString(),
                dropoffBuilding = b.dropdownDropoff.text?.toString(),
                itemDescription = b.editItemDescription.text?.toString().orEmpty(),
                weightCategory = weightCategory,
                tipAmount = b.sliderTip.value.toDouble()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitState.collect { state -> render(state, b) }
            }
        }
    }

    private fun render(state: SubmitState, b: FragmentCreateDeliveryBinding) {
        when (state) {
            SubmitState.Idle -> Unit
            SubmitState.Loading -> b.buttonSubmit.isEnabled = false
            is SubmitState.Submitted -> {
                b.buttonSubmit.isEnabled = true
                val message = if (state.queuedOffline) {
                    getString(R.string.create_delivery_queued_offline)
                } else {
                    getString(R.string.create_delivery_success)
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                clearForm(b)
                viewModel.resetState()
                findNavController().navigate(R.id.homeFragment)
            }
            is SubmitState.Error -> {
                b.buttonSubmit.isEnabled = true
                val message = if (state.message == "validation") {
                    getString(R.string.create_delivery_validation_error)
                } else {
                    getString(R.string.create_delivery_error_generic)
                }
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
        }
    }

    private fun updateTipLabel(b: FragmentCreateDeliveryBinding, tip: Float) {
        b.textTipLabel.text = getString(R.string.create_delivery_tip_label, tip.toDouble())
    }

    private fun clearForm(b: FragmentCreateDeliveryBinding) {
        b.dropdownPickup.setText("", false)
        b.dropdownDropoff.setText("", false)
        b.editItemDescription.setText("")
        b.radioGroupWeight.check(R.id.radio_weight_small)
        b.sliderTip.value = 20f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
