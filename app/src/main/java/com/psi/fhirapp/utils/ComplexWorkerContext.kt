package com.psi.fhirapp.utils

import org.hl7.fhir.r4.context.SimpleWorkerContext
import org.hl7.fhir.utilities.npm.NpmPackage
import java.io.IOException


class ComplexWorkerContext : SimpleWorkerContext() {
    @Throws(IOException::class)
    fun loadFromMultiplePackages(packages: ArrayList<NpmPackage>, allowDuplicates: Boolean?) {
        this.isAllowLoadingDuplicates = allowDuplicates!!
        for (i in packages.indices) {
            println("--- packages.indices[i]: ${packages[i].path}")
            loadFromPackage(packages[i], null)
        }
    }
    fun fromClassPaths(name: String) {
//        this.isAllowLoadingDuplicates = allowDuplicates!!
//        for (i in packages.indices) {
//            println("--- packages.indices[i]: ${packages[i].path}")
        loadFromFolder("careplan")
//        }
    }


}

