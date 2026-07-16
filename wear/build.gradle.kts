import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "kattcrazy.sharemything"
    compileSdk = 36

    defaultConfig {
        applicationId = "kattcrazy.sharemything"
        minSdk = 30
        targetSdk = 35
        // Play versionCode: wear uses 2000, 2001… (offset from phone); versionName stays in sync with mobile.
        versionCode = 2038
        versionName = "2.3.8"
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue("string", "app_name", "Share My Thing Debug")
            resValue("string", "app_name_short", "Share My Thing Debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":ui-theme"))

    val composeBom = platform(libs.androidx.compose.bom)

    implementation(libs.androidx.core.ktx)
    implementation(composeBom)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wear.compose.material3)

    implementation(libs.wear.tiles)
    implementation(libs.wear.protolayout)
    implementation(libs.wear.protolayout.material3)
    implementation(libs.wear.watchface.complications.data)
    implementation(libs.wear.watchface.complications.data.source.ktx)
    implementation(libs.guava)
    implementation(libs.play.services.wearable)

    debugImplementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.wear.compose.ui.tooling)
    debugImplementation(libs.wear.tiles.tooling.preview)
    debugImplementation(libs.wear.tiles.tooling)
}
