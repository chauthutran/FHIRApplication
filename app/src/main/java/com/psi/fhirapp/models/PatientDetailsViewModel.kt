package com.psi.fhirapp.models

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.search
import com.psi.fhirapp.data.PatientDetailProperty
import com.psi.fhirapp.data.PatientProperty
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import java.util.Locale

class PatientDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
    ): AndroidViewModel(application){

    val livePatientData = MutableLiveData<List<PatientDetailProperty>>()

    fun getPatientDetailData() {
        viewModelScope.launch { livePatientData.value = getPatientDetailDataModel() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getPatientDetailDataModel(): MutableList<PatientDetailProperty> {
        val searchResult = fhirEngine.search<Patient> {
            filter(Resource.RES_ID, { value = of(patientId) })
        }

        val data = mutableListOf<PatientDetailProperty>()

        searchResult.first().let {
            data.addPatientDetailData( it.resource )
        }

        return data
    }

    private fun MutableList<PatientDetailProperty>.addPatientDetailData(
        patient: Patient
    ) {
        patient
            .toPatientItem(0)
            .let { patientItem ->
                add(
                    PatientDetailProperty(
                        PatientProperty("Name", patientItem.name),
                    ),
                )
                add(
                    PatientDetailProperty(
                        PatientProperty("Resource Id", patientItem.resourceId),
                    ),
                )
                add(
                    PatientDetailProperty(
                        PatientProperty("Phone", patientItem.phone),
                    ),
                )
                add(
                    PatientDetailProperty(
                        PatientProperty(
                            "Address",
                            "${patientItem.city}, ${patientItem.country} ",
                        ),
                    ),
                )
                add(
                    PatientDetailProperty(
                        PatientProperty(
                            "Birthdate",
                            patientItem.dob?.toString() ?: "",
                        ),
                    ),
                )
                add(
                    PatientDetailProperty(
                        PatientProperty(
                            "Gender",
                            patientItem.gender.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                            },
                        ),
                        lastInGroup = true,
                    ),
                )
            }
    }
}
