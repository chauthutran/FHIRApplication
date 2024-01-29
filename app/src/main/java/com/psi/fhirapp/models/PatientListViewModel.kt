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
import com.google.android.fhir.search.count
import com.google.android.fhir.search.search
import com.google.android.fhir.sync.Sync
import com.google.android.fhir.sync.SyncJobStatus
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.data.PatientListItem
import com.psi.fhirapp.sync.PatientPeriodicSyncWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import org.hl7.fhir.r4.model.Patient
import java.time.LocalDate
import java.time.LocalDateTime
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
    val liveSearchedPatients = MutableLiveData<List<PatientListItem>>()
    val livePatientCount = MutableLiveData<Long>()

    init {
        searchPatientsByName()
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
            Sync.oneTimeSync<PatientPeriodicSyncWorker>(getApplication())
                .shareIn(this, SharingStarted.Eagerly, 10)
                // Emits a value to this shared flow, suspending/stopping on buffer overflow.
                .collect { _pollState.emit(it) }
        }
    }

    fun searchPatientsByName(nameQuery: String = "") {
        updatePatientList({ retrievePatientsByName(nameQuery) }, { getPatientCount(nameQuery) })
    }

    /**
     * [updatePatientList] calls the search and count lambda and updates the live data values
     * accordingly. It is initially called when this [ViewModel] is created. Later its called by the
     * client every time search query changes or data-sync is completed.
     */
    private fun updatePatientList (
        getList: suspend () -> List<PatientListItem>,
        getCount: suspend() -> Long
    ) {
        viewModelScope.launch {
            liveSearchedPatients.value = getList()
            livePatientCount.value = getCount()
        }
    }


    /**
     * Keyword "suspend" is the same "async/await" keyword in JS Async
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun retrievePatientsByName(nameQuery: String = ""): List<PatientListItem> {
        val patients: MutableList<PatientListItem> = mutableListOf()

        var searchResult = fhirEngine.search<Patient> {
            if (nameQuery.isNotEmpty()) {
                filter(
                    Patient.NAME,
                    {
                        modifier = StringFilterModifier.CONTAINS
                        value = nameQuery
                    },
                )
            }
            sort(Patient.GIVEN, Order.ASCENDING)
            count = 20
            from = 0
        }
        .mapIndexed { index, fhirPatient -> fhirPatient.resource.toPatientItem(index + 1) }
        .let { patients.addAll(it) }

        return patients
    }

    private suspend fun getPatientCount(nameQuery: String = ""): Long {
        return fhirEngine.count<Patient> {
            if(nameQuery.isNotEmpty())
            {
                filter(
                    Patient.NAME,
                    {
                        modifier = StringFilterModifier.CONTAINS
                        value = nameQuery
                    }
                )
            }
        }
    }

}


/**
 * Below is an internal function, it will be visible everywhere in the same module.
 **/
@RequiresApi(Build.VERSION_CODES.O)
internal fun Patient.toPatientItem(position: Int): PatientListItem {
    Log.d("Patient.toPatientItem", "$idElement" )

    // Show nothing if no values available for gender and date of birth.
    val patientId = if (hasIdElement()) idElement.idPart else ""
    val name = if (hasName()) name[0].nameAsSingleString else ""
    val gender = if (hasGenderElement()) genderElement.valueAsString else ""
    val dob =
        if (hasBirthDateElement()) {
//            LocalDate.parse(birthDateElement.valueAsString, DateTimeFormatter.ISO_DATE)
//            OffsetDateTime.parse(
//                birthDateElement.valueAsString,
//                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX")
//            )
            LocalDate.parse(birthDateElement.valueAsString.substring(0,10), DateTimeFormatter.ofPattern("yyyy-MM-dd"))

        } else {
            null
        }
    val phone = if (hasTelecom()) telecom[0].value else ""
    val city = if (hasAddress()) address[0].city else ""
    val country = if (hasAddress()) address[0].country else ""
    val isActive = active
    val html: String = if (hasText()) text.div.valueAsString else ""

    return PatientListItem(
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