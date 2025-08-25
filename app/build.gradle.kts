plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "com.amvarpvtltd.selfnote"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.amvarpvtltd.selfnote"
        minSdk = 26
        targetSdk = 35
        versionCode = 7
        versionName = "1.0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Add 16KB page size testing support
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR,LOW-BATTERY"

        // Add Room schema export directory
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true",
                    "room.expandProjection" to "true"
                )
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // Debug configuration
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
        compose = true
    }

    // Configure KAPT properly
    kapt {
        correctErrorTypes = true
        useBuildCache = true
        mapDiagnosticLocations = true
        javacOptions {
            this.option("-Xmaxerrs".toString(), 500.toString())
            this.option("-Xmaxwarns".toString(), 500.toString())
        }
        arguments {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.expandProjection", "true")
        }
    }

    // Comprehensive packaging configuration for 16KB page size compatibility
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            // Exclude problematic native debug symbols
            excludes += "**/dump_syms/**"
            excludes += "**/dump_syms.bin"
        }

        // Critical: JNI library configuration for 16KB alignment
        jniLibs {
            useLegacyPackaging = false
            pickFirsts += listOf(
                "**/libc++_shared.so",
                "**/libjsc.so",
                "**/libfbjni.so",
                "**/libreactnativejni.so",
                "**/libhermes.so"
            )
            // Exclude debug symbols that cause alignment issues
            excludes += listOf(
                "**/dump_syms.bin",
                "**/dump_syms/**",
                "**/libcrashlytics.so"
            )
        }

        // Ensure proper DEX alignment
        dex {
            useLegacyPackaging = false
        }
    }

    // Remove problematic splits configuration and replace with proper one
    splits {
        abi {
            isEnable = true
            reset()
            // Only include ABIs that support 16KB page size properly
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = false
        }
        // Remove density splits completely as they're deprecated
    }

    // Add lint configuration to catch 16KB compatibility issues
    lint {
        checkReleaseBuilds = true
        abortOnError = false
        warningsAsErrors = false
        // Enable 16KB page size checks
        enable += listOf("Instantiatable", "UnsafeNativeCodeLocation")
    }

    // Add bundle configuration for Play Store (preferred over APK splits)
    bundle {
        abi {
            enableSplit = true
        }
        density {
            enableSplit = true  // Use bundles for density instead of deprecated splits
        }
        language {
            enableSplit = false
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Add Android Material library for XML themes
    implementation(libs.androidx.material)

    // Firebase BOM - MUST be added as platform dependency
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.crashlytics.buildtools)

    implementation(libs.androidx.tv.material)

    // Use version catalog dependencies where possible
    implementation(libs.gson)

    // Room components with proper versions from catalog
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    // ML Kit Entity Extraction for Smart Reminders
    implementation(libs.mlkit.entity.extraction)

    // Additional dependencies for Smart Reminders
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)


}