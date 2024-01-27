package com.psi.fhirapp.sync

import com.google.android.fhir.sync.DownloadWorkManager
import com.google.android.fhir.sync.download.DownloadRequest
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.util.LinkedList


/**
 * Define how the application fetches the next resource from the "Patient" list to download
 **/
class DownloadPatientWorkManagerImpl : DownloadWorkManager {
    private val urls = LinkedList(listOf("Patient"))

    override suspend fun getNextRequest(): DownloadRequest? {
        val url = urls.poll() ?: return null
        return DownloadRequest.of(url)
    }

    override suspend fun getSummaryRequestUrls() = mapOf<ResourceType, String>()

    override suspend fun processResponse(response: Resource): Collection<Resource> {
        var bundleCollection: Collection<Resource> = mutableListOf()
        if (response is Bundle && response.type == Bundle.BundleType.SEARCHSET) {
            bundleCollection = response.entry.map { it.resource }
        }
        return bundleCollection
    }
}