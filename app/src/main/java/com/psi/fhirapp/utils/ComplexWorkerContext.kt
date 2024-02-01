package com.psi.fhirapp.utils

import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.utilities.npm.NpmPackage
import java.io.IOException


class ComplexWorkerContext : SimpleWorkerContext() {
    @Throws(IOException::class)
    fun loadFromMultiplePackages(packages: ArrayList<NpmPackage>, allowDuplicates: Boolean?) {
        this.isAllowLoadingDuplicates = allowDuplicates!!
        for (i in packages.indices) {
            loadFromPackage(packages[i], null)
        }
    }
}

