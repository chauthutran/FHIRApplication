package com.psi.fhirapp.data

data class ObservationItem(
    val id: String,
    val code: String,
    val effective: String,
    val value: String,
) {
    override fun toString(): String = code
}