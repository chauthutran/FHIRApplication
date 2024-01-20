package com.psi.fhirapp.fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.fhir.sync.SyncJobStatus
import com.psi.fhirapp.R
import com.psi.fhirapp.adapters.PatientItemRecyclerViewAdapter
import com.psi.fhirapp.data.PatientListItem
import com.psi.fhirapp.databinding.FragmentPatientListBinding
import com.psi.fhirapp.models.PatientListViewModel
import kotlinx.coroutines.launch


class PatientListFragment : Fragment() {
    private lateinit var searchView: SearchView
    // Layout "fragment_patient_list_view"
    private var _binding: FragmentPatientListBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: PatientListViewModel by viewModels()

    /**
     * Inflate the layout in the method "onCreateView"
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPatientListBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * The method "onViewCreated" called immediately after "onCreateView" has returned,
     * but before any saved state has been restored in to the view.
     *
     * Inside this method, we can use the method "findViewById" to find/get a view in the layout
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSearchView()
        initMenu()

        var adapter = createAdapter().apply {
            binding.patientList.adapter = this
            viewModel.liveSearchedPatients.observe(viewLifecycleOwner) { submitList(it) }
        }

        /**
         *  Execute while the fragment's view is in the valid state
         *  If you navigate away from the fragment, the coroutine still executing in that scope will be cancelled
         */
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Display a message when the sync is finished and then refresh the list of patients
                // on the UI by sending a search patient request
                viewModel.pollState.collect { handleSyncJobStatus(it) }
            }
        }
    }

    private fun createAdapter(): PatientItemRecyclerViewAdapter {
        // Implement Item clicked
        return PatientItemRecyclerViewAdapter { view: View, patientListItem: PatientListItem ->
            setFragmentResult("selectedItem", bundleOf("bundleItemId" to patientListItem.resourceId))
//            findNavController().navigate(R.id.nav_patient_list_to_details)

            val bundle = Bundle()
            bundle.putString("patient_id", patientListItem.resourceId)
            findNavController().navigate(R.id.nav_patient_list_to_details, bundle)
        }
    }

    /**
     * When the sync process finishes, a toast message will display notifying the user,
     * and the app will then display all patients by invoking a search with an empty name.
     */
    private fun handleSyncJobStatus(syncJobStatus: SyncJobStatus) {
        when (syncJobStatus) {
            is SyncJobStatus.Finished -> {
                Toast.makeText(requireContext(), "Sync Finished", Toast.LENGTH_SHORT).show()
                    viewModel.searchPatientsByName("")
            }
            else -> {}
        }
    }

    private fun initMenu() {
        (requireActivity() as MenuHost).addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu, menu)
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.sync -> {
                            viewModel.triggerOneTimeSync()
                            true
                        }
                        R.id.update -> {
                            viewModel.triggerUpdate()
                            true
                        }
                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED,
        )
    }

    private fun initSearchView() {
        searchView = binding.search
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onQueryTextChange(newText: String): Boolean {
                    viewModel.searchPatientsByName(newText)
                    return true
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onQueryTextSubmit(query: String): Boolean {
                    viewModel.searchPatientsByName(query)
                    return true
                }
            },
        )
        searchView.setOnQueryTextFocusChangeListener { view, focused ->
            if (!focused) {
                (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                    .hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (searchView.query.isNotEmpty()) {
                            searchView.setQuery("", true)
                        } else {
                            isEnabled = false
                            activity?.onBackPressed()
                        }
                    }
                },
            )
    }
//
//    override fun forwardClick(patientItem: PatientItem) {
//        findNavController().navigate(R.id.nav_patient_list_to_details)
//    }

    // To avoid memory leak from injected adapter
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
