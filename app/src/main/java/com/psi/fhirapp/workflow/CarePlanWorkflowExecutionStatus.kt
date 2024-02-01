package com.psi.fhirapp.workflow

import com.psi.fhirapp.data.CarePlanWorkflowExecutionException
import java.time.OffsetDateTime


/** Currently only Started and Finished statuses are being used. */
sealed class CarePlanWorkflowExecutionStatus {
        val timestamp: OffsetDateTime = OffsetDateTime.now()

        /** Workflow execution has been started on the client. */
        class Started(val total: Int) : CarePlanWorkflowExecutionStatus()

        /** Workflow execution in progress. */
        class InProgress : CarePlanWorkflowExecutionStatus()

        /** Workflow execution finished successfully. */
        class Finished(val completed: Int = 0, val total: Int = 0) : CarePlanWorkflowExecutionStatus()

        /** Workflow execution failed. */
        data class Failed(val exceptions: List<CarePlanWorkflowExecutionException>) :
                CarePlanWorkflowExecutionStatus()
    }
