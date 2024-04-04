package com.psi.fhirapp

import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.r4.model.Parameters
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun generateStructureMap() {
        val registrationQuestionnaireResponseString: String =
            "Questionnaire_Response.json".readFile()
        val immunizationStructureMap = "StructureMap_Generate.txt".readFile()

        val packageCacheManager = FilesystemPackageCacheManager(true)

        val contextR4 =
            SimpleWorkerContext.fromPackage(packageCacheManager.loadPackage("hl7.fhir.r4.core", "4.0.1"))
                .apply {
                    setExpansionProfile(Parameters())
                    isCanRunWithoutTerminology = true
                }

        // Create a FhirContext
        val ctx = FhirContext.forR4()

        val transformSupportServices = TransformSupportServices(contextR4)
        val structureMapUtilities =  org.hl7.fhir.r4.utils.StructureMapUtilities(contextR4, transformSupportServices)
        val structureMap =
            structureMapUtilities.parse(immunizationStructureMap, "psi reg")


        println( structureMap.encodeResourceToString(iParser) )
        assertEquals(true, true)
    }
}