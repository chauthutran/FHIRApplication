package com.psi.fhirapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.google.android.fhir.FhirEngine
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.MainActivity
import com.psi.fhirapp.adapters.PatientDetailsRecyclerViewAdapter
import com.psi.fhirapp.data.PatientItem
import com.psi.fhirapp.databinding.FragmentPatientDetailsBinding
import com.psi.fhirapp.models.PatientDetailsViewModel

class PatientDetailsFragment : Fragment() {

    private lateinit var _binding: FragmentPatientDetailsBinding
    private lateinit var viewModel: PatientDetailsViewModel

    private var patientId : String = ""


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

        setFragmentResultListener("selectedItem") { requestKey, bundle ->
            // We use a String here, but any type that can be put in a Bundle is supported.
            patientId = bundle.getString("bundleItemId") ?: ""

            var fhirEngine: FhirEngine = FhirApplication.fhirEngine(requireContext())
            viewModel = PatientDetailsViewModel(requireActivity().application, fhirEngine, patientId )
            var adapter = PatientDetailsRecyclerViewAdapter(requireContext())
            binding.recyclerDetails.adapter = adapter

            (requireActivity() as AppCompatActivity).supportActionBar?.apply {
                title = "Patient Card"
                setDisplayHomeAsUpEnabled(true)
            }


            viewModel.livePatientData.observe(viewLifecycleOwner) {
                adapter.submitList(it)
            }
            viewModel.getPatientDetailData()
        }

    }

}

interface PatientDetailData {
    val firstInGroup: Boolean
    val lastInGroup: Boolean
}


data class PatientProperty(val header: String, val value: String)

data class PatientDetailProperty(
    val patientProperty: PatientProperty,
    override val firstInGroup: Boolean = false,
    override val lastInGroup: Boolean = false,
) : PatientDetailData