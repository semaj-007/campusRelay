package com.example.campusrelay.ui.marketplace

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
import com.bumptech.glide.Glide
import com.example.campusrelay.CampusRelayApp
import com.example.campusrelay.R
import com.example.campusrelay.databinding.FragmentMarketplaceItemDetailsBinding
import com.example.campusrelay.ui.common.ViewModelFactory
import kotlinx.coroutines.launch

/** Fragment for displaying marketplace listing details (REQ-MKT-2). */
class MarketplaceItemDetailsFragment : Fragment() {

    private var binding: FragmentMarketplaceItemDetailsBinding? = null
    private val args: MarketplaceItemDetailsFragmentArgs by navArgs()

    private val viewModel: MarketplaceItemDetailsViewModel by viewModels {
        val app = requireActivity().application as CampusRelayApp
        ViewModelFactory {
            MarketplaceItemDetailsViewModel(app.serviceLocator.marketplaceRepository, args.listingId)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMarketplaceItemDetailsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return

        b.buttonBack.setOnClickListener {
            findNavController().navigateUp()
        }

        b.buttonContactSeller.setOnClickListener {
            // TODO: Implement contact seller functionality
            // This would open a chat or contact dialog
        }

        b.buttonBuyRequest.setOnClickListener {
            // TODO: Implement buy/request functionality
            // This would create a transaction or request
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state -> render(state, b) }
            }
        }
    }

    private fun render(state: MarketplaceItemDetailsUiState, b: FragmentMarketplaceItemDetailsBinding) {
        when (state) {
            is MarketplaceItemDetailsUiState.Loading -> {
                // Show loading state
            }
            is MarketplaceItemDetailsUiState.Success -> {
                val listing = state.listing
                b.textItemTitle.text = listing.title
                b.textItemPrice.text = "R${"%.2f".format(listing.price)}"
                b.textItemCategory.text = "Category: ${listing.category.name.replace("_", " ")}"
                b.textItemCondition.text = "Condition: ${listing.condition.name.replace("_", " ")}"
                b.textItemDescription.text = listing.description

                // Load first photo if available
                if (listing.photoUrls.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load(listing.photoUrls.first())
                        .centerCrop()
                        .placeholder(android.R.color.light_gray)
                        .into(b.imageItemPhoto)
                }

                // TODO: Load seller info (name, rating, profile picture)
                b.textSellerName.text = "Student Name"
                b.textSellerRating.text = "⭐ 4.8"
            }
            is MarketplaceItemDetailsUiState.Error -> {
                // Show error state
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
