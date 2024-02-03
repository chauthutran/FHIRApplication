package com.psi.fhirapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.psi.fhirapp.R
import com.psi.fhirapp.databinding.FragmentLoginBinding
import com.psi.fhirapp.databinding.FragmentPatientListBinding
import com.psi.fhirapp.viewmodels.CarePlanWorkflowExecutionViewModel
import com.psi.fhirapp.workflow.CareConfiguration
import com.psi.fhirapp.workflow.ConfigurationManager

class LoginFragment : Fragment() {


    private val carePlanWorkflowExecutionViewModel by activityViewModels<CarePlanWorkflowExecutionViewModel>()

    private lateinit var careConfiguration: CareConfiguration

    private var _binding: FragmentLoginBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        careConfiguration = ConfigurationManager.getCareConfiguration(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = resources.getString(R.string.app_name)
            setDisplayHomeAsUpEnabled(false)
        }

        setIGSpinner()

        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.nav_login_page_to_patient_list)
        }
    }

    private fun setIGSpinner() {
        val igSpinner = binding.igSpinner
        val adapter =
            ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item)
        careConfiguration.supportedImplementationGuides.forEach {
            adapter.add(it.implementationGuideConfig.entryPoint)
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        igSpinner.adapter = adapter

        igSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (p0 != null) {
                        carePlanWorkflowExecutionViewModel.currentIg = p0.getItemAtPosition(p2) as String
                        carePlanWorkflowExecutionViewModel.setActiveRequestConfiguration(
                            carePlanWorkflowExecutionViewModel.currentIg
                        )
                    }
                }
                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
    }
}