package com.psi.fhirapp.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.datacapture.validation.Invalid
import com.google.android.fhir.datacapture.validation.QuestionnaireResponseValidator
import com.google.android.fhir.testing.jsonParser
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.fragments.AddPatientFragment
import kotlinx.coroutines.launch
import org.apache.commons.logging.Log
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import java.util.UUID


class AddPatientViewModel( application: Application) : AndroidViewModel(application){

    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    private var _questionnaireJson: String? = null
    val questionnaireJson: String
        get() = fetchQuestionnaireJson()

    private val questionnaire: Questionnaire
        get() = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaireJson) as Questionnaire

    val isPatientSaved = MutableLiveData<Boolean>()

    // *********************************************************************************************
    // For fetching Questionnaire json

    private fun fetchQuestionnaireJson(): String {
        _questionnaireJson?.let {
            return it
        }
        _questionnaireJson = readFileFromAssets("new-patient-registration-paginated.json")
        return _questionnaireJson!!
    }

    private fun readFileFromAssets(filename: String): String {
        return getApplication<Application>().assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }


    // *********************************************************************************************
    // For adding a patient from QuestionnaireResponse


    fun addPatient(questionnaireResponse: QuestionnaireResponse) {

//        val validationResult = QuestionnaireResponseValidator.validate(questionnaire, questionnaireResponse, context)


        viewModelScope.launch {
            val questionnaire =
                jsonParser.parseResource(_questionnaireJson) as Questionnaire
            val bundle = ResourceMapper.extract(questionnaire, questionnaireResponse)
            val entry = bundle.entryFirstRep
            if( entry.resource is Patient )
            {
                var patient: Patient = entry.resource as Patient
                patient.id = UUID.randomUUID().toString()
                fhirEngine.create(patient)
                isPatientSaved.value = true
            }
        }
    }

}