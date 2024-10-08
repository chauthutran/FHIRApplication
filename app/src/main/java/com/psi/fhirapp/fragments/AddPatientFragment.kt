package com.psi.fhirapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.google.android.fhir.datacapture.QuestionnaireFragment
import com.google.android.fhir.testing.jsonParser
import com.psi.fhirapp.MainActivity
import com.psi.fhirapp.R
import com.psi.fhirapp.viewmodels.AddPatientViewModel
import com.psi.fhirapp.viewmodels.CarePlanWorkflowExecutionViewModel
import com.psi.fhirapp.workflow.CareConfiguration
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.Questionnaire


class AddPatientFragment : Fragment() {

    private val addPatientViewModel: AddPatientViewModel by viewModels()
    private val careWorkflowExecutionViewModel: CarePlanWorkflowExecutionViewModel by activityViewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_patient, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpToolbar()

        if (savedInstanceState == null) {
            addQuestionnaireFragment()
        }

        addPatientViewModel.savedPatient.observe(viewLifecycleOwner){
            if(it == null) { // isPatientSaved.value == false
                Toast.makeText(requireContext(), R.string.required_fields_violated, Toast.LENGTH_SHORT).show()
                return@observe
            }

//            Toast.makeText(requireContext(), R.string.patient_added, Toast.LENGTH_SHORT).show()
//            NavHostFragment.findNavController(this).navigateUp()
            val questionnaireId = IdType((jsonParser.parseResource(addPatientViewModel.questionnaire) as Questionnaire).id).idPart
            careWorkflowExecutionViewModel.setPlanDefinitionId("Questionnaire/$questionnaireId")
//            careWorkflowExecutionViewModel.executeCareWorkflowForPatient(it)
            NavHostFragment.findNavController(this)
                .previousBackStackEntry
                ?.savedStateHandle?.set(NEW_PATIENT_RESULT_KEY, it.name[0].nameAsSingleString)
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

    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).supportActionBar?.apply {
            title = getString(R.string.add_patient)
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.toolbar_back_button)
        }

        (activity as MainActivity).setDrawerEnabled(false)
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

    private fun addQuestionnaireFragment()
    {
        runBlocking {
            addPatientViewModel.questionnaire = careWorkflowExecutionViewModel.getActivePatientRegistrationQuestionnaire()
//            careWorkflowExecutionViewModel.setCurrentStructureMap()
//            addPatientViewModel.currentTargetResourceType = careWorkflowExecutionViewModel.currentTargetResourceType
        }

        childFragmentManager.commit {
            add(
                R.id.add_patient_container,
                QuestionnaireFragment.builder()
                    .setQuestionnaire(addPatientViewModel.questionnaire)
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

        addPatientViewModel.addPatient(questionnaireResponse)
    }

    companion object {
        const val QUESTIONNAIRE_FRAGMENT_TAG = "questionnaire-fragment-tag"
        const val NEW_PATIENT_RESULT_KEY = "newPatientUid"
    }
}