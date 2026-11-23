plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.matsuge.gm.sensketch"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.matsuge.gm.sensketch"
        minSdk = 27
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Keep the Android app and GitHub Pages on the same drawing-tool source.
    sourceSets.getByName("main").assets.srcDir(rootProject.file("docs"))

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
// BEGIN COMMON RELEASE SIGNING CONFIG
val uploadSigningStoreFile = providers.environmentVariable("ANDROID_SIGNING_STORE_FILE").orNull
val uploadSigningStorePassword = providers.environmentVariable("ANDROID_SIGNING_STORE_PASSWORD").orNull
val uploadSigningKeyAlias = providers.environmentVariable("ANDROID_SIGNING_KEY_ALIAS").orNull
val uploadSigningKeyPassword = providers.environmentVariable("ANDROID_SIGNING_KEY_PASSWORD").orNull
val hasUploadSigning = listOf(
    uploadSigningStoreFile,
    uploadSigningStorePassword,
    uploadSigningKeyAlias,
    uploadSigningKeyPassword
).all { !it.isNullOrBlank() }

val isBundleReleaseRequested = gradle.startParameter.taskNames.any {
    it == "bundleRelease" || it.endsWith(":bundleRelease")
}
if (isBundleReleaseRequested) {
    val missingSigningValues = mutableListOf<String>()
    if (uploadSigningStoreFile.isNullOrBlank()) missingSigningValues.add("ANDROID_SIGNING_STORE_FILE")
    if (uploadSigningStorePassword.isNullOrBlank()) missingSigningValues.add("ANDROID_SIGNING_STORE_PASSWORD")
    if (uploadSigningKeyAlias.isNullOrBlank()) missingSigningValues.add("ANDROID_SIGNING_KEY_ALIAS")
    if (uploadSigningKeyPassword.isNullOrBlank()) missingSigningValues.add("ANDROID_SIGNING_KEY_PASSWORD")
    if (missingSigningValues.isNotEmpty()) {
        throw GradleException("Release signing is not configured. Missing: ${missingSigningValues.joinToString()}")
    }
    if (!file(uploadSigningStoreFile!!).isFile) {
        throw GradleException("Release keystore file was not found: $uploadSigningStoreFile")
    }
}

android {
    signingConfigs {
        if (hasUploadSigning) {
            create("release") {
                storeFile = file(uploadSigningStoreFile!!)
                storePassword = uploadSigningStorePassword
                keyAlias = uploadSigningKeyAlias
                keyPassword = uploadSigningKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            if (hasUploadSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}
// END COMMON RELEASE SIGNING CONFIG
