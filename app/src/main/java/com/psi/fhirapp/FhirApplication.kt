package com.psi.fhirapp

import android.app.Application
import android.content.Context
import android.util.Log
import ca.uhn.fhir.context.FhirContext
import com.google.android.fhir.DatabaseErrorStrategy
import com.google.android.fhir.FhirEngine
import com.google.android.fhir.FhirEngineConfiguration
import com.google.android.fhir.FhirEngineProvider
import com.google.android.fhir.NetworkConfiguration
import com.google.android.fhir.ServerConfiguration
import com.google.android.fhir.datacapture.BuildConfig
import com.google.android.fhir.datacapture.DataCaptureConfig
import com.google.android.fhir.datacapture.XFhirQueryResolver
import com.google.android.fhir.search.search
import com.google.android.fhir.sync.FhirSyncWorker
import com.google.android.fhir.sync.Sync
import com.google.android.fhir.sync.remote.HttpLogger
import com.psi.fhirapp.sync.PatientPeriodicSyncWorker
import com.psi.fhirapp.sync.ReferenceUrlResolver
import com.psi.fhirapp.utils.ComplexWorkerContext
import com.psi.fhirapp.utils.ValueSetResolver
import com.psi.fhirapp.workflow.CarePlanManager
import com.psi.fhirapp.workflow.RequestManager
import com.psi.fhirapp.workflow.TestRequestHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.hl7.fhir.utilities.npm.NpmPackage


class FhirApplication : Application(), DataCaptureConfig.Provider {

    /**
     * This instantiate of FHIR Engine ensures the FhirEngine instance is only created
     * when it's accessed for the first time, not immediately when the app starts.
     **/
    private val fhirEngine: FhirEngine by lazy { constructFhirEngine() }
    private val carePlanManager: CarePlanManager by lazy { constructCarePlanManager() }
    private val requestManager: RequestManager by lazy { constructRequestManager()}
    private var dataCaptureConfig: DataCaptureConfig? = null


    private var contextR4: ComplexWorkerContext? = null


    override fun onCreate() {
        super.onCreate()

        // Init FHIR engine
        FhirEngineProvider.init(
            FhirEngineConfiguration(
                //Enables data encryption if the device supports it.
                enableEncryptionIfSupported = true,
                // Determines the database error strategy. In this case, it recreates the database if an error occurs upon opening
                DatabaseErrorStrategy.RECREATE_AT_OPEN,
                ServerConfiguration(
                    "https://hapi.fhir.org/baseR4/",
//                    "http://172.30.1.27:8080/fhir/",
                    httpLogger =
                    HttpLogger(
                        HttpLogger.Configuration(
                            if (BuildConfig.DEBUG) HttpLogger.Level.BODY else HttpLogger.Level.BASIC,
                        ),
                    ) {
                        Log.d("App-HttpLog", it)
                    },
                    networkConfiguration = NetworkConfiguration(uploadWithGzip = false),
//                    authenticator = { HttpAuthenticationMethod.Bearer("mySecureToken") }
                ),
            ),
        )

        constructR4Context()
//        Sync.oneTimeSync<PatientPeriodicSyncWorker>(this)

        dataCaptureConfig =
            DataCaptureConfig().apply {
                urlResolver = ReferenceUrlResolver(this@FhirApplication as Context)
                xFhirQueryResolver = XFhirQueryResolver { it -> fhirEngine.search(it).map { it.resource } }
            }
    }



    private fun constructCarePlanManager(): CarePlanManager {
        return CarePlanManager(fhirEngine, FhirContext.forR4(), this)
    }


    private fun constructRequestManager(): RequestManager {
        return RequestManager(fhirEngine, FhirContext.forR4(), TestRequestHandler())
    }

    private fun constructR4Context() =
        CoroutineScope(Dispatchers.IO).launch {
            println("**** creating contextR4")

            val measlesIg = async {
                NpmPackage.fromPackage(assets.open("smart-imm-measles/ig/package.r4.tgz"))
            }

            val baseIg = async { NpmPackage.fromPackage(assets.open("package.tgz")) }

            val packages = arrayListOf<NpmPackage>(measlesIg.await(), baseIg.await())

            println("**** read assets contextR4")
            contextR4 = ComplexWorkerContext()
            contextR4?.apply {
                loadFromMultiplePackages(packages, true)
                println("**** created contextR4")
                ValueSetResolver.init(this@FhirApplication, this)
            }
        }


    override fun getDataCaptureConfig(): DataCaptureConfig = dataCaptureConfig ?: DataCaptureConfig()

    private fun constructFhirEngine(): FhirEngine {
        return FhirEngineProvider.getInstance(this)
    }


    // Easier access throughout your application
    companion object {
        fun fhirEngine(context: Context) =
            (context.applicationContext as FhirApplication).fhirEngine

        fun carePlanManager(context: Context) =
            (context.applicationContext as FhirApplication).carePlanManager

        fun requestManager(context: Context) =
            (context.applicationContext as FhirApplication).requestManager

        fun contextR4(context: Context) = (context.applicationContext as FhirApplication).contextR4


//        fun fhirContext(context: Context) = (context.applicationContext as FhirApplication).fhirContext
//        fun fhirOperator(context: Context) = (context.applicationContext as FhirApplication).fhirOperator
    }

}

