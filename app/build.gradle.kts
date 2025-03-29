plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.ncorti.ktfmt.gradle") version "0.21.0"
  id("org.jetbrains.kotlin.plugin.compose")
}

android {
    compileSdk = 35

    defaultConfig {
        applicationId = "com.a3.yearlyprogess"
        minSdk = 30
        targetSdk = 35
        versionCode = 118
        versionName = "2.16.0-alpha01"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("\\keystore\\keystore.jks")
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
      compose = true
    }
    namespace = "com.a3.yearlyprogess"
  kotlinOptions {
    jvmTarget = "21"
  }
}

dependencies {

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("com.google.firebase:firebase-crashlytics:19.4.2")
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
  implementation("androidx.activity:activity-compose:1.10.1")
  implementation(platform("androidx.compose:compose-bom:2025.03.01"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // RecyclerView Selection
    implementation("androidx.recyclerview:recyclerview-selection:1.1.0")

    // Material Design 3.0
    implementation("com.google.android.material:material:1.13.0-alpha12")

    // Splash Screen for android 11 and below
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Ads
    implementation("com.google.android.gms:play-services-ads:24.1.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
  androidTestImplementation(platform("androidx.compose:compose-bom:2025.03.01"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")

  // Room
    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    androidTestImplementation("androidx.room:room-testing:$roomVersion")

    // Room backup and restore
    implementation("de.raphaelebner:roomdatabasebackup:1.0.2")

    // Lifecycle Components
    val lifecycleVersion = "2.8.7"

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")

    // UMP SDK
    implementation("com.google.android.ump:user-messaging-platform:3.1.0")

    // Preference
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
}
