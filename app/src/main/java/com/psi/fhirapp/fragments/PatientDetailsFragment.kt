package com.psi.fhirapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.MainActivity
import com.psi.fhirapp.R
import com.psi.fhirapp.adapters.PatientDetailsRecyclerViewAdapter
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import com.psi.fhirapp.models.PatientDetailsViewModel

class PatientDetailsFragment : Fragment() {

    private lateinit var _binding: FragmentPatientDetailsBinding
    private lateinit var viewModel: PatientDetailsViewModel
    private val args: PatientDetailsFragmentArgs by navArgs()

    private var editMenuItem: MenuItem? = null

    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPatientDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var fhirEngine: FhirEngine = FhirApplication.fhirEngine(requireContext())
        viewModel = PatientDetailsViewModel(requireActivity().application, fhirEngine, args.patientId )
        var adapter = PatientDetailsRecyclerViewAdapter(requireContext())
        binding.recyclerDetails.adapter = adapter

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = "Patient Details"
            setDisplayHomeAsUpEnabled(true)
        }

        viewModel.livePatientData.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            if (!it.isNullOrEmpty()) {
                editMenuItem?.isEnabled = true
            }
        }
        viewModel.getPatientDetailData()
        (activity as MainActivity).setDrawerEnabled(false)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.patient_details_menu, menu)
        editMenuItem = menu.findItem(R.id.menu_patient_edit)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }
//            R.id.menu_patient_edit -> {
//                findNavController()
//                    .navigate(PatientDetailsFragmentDirections.navigateToEditPatient(args.patientId))
//                true
//            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
