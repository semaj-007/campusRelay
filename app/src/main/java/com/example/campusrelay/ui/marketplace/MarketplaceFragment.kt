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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrelay.CampusRelayApp
import com.example.campusrelay.R
import com.example.campusrelay.data.model.ListingCategory
import com.example.campusrelay.data.model.MarketplaceListing
import com.example.campusrelay.databinding.FragmentMarketplaceBinding
import com.example.campusrelay.ui.common.ViewModelFactory
import kotlinx.coroutines.launch

/** Marketplace Fragment - Browse and filter marketplace listings (REQ-MKT-1, REQ-MKT-2, REQ-MKT-3). */
class MarketplaceFragment : Fragment() {

    private var binding: FragmentMarketplaceBinding? = null
    private lateinit var adapter: MarketplaceListingAdapter

    private val viewModel: MarketplaceViewModel by viewModels {
        val app = requireActivity().application as CampusRelayApp
        ViewModelFactory {
            MarketplaceViewModel(app.serviceLocator.marketplaceRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMarketplaceBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return

        adapter = MarketplaceListingAdapter(onItemClick = { listing ->
            val action = MarketplaceFragmentDirections.actionMarketplaceToListingDetails(listing.id)
            findNavController().navigate(action)
        })
        b.recyclerListings.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerListings.adapter = adapter

        b.fabCreateListing.setOnClickListener {
            findNavController().navigate(R.id.createListingFragment)
        }

        b.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        b.editSearch.setOnEditorActionListener { _, _, _ ->
            viewModel.search(b.editSearch.text.toString())
            true
        }

        b.chipGroupCategories.setOnCheckedStateChangeListener { _, checkedIds ->
            val category = when (checkedIds.firstOrNull()) {
                R.id.chip_all -> null
                R.id.chip_books -> ListingCategory.BOOKS
                R.id.chip_electronics -> ListingCategory.ELECTRONICS
                R.id.chip_clothing -> ListingCategory.CLOTHING
                R.id.chip_furniture -> ListingCategory.FURNITURE
                R.id.chip_other -> ListingCategory.OTHER
                else -> null
            }
            viewModel.filterByCategory(category)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.listings)
                    b.swipeRefresh.isRefreshing = state.isLoading
                    b.textEmptyState.visibility = if (!state.isLoading && state.listings.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
