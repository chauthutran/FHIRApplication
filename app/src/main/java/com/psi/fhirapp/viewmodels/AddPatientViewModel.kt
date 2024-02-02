package com.psi.fhirapp.viewmodels

import android.app.Application
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.testing.jsonParser
import com.psi.fhirapp.FhirApplication
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse
import java.util.UUID


class AddPatientViewModel( application: Application) : AndroidViewModel(application){

    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

//    private var _questionnaireJson: String? = null

    var questionnaire: String = ""
    private val questionnaireResource: Questionnaire
        get() =
            FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire)
                    as Questionnaire

    val isPatientSaved = MutableLiveData<Boolean>()

    var savedPatient = MutableLiveData<Patient?>()

    fun addPatient(questionnaireResponse: QuestionnaireResponse) {

//        val validationResult = QuestionnaireResponseValidator.validate(questionnaire, questionnaireResponse, context)


        viewModelScope.launch {
            val bundle = ResourceMapper.extract(questionnaireResource, questionnaireResponse)
            val entry = bundle.entryFirstRep
            if( entry.resource is Patient )
            {
                var patient: Patient = entry.resource as Patient
                patient.id = UUID.randomUUID().toString()

                fhirEngine.create(patient)
                savedPatient.value = patient

                isPatientSaved.value = true
            }
            else
            {
                savedPatient.value = null
            }
        }
    }

}