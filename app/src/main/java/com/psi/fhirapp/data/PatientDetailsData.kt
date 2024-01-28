package com.psi.fhirapp.data

import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import java.time.LocalDate
import java.time.format.DateTimeFormatter


data class PatientDetailsData (
    override val id: String,
    override val resourceId: String,
    override val name: String,
    override val gender: String,
    override val dob: LocalDate,
    override val phone: String,
    override val city: String,
    override val country: String,
    override val isActive: Boolean,
    val html: String
): PatientData {

    companion object {
        fun toPatientDetailsData(patient: Patient): PatientDetailsData {
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

            return PatientDetailsData(
                id = patientId,
                resourceId = patientId,
                name = name,
                gender = gender ?: "",
                dob = dob!!,
                phone = phone ?: "",
                city = city ?: "",
                country = country ?: "",
                isActive = isActive,
                html = html,
            )
        }
    }
}
