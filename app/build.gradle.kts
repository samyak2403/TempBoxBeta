plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.samyak.tempboxbeta"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.samyak.tempboxbeta"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        // Enable core library desugaring for java.time support on older Android versions
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        viewBinding = true
    }
    lint {
        baseline = file("lint-baseline.xml")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    // https://gitlab.com/fdroid/fdroidserver/-/issues/1056
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles.
        includeInBundle = false
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.core:core:1.13.0")
        force("androidx.appcompat:appcompat:1.7.0")
        
        // Exclude all support library dependencies in favor of AndroidX
        exclude(group = "com.android.support")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.fragment)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.recyclerview)
    implementation(libs.cardview)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.androidx.swiperefreshlayout)
    
    // Core library desugaring for java.time support on API level < 26
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Sliding Button - SlideToAct (Modern AndroidX compatible)
    implementation("com.ncorti:slidetoact:0.11.0") {
        exclude(group = "com.android.support", module = "support-compat")
        exclude(group = "com.android.support", module = "support-core-utils")
        exclude(group = "com.android.support", module = "support-annotations")
    }
    
    // Force AndroidX versions to avoid conflicts
    implementation("androidx.core:core:1.13.0")
    implementation("androidx.appcompat:appcompat:1.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}