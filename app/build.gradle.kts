
import com.android.build.api.dsl.ApplicationExtension

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// 1. Reemplazamos el bloque 'android { ... }' por 'configure<ApplicationExtension> { ... }'
// para evitar la advertencia de deprecación de BaseAppModuleExtension.
configure<ApplicationExtension> {
    namespace = "samf.gestorestudiantil"
    compileSdk = 36

    defaultConfig {
        applicationId = "samf.gestorestudiantil"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        multiDexEnabled = true
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
        }
    }
}

// 3. ¡CORRECCIÓN IMPORTANTE! El bloque ksp DEBE ir fuera de la configuración de Android
ksp {
    arg("hilt.correctErrorTypes", "true")
}

// Configuración global de Kotlin fuera de android { }
kotlin {
    jvmToolchain(17)
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "io.grpc") {
            useVersion("1.62.2") // Versión estable compatible con Firestore 26.2.0
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.text.google.fonts)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Importar la plataforma Firebase BOM
    implementation(platform(libs.firebase.bom))

    // Librerías de Firebase (Auth, Firestore y Messaging)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.messaging)

    // Librería para Google Sign-In
    implementation(libs.play.services.auth)

    // Navegación en Compose (Navigation 3)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.kotlinx.serialization.json)

    // Cloudinary para Android
    implementation(libs.cloudinary.android)
    // Coil para mostrar la imagen desde la URL
    implementation(libs.coil.compose)

    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.datastore.preferences)

    // Credential Manager
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.gson)

    implementation(libs.godaddy.colorpicker)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.core)
    implementation(libs.supabase.storage)
    implementation(libs.supabase.postgrest)
    implementation(libs.ktor.client.android)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Google Auth Library for FCM HTTP v1 (Teacher app only)
    implementation(libs.google.auth.library) {
        exclude(group = "io.grpc", module = "grpc-netty-shaded")
    }

    implementation(libs.calendar.compose)
}