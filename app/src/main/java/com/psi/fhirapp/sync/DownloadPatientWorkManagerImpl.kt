package com.psi.fhirapp.sync

import com.google.android.fhir.sync.DownloadWorkManager
import com.google.android.fhir.sync.SyncDataParams
import com.google.android.fhir.sync.download.DownloadRequest
import org.hl7.fhir.exceptions.FHIRException
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.ListResource
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.util.LinkedList


/**
 * Define how the application fetches the next resource from the "Patient" list to download
 **/
class DownloadPatientWorkManagerImpl : DownloadWorkManager {
    private val resourceTypeList = ResourceType.values().map { it.name }
    private val urls = LinkedList(listOf("Patient?address-city=NAIROBI"))

    override suspend fun getNextRequest(): DownloadRequest? {
        var url = urls.poll() ?: return null

        var resourceTypeToDownload = ResourceType.fromCode(url.findAnyOf(resourceTypeList, ignoreCase = true)!!.second)
            println("=== resourceTypeToDownload: ${resourceTypeToDownload}")
//        dataStore.getLasUpdateTimestamp(resourceTypeToDownload)?.let {
//            url = affixLastUpdatedTimestamp(url)
//        }
println("===url : ${url}")
        return DownloadRequest.of(url)
    }

//    override suspend fun getSummaryRequestUrls() = mapOf<ResourceType, String>()


    override suspend fun getSummaryRequestUrls(): Map<ResourceType, String> {
        return urls.associate {
            ResourceType.fromCode(it.substringBefore("?")) to
                    it.plus("&${SyncDataParams.SUMMARY_KEY}=${SyncDataParams.SUMMARY_COUNT_VALUE}")
        }
    }

//    override suspend fun processResponse(response: Resource): Collection<Resource> {
//        var bundleCollection: Collection<Resource> = mutableListOf()
//        if (response is Bundle && response.type == Bundle.BundleType.SEARCHSET) {
//            bundleCollection = response.entry.map { it.resource }
//        }
//        return bundleCollection
//    }

    override suspend fun processResponse(response: Resource): Collection<Resource> {
        // As per FHIR documentation :
        // If the search fails (cannot be executed, not that there are no matches), the
        // return value SHALL be a status code 4xx or 5xx with an OperationOutcome.
        // See https://www.hl7.org/fhir/http.html#search for more details.
        if (response is OperationOutcome) {
            throw FHIRException(response.issueFirstRep.diagnostics)
        }

        // If the resource returned is a List containing Patients, extract Patient references and fetch
        // all resources related to the patient using the $everything operation.
        if (response is ListResource) {
            for (entry in response.entry) {
                val reference = Reference(entry.item.reference)
                if (reference.referenceElement.resourceType.equals("Patient")) {
                    val patientUrl = "${entry.item.reference}/\$everything"
                    urls.add(patientUrl)
                }
            }
        }

        // If the resource returned is a Bundle, check to see if there is a "next" relation referenced
        // in the Bundle.link component, if so, append the URL referenced to list of URLs to download.
        if (response is Bundle) {
            val nextUrl = response.link.firstOrNull { component -> component.relation == "next" }?.url
            if (nextUrl != null) {
                urls.add(nextUrl)
            }
        }

        // Finally, extract the downloaded resources from the bundle.
        var bundleCollection: Collection<Resource> = mutableListOf()
        if (response is Bundle && response.type == Bundle.BundleType.SEARCHSET) {
            bundleCollection =
                response.entry
                    .map { it.resource }
        }
        return bundleCollection
    }
}


private fun affixLastUpdatedTimestamp(url: String): String {
    var downloadUrl = url

    // Affix lastUpdate to a $everything query using _since as per:
    // https://hl7.org/fhir/operation-patient-everything.html
    if (downloadUrl.contains("\$everything")) {
        downloadUrl = "$downloadUrl"
    }

    // Affix lastUpdate to non-$everything queries as per:
    // https://hl7.org/fhir/operation-patient-everything.html
    if (!downloadUrl.contains("\$everything")) {
        downloadUrl = "$downloadUrl"
    }

    // Do not modify any URL set by a server that specifies the token of the page to return.
    if (downloadUrl.contains("&page_token")) {
        downloadUrl = url
    }

    return downloadUrl
}
