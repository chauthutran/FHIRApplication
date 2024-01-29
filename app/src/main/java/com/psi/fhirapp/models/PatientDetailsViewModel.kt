package com.psi.fhirapp.models

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.search
import com.psi.fhirapp.data.PatientDetailsData
import kotlinx.coroutines.launch
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
        val searchResult = fhirEngine.search<Patient> {
            filter(Resource.RES_ID, { value = of(patientId) })
        }

        val data: PatientDetailsData

        searchResult.first().let {
            data = PatientDetailsData.toPatientDetailsData(it)
        }

        return data
    }


}
