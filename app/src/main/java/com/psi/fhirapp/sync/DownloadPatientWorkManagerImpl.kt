package com.psi.fhirapp.sync

import android.net.Uri
import com.google.android.fhir.sync.DownloadWorkManager
import com.google.android.fhir.sync.SyncDataParams
import com.google.android.fhir.sync.download.DownloadRequest
import org.hl7.fhir.exceptions.FHIRException
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CarePlan
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.OperationOutcome
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import java.util.LinkedList


/**
 * Define how the application fetches the next resource from the "Patient" list to download
 **/
class DownloadPatientWorkManagerImpl() : DownloadWorkManager {

    companion object {
        private const val resourceFetchByIdRequestSize = 100
        private val resourceTypeList = ResourceType.values().map { it.name }
        private val resourceReferencesDownloadOrderByTypeSequence =
            listOf(
                // Server should filter all the CarePlan that belong to the patients the Health Professional
                // is assigned to
                ResourceType.CarePlan,
                ResourceType.Observation
            )
    }

    // The assumption with the following URLs is that the server has the capability to identify the
    // correct set of Patients that are assigned to the Health Professional who is using this
    // application.
    // The expectation is that the server is able to filter the requested resources according to the
    // role and assignments of a Health Professional.
    // The server could gather the identity of the Health Professional from the Authorisation tokens.
    private val resourceReferences = mutableMapOf<ResourceType, MutableMap<String, Set<String>>>()
    // These initial set of URLs can be a configuration that is downloadable from the server
    private val urls =
        LinkedList(
            listOf(
                // Server should filter all the patients the Health Professional is assigned to
                "Patient",
                // Server should filter all the PlanDefinition that need to be executed by this Health
                // Professional
                "PlanDefinition",
                // Server should fetch the PractitionerRole corresponding to the health Professional
                "PractitionerRole"
            )
        )


    override suspend fun getNextRequest(): DownloadRequest? {
        var url = urls.poll()
        return if(url == null) {
            constructNextRequestFromResourceReferences()
        }else {
            val resourceTypeToDownload =
                ResourceType.fromCode(url.findAnyOf(resourceTypeList, ignoreCase = true)!!.second)
            DownloadRequest.of(url)
        }
    }


    private fun constructNextRequestFromResourceReferences(): DownloadRequest? {
        for (resourceType in resourceReferencesDownloadOrderByTypeSequence) {
            if (resourceReferences.getOrDefault(resourceType, emptyMap()).isNotEmpty()) {
                val resourceSearchValues = resourceReferences[resourceType]!!
                resourceSearchValues.entries.forEach { (searchParameter, searchIds) ->
                    if (searchIds.isNotEmpty()) {
                        val toBeRequestedIds = searchIds.take(resourceFetchByIdRequestSize).toSet()
                        val leftOverIds = searchIds - toBeRequestedIds
                        resourceReferences[resourceType]!![searchParameter] = leftOverIds
                        val request =
                            createUrlSearchRequestFromIds(resourceType, searchParameter, toBeRequestedIds)
                        if (request != null) {
                            return request
                        }
                    }
                }
            }
        }
        return null
    }

    private fun createUrlSearchRequestFromIds(
        resourceType: ResourceType,
        searchField: String,
        resourceIds: Set<String>
    ): DownloadRequest? {
        return if (resourceIds.isNotEmpty()) {
            DownloadRequest.of("${resourceType.name}?$searchField=${resourceIds.joinToString(",")}")
        } else {
            null
        }
    }

    override suspend fun processResponse(response: Resource): Collection<Resource> {
        // As per FHIR documentation :
        // If the search fails (cannot be executed, not that there are no matches), the
        // return value SHALL be a status code 4xx or 5xx with an OperationOutcome.
        // See https://www.hl7.org/fhir/http.html#search for more details.
        if (response is OperationOutcome) {
            throw FHIRException(response.issueFirstRep.diagnostics)
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
            for (item in response.entry) {
                bundleCollection += processResourceForExtraction(item.resource)
            }
        }
        println("Bundle size is " + bundleCollection.size)
        return bundleCollection

//        // As per FHIR documentation :
//        // If the search fails (cannot be executed, not that there are no matches), the
//        // return value SHALL be a status code 4xx or 5xx with an OperationOutcome.
//        // See https://www.hl7.org/fhir/http.html#search for more details.
//        if (response is OperationOutcome) {
//            throw FHIRException(response.issueFirstRep.diagnostics)
//        }
//
//        // If the resource returned is a List containing Patients, extract Patient references and fetch
//        // all resources related to the patient using the $everything operation.
//        if (response is ListResource) {
//            for (entry in response.entry) {
//                val reference = Reference(entry.item.reference)
//                if (reference.referenceElement.resourceType.equals("Patient")) {
//                    val patientUrl = "${entry.item.reference}/\$everything"
//                    urls.add(patientUrl)
//                }
//            }
//        }
//
//        // Finally, extract the downloaded resources from the bundle.
//        var bundleCollection: Collection<Resource> = mutableListOf()
//        if (response is Bundle && response.type == Bundle.BundleType.SEARCHSET) {
//
//            bundleCollection = response.entry.map { it.resource }
//
//            for (entry in bundleCollection) {
//                val type = entry.resourceType.name
//                if (type == "Patient") {
//                    urls.add("Observation?patient=${entry.idElement.idPart}")
//                }
//            }
//        }
//        return bundleCollection
    }


