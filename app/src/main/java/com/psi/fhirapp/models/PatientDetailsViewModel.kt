package com.psi.fhirapp.models

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.revInclude
import com.google.android.fhir.search.search
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.R
import com.psi.fhirapp.data.PatientItem
import com.psi.fhirapp.fragments.PatientDetailData
import com.psi.fhirapp.fragments.PatientDetailProperty
import com.psi.fhirapp.fragments.PatientProperty
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.RiskAssessment
import java.util.Locale

class PatientDetailsViewModel(
    application: Application,
    private val fhirEngine: FhirEngine,
    private val patientId: String,
    ): AndroidViewModel(application){

    val livePatientData = MutableLiveData<List<PatientDetailData>>()

    fun getPatientDetailData() {
        viewModelScope.launch { livePatientData.value = getPatientDetailDataModel() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getPatientDetailDataModel(): MutableList<PatientDetailData> {
        val searchResult = fhirEngine.search<Patient> {
            filter(Resource.RES_ID, { value = of(patientId) })
        }

        val data = mutableListOf<PatientDetailData>()

        searchResult.first().let {
            data.addPatientDetailData( it.resource )
        }

        return data
    }

    private fun MutableList<PatientDetailData>.addPatientDetailData(
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


//private fun getString(resId: Int) = getApplication<Application>().resources.getString(resId)
