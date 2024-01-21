package com.psi.fhirapp

import android.app.Application
import android.os.Build
import android.text.format.DateFormat
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import com.google.android.fhir.sync.PeriodicSyncConfiguration
import com.google.android.fhir.sync.RepeatInterval
import com.google.android.fhir.sync.Sync
import com.google.android.fhir.sync.SyncJobStatus
import com.psi.fhirapp.sync.FhirPeriodicSyncWorker
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit


/** View model for [MainActivity]. */
@OptIn(InternalCoroutinesApi::class)
class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val _lastSyncTimestampLiveData = MutableLiveData<String>()
    val lastSyncTimestampLiveData: LiveData<String>
        get() = _lastSyncTimestampLiveData

    private val _pollState = MutableSharedFlow<SyncJobStatus>()
    val pollState: Flow<SyncJobStatus>
        get() = _pollState

    init {
        // oneTimeSync
        viewModelScope.launch {
            Sync.periodicSync<FhirPeriodicSyncWorker>(
                application.applicationContext,
                periodicSyncConfiguration =
                PeriodicSyncConfiguration(
                    syncConstraints = Constraints.Builder().build(),
                    repeat = RepeatInterval(interval = 15, timeUnit = TimeUnit.MINUTES),
                ),
            )
                .shareIn(this, SharingStarted.Eagerly, 10)
                .collect { _pollState.emit(it) }
        }
    }

    fun triggerOneTimeSync() {
        viewModelScope.launch {
            Sync.oneTimeSync<FhirPeriodicSyncWorker>(getApplication())
                .shareIn(this, SharingStarted.Eagerly, 10)
                .collect { _pollState.emit(it) }
        }
    }

    /** Emits last sync time. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun updateLastSyncTimestamp() {
        val formatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern(
                if (DateFormat.is24HourFormat(getApplication())) formatString24 else formatString12,
            )
        _lastSyncTimestampLiveData.value =
            Sync.getLastSyncTimestamp(getApplication())?.toLocalDateTime()?.format(formatter) ?: ""
    }

    companion object {
        private const val formatString24 = "yyyy-MM-dd HH:mm:ss"
        private const val formatString12 = "yyyy-MM-dd hh:mm:ss a"
    }
}
