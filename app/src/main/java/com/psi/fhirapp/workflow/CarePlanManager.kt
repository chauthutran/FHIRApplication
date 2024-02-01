package com.psi.fhirapp.workflow

import android.content.Context
import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.knowledge.FhirNpmPackage
import com.google.android.fhir.knowledge.KnowledgeManager
import com.google.android.fhir.workflow.FhirOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.hl7.fhir.r4.model.Resource
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
            return
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

                            withContext(Dispatchers.IO) {
                                val fis = FileOutputStream(File(context.filesDir, "$path/$filename"))
                                fis.write(contents.toByteArray())
                                println("Saved: ${context.filesDir}/$path/$filename")
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
        knowledgeManager.install(
            FhirNpmPackage(
                "who.fhir.immunization",
                "1.0.0",
                "https://github.com/WorldHealthOrganization/smart-immunizations",
            ),
            rootDirectory,
        )
        println("KM has been initialized")
    }

    private fun readFileFromAssets(context: Context, filename: String): String {
        return context.assets.open(filename).bufferedReader().use { it.readText() }
    }

}