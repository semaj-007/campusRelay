package com.example.campusrelay.ui.marketplace

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
import com.example.campusrelay.data.model.ListingCategory
import com.example.campusrelay.data.model.ListingCondition
import com.example.campusrelay.databinding.FragmentCreateListingBinding
import com.example.campusrelay.ui.common.ViewModelFactory
import kotlinx.coroutines.launch

/** Fragment for creating a new marketplace listing (REQ-MKT-1). */
class CreateListingFragment : Fragment() {

    private var binding: FragmentCreateListingBinding? = null

    private val viewModel: CreateListingViewModel by viewModels {
        val app = requireActivity().application as CampusRelayApp
        ViewModelFactory {
            CreateListingViewModel(app.serviceLocator.marketplaceRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCreateListingBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return

        // Setup category dropdown
        val categories = ListingCategory.values().map { 
            getCategoryDisplayName(it) 
        }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, categories)
        b.dropdownCategory.setAdapter(categoryAdapter)

        // Setup condition dropdown
        val conditions = ListingCondition.values().map { 
            getConditionDisplayName(it) 
        }
        val conditionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, conditions)
        b.dropdownCondition.setAdapter(conditionAdapter)

        b.buttonPostListing.setOnClickListener {
            val title = b.editTitle.text?.toString().orEmpty()
            val description = b.editDescription.text?.toString().orEmpty()
            val priceText = b.editPrice.text?.toString().orEmpty()
            val categoryIndex = b.dropdownCategory.text?.toString()
            val conditionIndex = b.dropdownCondition.text?.toString()

            val price = try { priceText.toDouble() } catch (e: NumberFormatException) { 0.0 }
            val category = categories.indexOf(categoryIndex).takeIf { it != -1 }?.let { ListingCategory.values()[it] }
            val condition = conditions.indexOf(conditionIndex).takeIf { it != -1 }?.let { ListingCondition.values()[it] }

            if (title.isBlank() || description.isBlank() || price <= 0 || category == null || condition == null) {
                Toast.makeText(requireContext(), R.string.create_listing_validation_error, Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            viewModel.createListing(
                title = title,
                description = description,
                price = price,
                category = category,
                condition = condition,
                photoUrls = emptyList() // Photos will be handled separately
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.submitState.collect { state -> render(state, b) }
            }
        }
    }

    private fun render(state: SubmitState, b: FragmentCreateListingBinding) {
        when (state) {
            SubmitState.Idle -> Unit
            SubmitState.Loading -> b.buttonPostListing.isEnabled = false
            is SubmitState.Success -> {
                b.buttonPostListing.isEnabled = true
                Toast.makeText(requireContext(), R.string.create_listing_success, Toast.LENGTH_LONG).show()
                clearForm(b)
                viewModel.resetState()
                findNavController().navigate(R.id.marketplaceFragment)
            }
            is SubmitState.Error -> {
                b.buttonPostListing.isEnabled = true
                Toast.makeText(requireContext(), R.string.create_listing_error, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
        }
    }

    private fun getCategoryDisplayName(category: ListingCategory): String {
        return when (category) {
            ListingCategory.BOOKS -> getString(R.string.marketplace_filter_books)
            ListingCategory.ELECTRONICS -> getString(R.string.marketplace_filter_electronics)
            ListingCategory.CLOTHING -> getString(R.string.marketplace_filter_clothing)
            ListingCategory.FURNITURE -> getString(R.string.marketplace_filter_furniture)
            ListingCategory.OTHER -> getString(R.string.marketplace_filter_other)
        }
    }

    private fun getConditionDisplayName(condition: ListingCondition): String {
        return when (condition) {
            ListingCondition.NEW -> getString(R.string.condition_new)
            ListingCondition.LIKE_NEW -> getString(R.string.condition_like_new)
            ListingCondition.GOOD -> getString(R.string.condition_good)
            ListingCondition.FAIR -> getString(R.string.condition_fair)
        }
    }

    private fun clearForm(b: FragmentCreateListingBinding) {
        b.editTitle.setText("")
        b.editDescription.setText("")
        b.editPrice.setText("")
        b.dropdownCategory.setText("", false)
        b.dropdownCondition.setText("", false)
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
