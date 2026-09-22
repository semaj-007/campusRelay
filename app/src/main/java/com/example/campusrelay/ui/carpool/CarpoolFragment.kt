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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.campusrelay.CampusRelayApp
import com.example.campusrelay.R
import com.example.campusrelay.databinding.FragmentCarpoolBinding
import com.example.campusrelay.ui.common.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Carpool Fragment - Search and browse ride offers (REQ-CAR-1, REQ-CAR-2, REQ-CAR-3). */
class CarpoolFragment : Fragment() {

    private var binding: FragmentCarpoolBinding? = null
    private lateinit var adapter: RideOfferAdapter

    private val viewModel: CarpoolViewModel by viewModels {
        val app = requireActivity().application as CampusRelayApp
        ViewModelFactory {
            CarpoolViewModel(app.serviceLocator.carpoolRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCarpoolBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return

        adapter = RideOfferAdapter(onItemClick = { rideOffer ->
            val action = CarpoolFragmentDirections.actionCarpoolToRideDetails(rideOffer.id)
            findNavController().navigate(action)
        })
        b.recyclerRides.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerRides.adapter = adapter

        b.fabOfferRide.setOnClickListener {
            findNavController().navigate(R.id.createRideOfferFragment)
        }

        b.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        b.buttonSearch.setOnClickListener {
            val origin = b.editOrigin.text?.toString().orEmpty()
            val destination = b.editDestination.text?.toString().orEmpty()
            val dateText = b.editDate.text?.toString().orEmpty()

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = try { dateFormat.parse(dateText) } catch (e: Exception) { null }

            viewModel.searchRides(origin, destination, date)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList(state.rideOffers)
                    b.swipeRefresh.isRefreshing = state.isLoading
                    b.textEmptyState.visibility = if (!state.isLoading && state.rideOffers.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
