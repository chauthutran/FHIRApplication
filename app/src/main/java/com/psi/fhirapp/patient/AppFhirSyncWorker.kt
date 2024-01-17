package com.psi.fhirapp.patient

import android.content.Context
import androidx.work.WorkerParameters
import com.google.android.fhir.sync.AcceptLocalConflictResolver
import com.google.android.fhir.sync.FhirSyncWorker
import com.psi.fhirapp.FhirApplication

/**
 * defines how the app will sync with the remote FHIR server using a background worker.
 * **/
class AppFhirSyncWorker(appContext: Context, workerParams: WorkerParameters) :
    FhirSyncWorker(appContext, workerParams) {

    override fun getDownloadWorkManager() = DownloadWorkManagerImpl()

    override fun getConflictResolver() = AcceptLocalConflictResolver

    override fun getFhirEngine() = FhirApplication.fhirEngine(applicationContext)
}