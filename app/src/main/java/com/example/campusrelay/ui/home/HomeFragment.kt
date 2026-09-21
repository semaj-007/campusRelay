package com.example.campusrelay.ui.home

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
import com.example.campusrelay.databinding.FragmentHomeBinding
import com.example.campusrelay.ui.common.ViewModelFactory
import kotlinx.coroutines.launch

/** Home Dashboard & Eco Feed (REQ-DEL-3/4, REQ-ECO-3, and the Home Dashboard UI spec). */
class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private lateinit var adapter: NearbyTaskAdapter

    private val viewModel: HomeViewModel by viewModels {
        val app = requireActivity().application as CampusRelayApp
        ViewModelFactory {
            HomeViewModel(app.serviceLocator.deliveryRepository, app.serviceLocator.settingsRepository)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val b = binding ?: return

        adapter = NearbyTaskAdapter(onItemClick = { /* No task-detail screen yet in this prototype. */ })
        b.recyclerTasks.layoutManager = LinearLayoutManager(requireContext())
        b.recyclerTasks.adapter = adapter

        b.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        b.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chip_requests -> HomeFilter.REQUESTS
                R.id.chip_my_routes -> HomeFilter.MY_ROUTES
                else -> HomeFilter.DELIVERIES
            }
            viewModel.setFilter(filter)
        }

        b.fabCreateTask.setOnClickListener {
            findNavController().navigate(R.id.createDeliveryFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        adapter.submitList(state.tasks)
                        b.swipeRefresh.isRefreshing = state.isLoading
                        b.textOfflineBanner.visibility = if (state.isOffline) View.VISIBLE else View.GONE
                        b.textEmptyState.visibility = if (!state.isLoading && state.tasks.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.ecoScoreKg.collect { kg ->
                        val unit = getString(R.string.home_eco_score_unit)
                        b.textEcoScore.text = "%.1f %s".format(kg, unit)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
