package com.psi.fhirapp.models


import android.app.Application
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.search.Order
import com.google.android.fhir.search.StringFilterModifier
import com.google.android.fhir.search.search
import com.google.android.fhir.sync.Sync
import com.google.android.fhir.sync.SyncJobStatus
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.AppFhirSyncWorker
import com.psi.fhirapp.data.PatientItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import java.time.LocalDate
import java.time.format.DateTimeFormatter



class PatientListViewModel(application: Application): AndroidViewModel(application) {
    /**
     * MutableSharedFlow provides the abilities to emit a value,
     * to tryEmit without suspension/stop if possible,
     * to track the subscriptionCount,
     * and to resetReplayCache
     **/
    private val _pollState = MutableSharedFlow<SyncJobStatus>()
    val pollState: Flow<SyncJobStatus>
    get() {
        return _pollState
    }

    private var fhirEngine: FhirEngine = FhirApplication.fhirEngine(application.applicationContext)


    /**
     * MutableLiveData provides mutability, allowing the modification of its value.
     * MutableLiveData is commonly used within ViewModels to hold and expose data
     * that can be updated over time.
     **/
    val liveSearchedPatients = MutableLiveData<List<PatientItem>>()

    init {
        updatePatientList{ getSearchResults() }
    }

    fun triggerOneTimeSync() {

        /**
         * viewModelScope.launch will start a coroutine in the viewModelScope.
         * his means when the job that we passed to viewModelScope gets canceled,
         * all coroutines in this job/scope will be cancelled.
         *
         * If the user left the Activity before delay returned, this coroutine will
         * automatically be cancelled when onCleared is called upon destruction of the ViewModel.
         *
         *
         * This coroutine initiates a one-time sync with the FHIR server using the AppFhirSyncWorker
         * we defined earlier. It will then update the UI based on the state of the sync process.
         * **/
        viewModelScope.launch {
            Sync.oneTimeSync<AppFhirSyncWorker>(getApplication())
                .shareIn(this, SharingStarted.Eagerly, 10)
                // Emits a value to this shared flow, suspending/stopping on buffer overflow.
                .collect { _pollState.emit(it) }
        }
    }

    /* Fetches patients stored locally based on the city they are in, and then updates the city field for
    each patient. Once that is complete, trigger a new sync so the changes can be uploaded.
    */
    fun triggerUpdate() {
//        viewModelScope.launch {
//            val fhirEngine = FhirApplication.fhirEngine(getApplication())
//
//            /** Use the FHIR engine to search for patients with an address city of Wakefield
//            * The result will be a list of patients from Wakefield.
//            **/
//            val patientsFromWakefield =
//                fhirEngine.search<Patient> {
//                    filter(
//                        Patient.ADDRESS_CITY,
//                        {
//                            modifier =  StringFilterModifier.MATCHES_EXACTLY
//                            value = "Wakefield"
//                        }
//                    )
//                }
//        }
    }

    fun searchPatientsByName(nameQuery: String) {
        viewModelScope.launch {
            val fhirEngine = FhirApplication.fhirEngine(getApplication())
            if (nameQuery.isNotEmpty()) {
                val searchResult = fhirEngine.search<Patient> {
                    filter(
                        Patient.NAME,
                        {
                            modifier = StringFilterModifier.CONTAINS
                            value = nameQuery
                        },
                    )
                }
                /** liveSearchedPatients.value is used to set the value of a MutableLiveData synchronously.
                 * It should be called from the main thread,
                 * as it directly updates the value and triggers observers immediately.
                 **/
                liveSearchedPatients.value  =  searchResult.mapIndexed { index, fhirPatient -> fhirPatient.resource.toPatientItem(index + 1) }
            }
        }
    }

    /**
     * [updatePatientList] calls the search and count lambda and updates the live data values
     * accordingly. It is initially called when this [ViewModel] is created. Later its called by the
     * client every time search query changes or data-sync is completed.
     */
    private fun updatePatientList(
        search: suspend () -> List<PatientItem>,
    ) {
        viewModelScope.launch { liveSearchedPatients.value = search() }
    }


    /**
     * Keyword "async" is the same "async/await" keyword in JS Async
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getSearchResults(): List<PatientItem> {
        val patients: MutableList<PatientItem> = mutableListOf()

        var searchResult = fhirEngine.search<Patient> {
            sort(Patient.GIVEN, Order.ASCENDING)
        }
        searchResult.mapIndexed { index, fhirPatient -> fhirPatient.resource.toPatientItem(index + 1) }
            .let { patients.addAll(it) }

        return patients
    }

}


/**
 * Below is an internal function, it will be visible everywhere in the same module.
 **/
@RequiresApi(Build.VERSION_CODES.O)
internal fun Patient.toPatientItem(position: Int): PatientItem {
    Log.d("Patient.toPatientItem", "$idElement" )

    // Show nothing if no values available for gender and date of birth.
    val patientId = if (hasIdElement()) idElement.idPart else ""
    val name = if (hasName()) name[0].nameAsSingleString else ""
    val gender = if (hasGenderElement()) genderElement.valueAsString else ""
    val dob =
        if (hasBirthDateElement()) {
            LocalDate.parse(birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
        } else {
            null
        }
    val phone = if (hasTelecom()) telecom[0].value else ""
    val city = if (hasAddress()) address[0].city else ""
    val country = if (hasAddress()) address[0].country else ""
    val isActive = active
    val html: String = if (hasText()) text.div.valueAsString else ""

    return PatientItem(
        id = position.toString(),
        resourceId = patientId,
        name = name,
        gender = gender ?: "",
        dob = dob,
        phone = phone ?: "",
        city = city ?: "",
        country = country ?: "",
        isActive = isActive,
        html = html,
    )
}