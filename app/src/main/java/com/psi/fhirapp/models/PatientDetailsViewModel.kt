package com.psi.fhirapp.models

import android.app.Application
import android.content.res.Resources
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.get
import com.google.android.fhir.logicalId
import com.google.android.fhir.search.revInclude
import com.google.android.fhir.search.search
import com.psi.fhirapp.MAX_RESOURCE_COUNT
import com.psi.fhirapp.R
import com.psi.fhirapp.data.ObservationListItem
import com.psi.fhirapp.data.PatientDetailsData
import com.psi.fhirapp.data.PatientListItem
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType

class PatientDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
    ): AndroidViewModel(application){

    val livePatientData = MutableLiveData<PatientDetailsData>()

    fun getPatientDetailData() {
        viewModelScope.launch { livePatientData.value = getPatientDetailDataModel() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getPatientDetailDataModel(): PatientDetailsData  {
        var patient = getPatient()
        var observations = getObservations()
        return PatientDetailsData.toPatientDetailsData(patient, observations)
    }

    private suspend fun getPatient(): Patient {
        return fhirEngine.get(patientId)
    }

    private suspend fun getObservations(): List<ObservationListItem> {
        val observations: MutableList<ObservationListItem> = mutableListOf()
        fhirEngine
            .search<Observation> { filter(Observation.SUBJECT, { value = "Patient/$patientId" }) }
//            .take(MAX_RESOURCE_COUNT)
            .map { createObservationItem(it.resource, getApplication<Application>().resources) }
            .let { observations.addAll(it) }

        println("== observations: ${observations.size}")
        return observations
    }

    private fun createObservationItem( observation: Observation, resources: Resources): ObservationListItem {
        val observationCode = observation.code.text ?: observation.code.codingFirstRep.display

        // Show nothing if no values available for datetime and value quantity.
        val dateTimeString =
            if (observation.hasEffectiveDateTimeType()) {
                observation.effectiveDateTimeType.asStringValue()
            }
            else {
                resources.getText(R.string.message_no_datetime).toString()
            }
        val value =
            if (observation.hasValueQuantity()) {
                observation.valueQuantity.value.toString()
            } else if (observation.hasValueCodeableConcept()) {
                observation.valueCodeableConcept.coding.firstOrNull()?.display ?: ""
            } else {
                ""
            }
        val valueUnit =
            if (observation.hasValueQuantity()) {
                observation.valueQuantity.unit ?: observation.valueQuantity.code
            } else {
                ""
            }
        val valueString = "$value $valueUnit"

        return ObservationListItem(
            observation.logicalId,
            observationCode,
            dateTimeString,
            valueString,
        )
    }

}
