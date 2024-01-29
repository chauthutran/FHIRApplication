package com.psi.fhirapp.data

import android.app.Application
import android.content.res.Resources
import com.google.android.fhir.SearchResult
import com.google.android.fhir.logicalId
import com.psi.fhirapp.MAX_RESOURCE_COUNT
import com.psi.fhirapp.R
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.time.LocalDate
import java.time.format.DateTimeFormatter


data class PatientDetailsData (
    override val id: String,
    override val resourceId: String,
    override val name: String,
    override val gender: String,
    override val dob: LocalDate?,
    override val phone: String,
    override val city: String,
    override val country: String,
    override val isActive: Boolean,
    val observations: List<ObservationListItem>
): PatientData {

    companion object {
        fun toPatientDetailsData(searchData: SearchResult<Patient>): PatientDetailsData {
            var patient = searchData.resource

            // Show nothing if no values available for gender and date of birth.
            val patientId = if (patient.hasIdElement()) patient.idElement.idPart else ""
            val name = if (patient.hasName()) patient.name[0].nameAsSingleString else ""
            val gender = if (patient.hasGenderElement()) patient.genderElement.valueAsString else ""
            val dob =
                if (patient.hasBirthDateElement()) {
                    LocalDate.parse(patient.birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
                } else {
                    null
                }
            val phone = if (patient.hasTelecom()) patient.telecom[0].value else ""
            val city = if (patient.hasAddress()) patient.address[0].city else ""
            val country = if (patient.hasAddress()) patient.address[0].country else ""
            val isActive = patient.active
            val html: String = if (patient.hasText()) patient.text.div.valueAsString else ""

            // For Observations data
            var observationData = listOf<ObservationListItem>()
            searchData.revIncluded?.get(ResourceType.Observation to Observation.SUBJECT.paramName)?.let {
                observationData = PatientDetailsData.getObservationsData(it as List<Observation>)
            }


            return PatientDetailsData(
                id = patientId,
                resourceId = patientId,
                name = name,
                gender = gender ?: "",
                dob = dob,
                phone = phone ?: "",
                city = city ?: "",
                country = country ?: "",
                isActive = isActive,
                observations = observationData
            )
        }

        private fun getObservationsData(observations: List<Observation>) : List<ObservationListItem> {
            val items: MutableList<ObservationListItem> = mutableListOf()

            if (observations.isNotEmpty()) {
                observations
                    .take(MAX_RESOURCE_COUNT)
                    .map { createObservationItem(it) }
//                    .mapIndexed { index, observationItem ->
//                            observationItem
//                    }
                    .let { items.addAll(it) }
            }
            return items
        }

        private fun createObservationItem(
            observation: Observation
        ): ObservationListItem {
            val observationCode = observation.code.text ?: observation.code.codingFirstRep.display

            // Show nothing if no values available for datetime and value quantity.
            val dateTimeString =
                if (observation.hasEffectiveDateTimeType()) {
                    observation.effectiveDateTimeType.asStringValue()
                }
            else
                {
                    "No effective datetime"
                }
//                else {
//                    resources.getText(R.string.message_no_datetime).toString()
//                }
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
}
