plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.psi.fhirapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.psi.fhirapp"
        minSdk = 32
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
//            excludes += "/META-INF/{ASL-2.0,AL2.0,LGPL2.1}"
            excludes += "META-INF/*"
        }
    }


//    exclude("jakarta.xml.bind:jakarta.xml.bind-api:2.3.2")

    configurations {
        all {
//            exclude(group = "javax.xml.bind", module = "jaxb-api")
//            exclude("jakarta.xml.bind:jakarta.xml.bind-api:2.3.2")
            exclude("com.sun.activation:javax.activation:1.2.0", "javax.activation-1.2.0", )
        }
    }

}

dependencies {

    val nav_version = "2.7.6"

    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")


    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.google.android.fhir:engine:0.1.0-beta05")
    implementation("com.google.android.fhir:data-capture:1.0.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Navigation related - Kotlin
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
//    implementation("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")


//    jakarta.xml.bind:jakarta.xml.bind-api:2.3.2

    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.2")

}