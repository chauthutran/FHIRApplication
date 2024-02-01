package com.psi.fhirapp.workflow


import android.content.Context
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.gson.Gson
import com.google.gson.JsonArray
import org.hl7.fhir.r4.model.Resource

data class RequestResourceConfig(
    var resourceType: String,
    var values: List<Value>,
    var maxDuration: String,
    var unit: String
) {
    data class Value(var field: String, var value: String)
}

data class RequestConfiguration(
    var requestType: String,
    var intentConditions: List<IntentCondition>
) {
    data class IntentCondition(var intent: String, var action: String, var condition: String)
}

class ImplementationGuideConfig(
    var implementationGuideId: String,
    var patientRegistrationQuestionnaire: String,
    var entryPoint: String,
    var requestResourceConfigurations: List<RequestResourceConfig>,
    var requestConfigurations: List<RequestConfiguration>,
    var supportedValueSets: JsonArray,
    var triggers: List<Trigger>
)

data class Trigger(
    var event: String,
    var planDefinition: String,
    var structureMap: String,
    var targetResourceType: String
)

data class SupportedImplementationGuide(
    var location: String,
    var carePlanPolicy: String,
    var implementationGuideConfig: ImplementationGuideConfig,
)

data class CareConfiguration(var supportedImplementationGuides: List<SupportedImplementationGuide>)

object ConfigurationManager {
    var careConfiguration: CareConfiguration? = null

    fun getCareConfiguration(context: Context): CareConfiguration {
        if (careConfiguration == null) {
            val gson = Gson()
            val careConfig = readFileFromAssets(context, "care-config.json").trimIndent()
            careConfiguration = gson.fromJson(careConfig, CareConfiguration::class.java)
        }
        return careConfiguration!!
    }

    fun getCareConfigurationResources(): Collection<Resource> {
        var bundleCollection: Collection<Resource> = mutableListOf()
        val jsonParser = FhirContext.forCached(FhirVersionEnum.R4).newJsonParser()

        for (implementationGuide in careConfiguration?.supportedImplementationGuides!!) {
            val resourceJsonList = implementationGuide.implementationGuideConfig.supportedValueSets

            for (resourceJson in resourceJsonList) {
                val resource = jsonParser.parseResource(resourceJson.toString()) as Resource
                bundleCollection += resource
            }
        }
        return bundleCollection
    }

    private fun readFileFromAssets(context: Context, filename: String): String {
        return context.assets.open(filename).bufferedReader().use { it.readText() }
    }
}