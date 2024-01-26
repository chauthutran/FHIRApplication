package com.psi.fhirapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.psi.fhirapp.MainActivity
import com.psi.fhirapp.R
import com.psi.fhirapp.models.AddPatientViewModel
import org.apache.commons.logging.Log
import org.hl7.fhir.r4.model.QuestionnaireResponse

/**
 * Requirements:
 *
 *  1. Add a dependency from the reference application to the data capture library (datacapture mdoule)
 *  2. Create a FHIR questionnaire resource which can be stored in the assets folder in the reference
 *     app that defines the questions to be asked while registering a patient (ask @fredhersch https://github.com/fredhersch)
 *  3. Use the QuestionnaireFragment to render the above questionnaire
 *  4. A new 'add patient' button (e.g. FAB https://material.io/components/buttons-floating-action-button)
 *     on the patient list screen
 *  5. Modify the navigation graph so that clicking the add patient button would take the user to the new fragment
 *  6. Use the ResourceMapper API to create a patient from the questionnaire response
 *  7. Save the resulting patient into the db using the FHIR engine API
 *  8. Show the new patient in the patient list view
 **/
class AddPatientFragment : Fragment() {

    private val viewModel: AddPatientViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_patient, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }
        (requireActivity() as AppCompatActivity).supportActionBar?.apply { title = "Add patient" }
        (activity as MainActivity).setDrawerEnabled(false)

        viewModel.isPatientSaved.observe(viewLifecycleOwner){
            if(!it) { // isPatientSaved.value == false
                Toast.makeText(requireContext(), R.string.required_fields_violated, Toast.LENGTH_SHORT).show()
                return@observe
            }

            Toast.makeText(requireContext(), R.string.patient_added, Toast.LENGTH_SHORT).show()
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

    private fun addQuestionnaireFragment()
    {
        childFragmentManager.commit {
            add(
                R.id.add_patient_container,
                QuestionnaireFragment.builder()
                    .setQuestionnaire(viewModel.questionnaireJson)
                    .setShowSubmitButton(true)
                    .build(),
                QUESTIONNAIRE_FRAGMENT_TAG,
            )
        }
    }

    private fun onSubmitHandler()
    {
        // Get a questionnaire response
        val questionnaireFragment = childFragmentManager.findFragmentByTag(QUESTIONNAIRE_FRAGMENT_TAG) as QuestionnaireFragment
        val questionnaireResponse = questionnaireFragment.getQuestionnaireResponse()

        viewModel.addPatient(questionnaireResponse)
    }

    private fun watchSavePatientAction() {
        viewModel.isPatientSaved.observe(viewLifecycleOwner) {
            if (!it) {
                Toast.makeText(requireContext(), "Inputs are missing.", Toast.LENGTH_SHORT).show()
                return@observe
            }
            Toast.makeText(requireContext(), "Patient is saved.", Toast.LENGTH_SHORT).show()
//            NavHostFragment.findNavController(this).navigateUp()
        }
    }

    companion object {
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
    }
}