package com.psi.fhirapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.psi.fhirapp.MainActivity
import com.psi.fhirapp.R
import com.psi.fhirapp.models.EditPatientViewModel
import com.psi.fhirapp.models.PatientDetailsViewModel
import org.hl7.fhir.utilities.SystemExitManager.finish

class EditPatientFragment : Fragment() {

    private lateinit var viewModel: EditPatientViewModel
    private val args: EditPatientFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_patient, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = EditPatientViewModel(requireActivity().application, args.patientId )

        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.edit_patient)
        }
        (activity as MainActivity).setDrawerEnabled(false)

        viewModel.livePatientData.observe(viewLifecycleOwner){ addQuestionnaireInFragment(it)}
        viewModel.isPatientSaved.observe(viewLifecycleOwner){
            if(!it) { // isPatientSaved.value == false
                Toast.makeText(requireContext(), R.string.required_fields_violated, Toast.LENGTH_SHORT).show()
                return@observe
            }

            Toast.makeText(requireContext(), R.string.patient_updated, Toast.LENGTH_SHORT).show()
            NavHostFragment.findNavController(this).navigateUp()
        }

        /** Use the provided Submit button from the Structured Data Capture Library  */
        childFragmentManager.setFragmentResultListener(
            QuestionnaireFragment.SUBMIT_REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, _ ->
            onSubmitHandler()
        }
    }



    private fun addQuestionnaireInFragment(pair: Pair<String, String>) {
        childFragmentManager.commit {
            add(
                R.id.edit_patient_container,
                QuestionnaireFragment.builder()
                    .setQuestionnaire(pair.first)
                    .setQuestionnaireResponse(pair.second)
                    .build(),
                QUESTIONNAIRE_FRAGMENT_TAG,
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                NavHostFragment.findNavController(this).navigateUp()
                true
            }
            else -> false
        }
    }

    private fun onSubmitHandler() {
        val questionnaireFragment =
            childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
        viewModel.updatePatient(questionnaireFragment.getQuestionnaireResponse())
    }

    companion object {
        const val QUESTIONNAIRE_FRAGMENT_TAG = "edit-questionnaire-fragment-tag"
    }
}