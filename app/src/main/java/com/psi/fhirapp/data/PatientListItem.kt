package com.psi.fhirapp.data

import java.time.LocalDate


/** The Patient's details for display purposes. */
data class PatientListItem(
    val id: String,
    val resourceId: String,
    val name: String,
    val gender: String,
    val dob: LocalDate? = null,
    val phone: String,
    val city: String,
    val country: String,
    val isActive: Boolean,
    val html: String,
    var risk: String? = "",
//    var riskItem: RiskAssessmentItem? = null,
) {
    override fun toString(): String = name
}