    private suspend fun processResourceForExtraction(resource: Resource): Collection<Resource> {
        when (resource) {
            is CarePlan -> return extractCarePlanDependentResources(resource)
            is Encounter -> return addEncounterRelatedResources(resource)
        }
        return emptyList()
    }

    override suspend fun getSummaryRequestUrls(): Map<ResourceType, String> {
        return urls.associate {
            ResourceType.fromCode(it.substringBefore("?")) to
                    it.plus("&${SyncDataParams.SUMMARY_KEY}=${SyncDataParams.SUMMARY_COUNT_VALUE}")
        }
    }


    private fun extractCarePlanDependentResources(carePlan: CarePlan): Collection<Resource> {
        // get all the tasks that have been carried out/ need to be carried out as a part of this
        // CarePlan
        val taskIds = carePlan.activity.map { getResourceIdFromReference(it.reference) }.toSet()
        addResourceIdsToSearch(ResourceType.Task, "_id", taskIds)
        // get all the encounters that were created as a result of carrying out tasks of this CarePlan
        val encounterIds =
            carePlan.activity
                .flatMap { it.outcomeReference.map { or -> getResourceIdFromReference(or) } }
                .toSet()
        addResourceIdsToSearch(ResourceType.Encounter, "_id", encounterIds)
        return emptyList()
    }

    private fun addEncounterRelatedResources(encounter: Encounter): Collection<Resource> {
        // get all related observations and conditions for these encounters
        addResourceIdsToSearch(ResourceType.Observation, "encounter", setOf(encounter.idElement.idPart))
        addResourceIdsToSearch(ResourceType.Condition, "encounter", setOf(encounter.idElement.idPart))
        return emptyList()
    }


    private fun getResourceIdFromReference(reference: Reference): String {
        val referenceElements = reference.reference.split("/")
        return referenceElements[1]
    }

    private fun addResourceIdsToSearch(
        resourceType: ResourceType,
        searchParam: String,
        resourceIds: Set<String>
    ) {
        if (resourceIds.isEmpty()) {
            return
        }
        val existingSearchParams =
            resourceReferences.getOrDefault(resourceType, emptyMap()).toMutableMap()
        resourceReferences[resourceType] = existingSearchParams
        val existingSearchIds =
            existingSearchParams.getOrDefault(searchParam, emptySet()).toMutableSet()
        existingSearchIds.addAll(resourceIds)
        resourceReferences[resourceType]!![searchParam] = existingSearchIds
    }


    /**
     * Affixes the last updated timestamp to the request URL.
     *
     * If the request URL includes the `$everything` parameter, the last updated timestamp will be
     * attached using the `_since` parameter. Otherwise, the last updated timestamp will be attached
     * using the `_lastUpdated` parameter.
     */
//    private fun affixLastUpdatedTimestamp(url: String, lastUpdated: String): String {
//        var downloadUrl = url
//
//        // Affix lastUpdate to a $everything query using _since as per:
//        // https://hl7.org/fhir/operation-patient-everything.html
//        if (downloadUrl.contains("\$everything")) {
//            downloadUrl = "$downloadUrl?_since=$lastUpdated"
//        }
//
//        // Affix lastUpdate to non-$everything queries as per:
//        // https://hl7.org/fhir/operation-patient-everything.html
//        if (!downloadUrl.contains("\$everything")) {
//            downloadUrl =
//                Uri.parse(downloadUrl).query?.let { "$it&_lastUpdated=gt$lastUpdated" }
//                    ?: "$downloadUrl?_lastUpdated=gt$lastUpdated"
//        }
//
//        // Do not modify any URL set by a server that specifies the token of the page to return.
//        if (downloadUrl.contains("&page_token")) {
//            downloadUrl = url
//        }
//
//        return downloadUrl
//    }
}
