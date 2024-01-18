package com.psi.fhirapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.FhirEngine
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.MainActivity
import com.psi.fhirapp.adapters.PatientDetailsRecyclerViewAdapter
import com.psi.fhirapp.adapters.PatientItemRecyclerViewAdapter
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import com.psi.fhirapp.models.PatientDetailsViewModel

class PatientDetailsFragment : Fragment() {

    private lateinit var fhirEngine: FhirEngine
    private var _binding: FragmentPatientDetailsBinding?= null

//    private val args: PatientDetailsFragmentArgs by navArgs()
    private val patientDetailsViewModel: PatientDetailsViewModel by viewModels()

    private val binding
        get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

//        PatientItemRecyclerViewAdapter().apply {
//            binding.root.adapter = this
//            viewModel.liveSearchedPatients.observe(viewLifecycleOwner) { submitList(it) }
//        }



//        patientDetailsViewModel = ViewModelProvider( this, args.patientId))
//                .get(PatientDetailsViewModel::class.java)
//
//        val adapter = PatientDetailsRecyclerViewAdapter(::onAddScreenerClick)
//        binding.recycler.adapter = adapter
//        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
//            title = "Patient Card"
//            setDisplayHomeAsUpEnabled(true)
//        }
//        patientDetailsViewModel.livePatientData.observe(viewLifecycleOwner) {
//            adapter.submitList(it)
//            if (!it.isNullOrEmpty()) {
//                editMenuItem?.isEnabled = true
//            }
//        }
//        patientDetailsViewModel.getPatientDetailData()
// //       (activity as MainActivity).setDrawerEnabled(false)
    }

}