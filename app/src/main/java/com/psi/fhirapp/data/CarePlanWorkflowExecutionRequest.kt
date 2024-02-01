package com.psi.fhirapp.data

import com.psi.fhirapp.workflow.CarePlanWorkflowExecutionStatus
import org.hl7.fhir.r4.model.Patient

data class CarePlanWorkflowExecutionRequest(
    val patient: Patient,
    val carePlanWorkflowExecutionStatus: CarePlanWorkflowExecutionStatus
)
