plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ksp) apply false
}

import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

subprojects {
    plugins.withId("com.android.application") {
        extensions.configure<ApplicationExtension> {
            val keystorePropertiesFile = rootProject.file("keystore.properties")
            if (!keystorePropertiesFile.exists()) return@configure
            val keystoreProperties = Properties().apply {
                keystorePropertiesFile.inputStream().use { load(it) }
            }
            signingConfigs {
                create("release") {
                    keyAlias = keystoreProperties.getProperty("keyAlias")
                    keyPassword = keystoreProperties.getProperty("keyPassword")
                    storeFile = rootProject.file(keystoreProperties.getProperty("storeFile"))
                    storePassword = keystoreProperties.getProperty("storePassword")
                }
            }
            buildTypes {
                getByName("release") {
                    signingConfig = signingConfigs.getByName("release")
                }
            }
        }
    }
}
