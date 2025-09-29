plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-kapt")
}

android {
    namespace = "com.amvarpvtltd.swiftNote"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.amvarpvtltd.selfnote"
        minSdk = 31
        targetSdk = 36
        versionCode = 8
        versionName = "2.0.0"

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


        // Add explicit 16 KB page size support
        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON"
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
            // Enable debug symbols for crash reporting
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
        debug {
            // Debug configuration
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
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
        buildConfig = true
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

        // Enable 16 KB page size alignment for .so files
        jniLibs {
            useLegacyPackaging = false
            // Enable 16 KB page alignment
            pickFirsts += listOf("**/libc++_shared.so", "**/liblog.so")
        }
    }

    // Updated splits configuration for 16 KB support
    splits {
        abi {
            isEnable = true
            reset()
            // Only include 64-bit ABIs that properly support 16KB page size
            include("arm64-v8a", "x86_64")
            isUniversalApk = false
        }
    }

    // Add lint configuration to catch 16KB compatibility issues
    lint {
        checkReleaseBuilds = true
        abortOnError = false
        warningsAsErrors = false
        // Enable 16KB page size checks
        enable += listOf("Instantiatable", "UnsafeNativeCodeLocation")
        // Add 16KB specific lint checks
        enable += listOf("NewerVersionAvailable", "GradleDependency")
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
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.pdf.viewer)
    kapt(libs.androidx.room.compiler)

    // ML Kit Entity Extraction for Smart Reminders
    implementation(libs.mlkit.entity.extraction)

    // Additional dependencies for Smart Reminders
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // CameraX + ML Kit Barcode scanning for QR scanner
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.play.services.mlkit.barcode.scanning)
    implementation(libs.androidx.concurrent.futures.ktx)

    // ZXing for QR code generation
    implementation(libs.zxing.core)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.3")
}