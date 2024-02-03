package com.psi.fhirapp.viewmodels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.fhir.get
import com.google.android.fhir.search.search
import com.google.android.fhir.testing.jsonParser
import com.psi.fhirapp.FhirApplication
import com.psi.fhirapp.data.CarePlanWorkflowExecutionRequest
import com.psi.fhirapp.workflow.CarePlanWorkflowExecutionStatus
import com.psi.fhirapp.workflow.ConfigurationManager
import com.psi.fhirapp.workflow.RequestConfiguration
import java.time.Instant
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.MedicationRequest
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Task

class CarePlanWorkflowExecutionViewModel(application: Application): AndroidViewModel(application) {

    private val fhirEngine = FhirApplication.fhirEngine(application.applicationContext)
    private val carePlanManager = FhirApplication.carePlanManager(application.applicationContext)
    private val requestManager = FhirApplication.requestManager(getApplication<Application>().applicationContext)

    private lateinit var activeRequestConfiguration: List<RequestConfiguration>
    lateinit var currentPlanDefinitionId: String
    lateinit var currentIg: String
    var currentStructureMapId: String = ""
    var currentTargetResourceType: String = ""
    lateinit var currentQuestionnaireId: String

    /**
     * Shared flow of [CarePlanWorkflowExecutionRequest]. For each collected patient the execution shall
     * run in blocking mode as asynchronous execution is resource exhaustive. extraBufferCapacity > 0
     * so that pending executions are collected properly.
     */
    // Having a map of patients is better ? Check TVPF.updateWorkflowExecutionBar
    // replay to 5 is not good since for multiple patients will be in queue. map makes lot more sense
    val patientFlowForCareWorkflowExecution = MutableSharedFlow<CarePlanWorkflowExecutionRequest>(replay = 5)
    private var totalPlanDefinitionsToApply = AtomicInteger(0)
    private var totalPlanDefinitionsApplied = AtomicInteger(0)

    init {
        /**
         * [patientFlowForCareWorkflowExecution] collects each patient in a coroutine and executes
         * workflow blocking. This can be invoked when there is an operation on a Patient or some Task
         * is updated.
         */
        viewModelScope.launch(Dispatchers.IO) {
            patientFlowForCareWorkflowExecution.collect { carePlanWorkflowExecutionRequest ->
                if (carePlanWorkflowExecutionRequest.carePlanWorkflowExecutionStatus
                            is CarePlanWorkflowExecutionStatus.Finished
                )
                    return@collect
                /**
                 * runBlocking because we want to run care workflows sequentially to avoid resource
                 * exhaustion.
                 */
                runBlocking {
                    if (currentPlanDefinitionId != "") {
                        if (currentPlanDefinitionId.contains("CreateImmunizationRecord")) {} else {
                            println("About to apply $currentPlanDefinitionId")
                            carePlanManager.applyPlanDefinitionOnPatient(
                                currentPlanDefinitionId,
                                carePlanWorkflowExecutionRequest.patient,
                                getActiveRequestConfiguration()
                            )
                        }
                    } else {
                        // do nothing
                    }
                }
                patientFlowForCareWorkflowExecution.emit(
                    CarePlanWorkflowExecutionRequest(
                        carePlanWorkflowExecutionRequest.patient,
                        CarePlanWorkflowExecutionStatus.Finished(
                            totalPlanDefinitionsApplied.incrementAndGet(),
                            totalPlanDefinitionsToApply.get()
                        )
                    )
                )
            }
        }
    }
    fun executeCareWorkflowForPatient(patient: Patient) {
        viewModelScope.launch {
            patientFlowForCareWorkflowExecution.emit(
                CarePlanWorkflowExecutionRequest(
                    patient,
                    CarePlanWorkflowExecutionStatus.Started(totalPlanDefinitionsToApply.incrementAndGet())
                )
            )
        }
    }
    /**
     * Updating task statuses should be done in scope of [CareWorkflowExecutionViewModel] under
     * activity context. Also re-triggering of [PlanDefinition].apply is done here by fetching the
     * [Patient] from FhirEngine. Update: Updating tasks could also happen in background!
     */
    fun updateTaskAndCarePlanStatus(
        taskLogicalId: String,
        taskStatus: Task.TaskStatus,
        encounterReferences: List<Reference>,
        updateCarePlan: Boolean,
    ) {
        viewModelScope.launch {
            val taskSearch =
                fhirEngine.search<Task> { filter(Task.RES_ID, { value = of(taskLogicalId) }) }

            val medicationRequestSearch =
                fhirEngine.search<MedicationRequest> {
                    filter(MedicationRequest.RES_ID, { value = of(taskLogicalId) })
                }

            val patient: Patient
            if (taskSearch.isNotEmpty()) {
                val task = taskSearch.first().resource
                patient = fhirEngine.get(task.`for`.reference.substring("Patient/".length))
                executeCareWorkflowForPatient(patient)

                task.status = Task.TaskStatus.COMPLETED
                task.lastModified = Date.from(Instant.now())
                task.meta.lastUpdated = Date.from(Instant.now())
                fhirEngine.update(task)
            } else if (medicationRequestSearch.isNotEmpty()) {
                val medicationRequest = medicationRequestSearch.first().resource
                patient = fhirEngine.get(medicationRequest.subject.reference.substring("Patient/".length))
                executeCareWorkflowForPatient(patient)
            }
        }
    }

    fun setActiveRequestConfiguration(planDefinitionId: String) {
        activeRequestConfiguration =
            ConfigurationManager.careConfiguration
                ?.supportedImplementationGuides
                ?.firstOrNull { it.implementationGuideConfig.entryPoint.contains(planDefinitionId) }
                ?.implementationGuideConfig
                ?.requestConfigurations!!
    }

    fun getActiveRequestConfiguration(): List<RequestConfiguration> {
        return activeRequestConfiguration
    }

    suspend fun getActivePatientRegistrationQuestionnaire(): String {
        currentQuestionnaireId =
            ConfigurationManager.careConfiguration
                ?.supportedImplementationGuides
                ?.firstOrNull { it.implementationGuideConfig.entryPoint.contains(currentIg) }
                ?.implementationGuideConfig
                ?.patientRegistrationQuestionnaire!!

        val questionnaire = fhirEngine.get<Questionnaire>(IdType(currentQuestionnaireId).idPart)

        return jsonParser.encodeResourceToString(questionnaire)
    }

    fun setPlanDefinitionId(event: String) {
        for (implementationGuide in
        ConfigurationManager.careConfiguration?.supportedImplementationGuides!!) {
            val triggers = implementationGuide.implementationGuideConfig.triggers
            for (trigger in triggers) if (trigger.event == event)
                currentPlanDefinitionId = trigger.planDefinition
        }
    }

}