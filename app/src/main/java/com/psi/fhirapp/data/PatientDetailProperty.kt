package com.psi.fhirapp.data


data class PatientDetailProperty(
    val patientProperty: PatientProperty,
    val firstInGroup: Boolean = false,
    val lastInGroup: Boolean = false,
)

data class PatientProperty(val header: String, val value: String)