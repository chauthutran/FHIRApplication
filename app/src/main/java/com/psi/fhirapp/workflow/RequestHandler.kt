package com.psi.fhirapp.workflow


import org.hl7.fhir.r4.model.RequestGroup
import org.hl7.fhir.r4.model.RequestGroup.RequestStatus
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.Task

interface RequestHandler {
    fun acceptProposedRequest(request: Resource): Boolean {
        return true
    }
}

class RequestUtils {

    companion object {

        fun isValidRequest(resourceType: ResourceType): Boolean {
            return when (resourceType) {
                ResourceType.Task,
                ResourceType.MedicationRequest,
                ResourceType.ServiceRequest,
                ResourceType.CommunicationRequest -> true
                else -> false
            }
        }

        // valid status change:
        // do by resource and then valid next states for present state
        fun mapTaskStatusToRequestStatus(taskStatus: Task.TaskStatus): RequestGroup.RequestStatus {
            val requestStatus =
                when (taskStatus) {
                    Task.TaskStatus.DRAFT -> RequestGroup.RequestStatus.DRAFT
                    Task.TaskStatus.ACCEPTED -> RequestGroup.RequestStatus.DRAFT
                    Task.TaskStatus.RECEIVED -> RequestGroup.RequestStatus.DRAFT
                    Task.TaskStatus.INPROGRESS -> RequestGroup.RequestStatus.ACTIVE
                    Task.TaskStatus.ONHOLD -> RequestGroup.RequestStatus.ONHOLD
                    Task.TaskStatus.REJECTED -> RequestGroup.RequestStatus.REVOKED
                    Task.TaskStatus.CANCELLED -> RequestGroup.RequestStatus.REVOKED
                    Task.TaskStatus.COMPLETED -> RequestGroup.RequestStatus.COMPLETED
                    Task.TaskStatus.ENTEREDINERROR -> RequestGroup.RequestStatus.ENTEREDINERROR
                    Task.TaskStatus.NULL -> RequestGroup.RequestStatus.NULL
                    else -> RequestGroup.RequestStatus.NULL
                }
            return requestStatus
        }

        fun mapRequestStatusToTaskStatus(requestStatus: RequestGroup.RequestStatus): Task.TaskStatus {
            val taskStatus =
                when (requestStatus) {
                    RequestGroup.RequestStatus.DRAFT -> Task.TaskStatus.DRAFT
                    RequestGroup.RequestStatus.ACTIVE -> Task.TaskStatus.INPROGRESS
                    RequestGroup.RequestStatus.ONHOLD -> Task.TaskStatus.ONHOLD
                    RequestGroup.RequestStatus.REVOKED -> Task.TaskStatus.REJECTED
                    RequestGroup.RequestStatus.COMPLETED -> Task.TaskStatus.COMPLETED
                    RequestGroup.RequestStatus.ENTEREDINERROR -> Task.TaskStatus.ENTEREDINERROR
                    RequestGroup.RequestStatus.UNKNOWN -> Task.TaskStatus.NULL
                    RequestGroup.RequestStatus.NULL -> Task.TaskStatus.NULL
                    else -> Task.TaskStatus.NULL
                }
            return taskStatus
        }

        fun mapTaskIntentToRequestIntent(taskIntent: Task.TaskIntent): RequestGroup.RequestIntent {
            val requestIntent =
                when (taskIntent) {
                    Task.TaskIntent.PLAN -> RequestGroup.RequestIntent.PLAN
                    Task.TaskIntent.PROPOSAL -> RequestGroup.RequestIntent.PROPOSAL
                    Task.TaskIntent.ORDER -> RequestGroup.RequestIntent.ORDER
                    else -> RequestGroup.RequestIntent.NULL
                }
            return requestIntent
        }

        fun mapRequestIntentToTaskIntent(requestIntent: RequestGroup.RequestIntent): Task.TaskIntent {
            val taskIntent =
                when (requestIntent) {
                    RequestGroup.RequestIntent.PLAN -> Task.TaskIntent.PLAN
                    RequestGroup.RequestIntent.PROPOSAL -> Task.TaskIntent.PROPOSAL
                    RequestGroup.RequestIntent.ORDER -> Task.TaskIntent.ORDER
                    else -> Task.TaskIntent.NULL
                }
            return taskIntent
        }
    }
}
