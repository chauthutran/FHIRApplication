package com.psi.fhirapp.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.datacapture.mapping.ResourceMapper
import com.google.android.fhir.get
import com.psi.fhirapp.FhirApplication
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.QuestionnaireResponse

class EditPatientViewModel(
    application: Application,
    private val patientId: String,
    ): AndroidViewModel(application) {


    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    val livePatientData = liveData{emit(retrievePatientData())}
    val isPatientSaved = MutableLiveData<Boolean>()

    private var questionnaireJson: String? = null

    private val questionnaire: String
        get() = getQuestionnaireJson()


    private val questionnaireResource: Questionnaire
        get() = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser().parseResource(questionnaire)
                    as Questionnaire

    private suspend fun retrievePatientData(): Pair<String, String> {

        var patient: Patient = fhirEngine.get<Patient>(patientId)
        val question = readFileFromAssets("new-patient-registration-paginated.json").trimIndent()

        val parser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()
        val questionnaire = parser.parseResource(Questionnaire::class.java, question) as Questionnaire

        val questionnaireResponse: QuestionnaireResponse = ResourceMapper.populate(questionnaire, patient)
        val questionnaireResponseJson = parser.encodeResourceToString(questionnaireResponse)

        return question to questionnaireResponseJson
    }

    fun updatePatient(questionnaireResponse: QuestionnaireResponse) {
        viewModelScope.launch {
            var entryData = ResourceMapper.extract(questionnaireResource, questionnaireResponse).entryFirstRep
            if( entryData.resource is Patient )
            {
                var patient = entryData.resource as Patient
                if( patient.hasName()
                    && patient.name[0].hasGiven()
                    && patient.name[0].hasFamily()
                    && patient.hasBirthDate()
                    && patient.hasTelecom()
                    && patient.telecom[0].value != null
                ) {
                    patient.id = patientId
                    fhirEngine.update(patient)
                    isPatientSaved.value = true
                    return@launch // After saving, run "viewModelScope.launch" again
                }
            }
            else
            {
                isPatientSaved.value = false
            }

        }
    }


    private fun getQuestionnaireJson(): String {

        // if the questionnaireJson is not null, return "questionnaireJson"
        questionnaireJson?.let {
            return it
        }

        // Read the "questionnaireJson" from the file
        questionnaireJson = readFileFromAssets("new-patient-registration-paginated.json")
        return questionnaireJson!!
    }

    private fun readFileFromAssets(filename: String): String {
        return getApplication<Application>().assets.open(filename).bufferedReader().use {
            it.readText()
        }
    }

}