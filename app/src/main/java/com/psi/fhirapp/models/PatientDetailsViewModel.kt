package com.psi.fhirapp.models

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.search
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.data.PatientItem
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.RiskAssessment

class PatientDetailsViewModel(application: Application): AndroidViewModel(application){

    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)

    private var livePatientData = MutableLiveData<PatientItem>()

    fun getPatientDetailData(patientId: String) {
        viewModelScope.launch { livePatientData.value = getPatientDataFromDb(patientId) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getPatientDataFromDb(patientId: String): PatientItem {

        val patients: MutableList<PatientItem> = mutableListOf()

        var searchResult = fhirEngine.search<Patient> {
            filter(Resource.RES_ID, { value = of(patientId) })
        }
        searchResult.mapIndexed { index, fhirPatient -> fhirPatient.resource.toPatientItem(index + 1) }
            .let { patients.addAll(it) }

        return patients.first();
    }
}