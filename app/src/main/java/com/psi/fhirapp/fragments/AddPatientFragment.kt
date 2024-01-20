package com.psi.fhirapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.psi.fhirapp.R


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

}