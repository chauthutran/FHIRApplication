package com.psi.fhirapp.workflow

import android.content.Context
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.knowledge.FhirNpmPackage
import com.google.android.fhir.knowledge.KnowledgeManager
import com.google.android.fhir.search.search
import com.google.android.fhir.workflow.FhirOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.CanonicalType
import org.hl7.fhir.r4.model.CarePlan
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.RequestGroup
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ServiceRequest
import org.hl7.fhir.r4.model.Task
import java.io.File
import java.io.FileOutputStream

class CarePlanManager(
    private var fhirEngine: FhirEngine,
    fhirContext: FhirContext,
    private val context: Context
){
    private var knowledgeManager = KnowledgeManager.create(context, inMemory = true)
    private var fhirOperator =
        FhirOperator.Builder(context.applicationContext)
            .fhirContext(fhirContext)
            .fhirEngine(fhirEngine)
            .knowledgeManager(knowledgeManager)
            .build()

    private val jsonParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()

    private var requestManager: RequestManager =
        RequestManager(fhirEngine, fhirContext, TestRequestHandler())


    suspend fun fetchKnowledgeResources(path: String) {
        val rootDirectory = File(context.filesDir, path)
        if (rootDirectory.exists()) {
            initializeKnowledgeManager(rootDirectory)
//            return
        }

        rootDirectory.mkdirs()

        val fileList = context.assets.list(path)
        if (fileList != null) {
            for (filename in fileList) {
                if (filename.contains(".json")) {
                    val contents = readFileFromAssets(context, "$path/$filename")
                    try {
                        val resource = jsonParser.parseResource(contents)
                        if (resource is Resource) {
                            fhirEngine.create(resource)
 println("------  Saved 1: ${resource.resourceType}")

                            withContext(Dispatchers.IO) {
                                val fis = FileOutputStream(File(context.filesDir, "$path/$filename"))
                                fis.write(contents.toByteArray())
                                println("------  Saved 2: ${context.filesDir}/$path/$filename")
                            }
                        }
                    } catch (exception: Exception) {
                        // do nothing
                    }
                }
            }
        }
        initializeKnowledgeManager(rootDirectory)
    }


    private suspend fun initializeKnowledgeManager(rootDirectory: File) {
//        knowledgeManager.install(
//            FhirNpmPackage(
//                "who.fhir.immunization",
//                "1.0.0",
//                "https://github.com/WorldHealthOrganization/smart-immunizations",
//            ),
//            rootDirectory,
//        )

        knowledgeManager.install(
            FhirNpmPackage(
                "com.psi.fhir",
                "1.0.0",
                "https://github.com/chauthutran/FHIRApplication",
            ),
            rootDirectory,
        )
        println("------ KM has been initialized")
    }

    private fun readFileFromAssets(context: Context, filename: String): String {
        return context.assets.open(filename).bufferedReader().use { it.readText() }
    }

    suspend fun applyPlanDefinitionOnPatient(
        planDefinitionUri: String,
        patient: Patient,
        requestConfiguration: List<RequestConfiguration>,
    ) {
        val patientId = IdType(patient.id).idPart

        println("------ PlanDefinition: ${CanonicalType(planDefinitionUri)}")
        val carePlanProposal =
            fhirOperator.generateCarePlan(
                planDefinition = CanonicalType(planDefinitionUri),
                subject = "Patient/$patientId"
            ) as CarePlan
        println("------ " + jsonParser.encodeResourceToString(carePlanProposal))

        // Fetch existing CarePlan of record for the Patient or create a new one if it does not exist
        val carePlanOfRecord = getCarePlanOfRecordForPatient(patient)

        // Accept the proposed (transient) CarePlan by default and add tasks to the CarePlan of record
        val resourceList = acceptCarePlan(patientId, carePlanProposal, requestConfiguration)

        addRequestResourcesToCarePlanOfRecord(carePlanOfRecord, resourceList)
    }


    /** Fetch the [CarePlan] of record for a given [Patient], if it exists, otherwise create it */
    private suspend fun getCarePlanOfRecordForPatient(patient: Patient): CarePlan {
        val patientId = IdType(patient.id).idPart
        val existingCarePlans = fhirEngine.search("CarePlan?subject=$patientId")

        val carePlanOfRecord = CarePlan()
        return if (existingCarePlans.isEmpty()) {
            carePlanOfRecord.status = CarePlan.CarePlanStatus.ACTIVE
            carePlanOfRecord.subject = Reference(patient)
            carePlanOfRecord.description = "CarePlan of Record"
            fhirEngine.create(carePlanOfRecord)
            carePlanOfRecord
        } else {
            existingCarePlans.first().resource as CarePlan
        }
    }

    private suspend fun acceptCarePlan(
        patientId: String,
        proposedCarePlan: CarePlan,
        requestConfiguration: List<RequestConfiguration>,
    ): List<Resource> {
        // modify this and use:
        val resourceList: MutableList<Resource> = mutableListOf()
        for (request in proposedCarePlan.contained) {
            if (request is RequestGroup) {
                resourceList.addAll(requestManager.createRequestFromRequestGroup(request))
            }
        }

        requestManager.evaluateNextStage(patientId, resourceList, requestConfiguration)
        return resourceList
    }

    /** Link the request resources created for the [Patient] back to the [CarePlan] of record */
    private suspend fun addRequestResourcesToCarePlanOfRecord(
        carePlan: CarePlan,
        requestResourceList: List<Resource>,
    ) {
        for (resource in requestResourceList) {
            when (resource) {
                is Task ->
                    carePlan.addActivity().setReference(Reference(resource)).detail.status =
                        mapTaskStatusToCarePlanStatus(resource)
                is ServiceRequest ->
                    carePlan.addActivity().setReference(Reference(resource)).detail.status =
                        mapServiceRequestStatusToCarePlanStatus(resource)
                is MedicationRequest ->
                    carePlan.addActivity().setReference(Reference(resource)).detail.status =
                        mapMedicationRequestStatusToCarePlanStatus(resource)
                else -> TODO("Not a supported request resource")
            }
        }
        fhirEngine.update(carePlan)
    }


    /** Map [Task] status to [CarePlan] status */
    private fun mapTaskStatusToCarePlanStatus(resource: Task): CarePlan.CarePlanActivityStatus {
        // Refer: http://hl7.org/fhir/R4/valueset-care-plan-activity-status.html for some mapping
        // guidelines
        return when (resource.status) {
            Task.TaskStatus.ACCEPTED -> CarePlan.CarePlanActivityStatus.SCHEDULED
            Task.TaskStatus.DRAFT -> CarePlan.CarePlanActivityStatus.NOTSTARTED
            Task.TaskStatus.REQUESTED -> CarePlan.CarePlanActivityStatus.NOTSTARTED
            Task.TaskStatus.RECEIVED -> CarePlan.CarePlanActivityStatus.NOTSTARTED
            Task.TaskStatus.REJECTED -> CarePlan.CarePlanActivityStatus.STOPPED
            Task.TaskStatus.READY -> CarePlan.CarePlanActivityStatus.NOTSTARTED
            Task.TaskStatus.CANCELLED -> CarePlan.CarePlanActivityStatus.CANCELLED
            Task.TaskStatus.INPROGRESS -> CarePlan.CarePlanActivityStatus.INPROGRESS
            Task.TaskStatus.ONHOLD -> CarePlan.CarePlanActivityStatus.ONHOLD
            Task.TaskStatus.FAILED -> CarePlan.CarePlanActivityStatus.STOPPED
            Task.TaskStatus.COMPLETED -> CarePlan.CarePlanActivityStatus.COMPLETED
            Task.TaskStatus.ENTEREDINERROR -> CarePlan.CarePlanActivityStatus.ENTEREDINERROR
            Task.TaskStatus.NULL -> CarePlan.CarePlanActivityStatus.NULL
            else -> CarePlan.CarePlanActivityStatus.NULL
        }
    }

    private fun mapMedicationRequestStatusToCarePlanStatus(
        resource: MedicationRequest
    ): CarePlan.CarePlanActivityStatus {
        // Refer: http://hl7.org/fhir/R4/valueset-care-plan-activity-status.html for some mapping
        // guidelines
        return when (resource.status) {
            MedicationRequest.MedicationRequestStatus.ACTIVE -> CarePlan.CarePlanActivityStatus.INPROGRESS
            MedicationRequest.MedicationRequestStatus.DRAFT -> CarePlan.CarePlanActivityStatus.NOTSTARTED
            else -> CarePlan.CarePlanActivityStatus.fromCode(resource.status.toCode())
        }
    }

    private fun mapServiceRequestStatusToCarePlanStatus(
        resource: ServiceRequest
    ): CarePlan.CarePlanActivityStatus {
        // Refer: http://hl7.org/fhir/R4/valueset-care-plan-activity-status.html for some mapping
        // guidelines
        return when (resource.status) {
            ServiceRequest.ServiceRequestStatus.ACTIVE -> CarePlan.CarePlanActivityStatus.INPROGRESS
            ServiceRequest.ServiceRequestStatus.REVOKED -> CarePlan.CarePlanActivityStatus.CANCELLED
            ServiceRequest.ServiceRequestStatus.DRAFT -> CarePlan.CarePlanActivityStatus.NOTSTARTED
            else -> CarePlan.CarePlanActivityStatus.fromCode(resource.status.toCode())
        }
    }

}