import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
}

val envProperties = Properties().apply {
    val envFile = rootProject.file("env.properties")
    if (envFile.exists()) {
        envFile.inputStream().use { load(it) }
    }
}

fun getEnvProperty(key: String, required: Boolean = false): String {
    val prop = envProperties.getProperty(key)
    if (prop == null && required) {
        throw GradleException("Property '$key' not found in env.properties. Please add it to root/env.properties")
    }
    return prop ?: ""
}

android {
    namespace = "com.a3.yearlyprogess"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.a3.yearlyprogess"
        minSdk = 30
        targetSdk = 36
        versionCode = 213
        versionName = "4.0.0-alpha14"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(getEnvProperty("SIGNING_KEYSTORE_FILE"))
            storePassword = getEnvProperty("SIGNING_STORE_PASSWORD")
            keyAlias = getEnvProperty("SIGNING_KEY_ALIAS")
            keyPassword = getEnvProperty("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            resValue("string", "admob_application_id", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "admob_native_ad_unit", "ca-app-pub-3940256099942544/2247696110")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")

            resValue("string", "admob_application_id", getEnvProperty("ADMOB_APPLICATION_ID", required = true))
            resValue("string", "admob_native_ad_unit", getEnvProperty("ADMOB_NATIVE_AD_UNIT", required = true))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.material)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.material3.window.size.class1)
    implementation(libs.retrofit)
    implementation(libs.converter.kotlinx.serialization)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.accompanist.permissions)
    implementation(libs.coil.compose)
    implementation(libs.androidx.compose.animation.core)
    implementation(libs.roomdatabasebackup)
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}