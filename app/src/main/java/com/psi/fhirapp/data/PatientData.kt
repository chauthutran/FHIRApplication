package com.psi.fhirapp.data

import org.hl7.fhir.r4.model.Patient
import java.time.LocalDate

interface PatientData {
    val id: String
    val resourceId: String
    val name: String
    val gender: String
    val dob: LocalDate?
    val phone: String
    val city: String
    val country: String
    val isActive: Boolean

}