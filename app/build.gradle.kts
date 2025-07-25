plugins {
  id("com.android.application")
  id("org.jetbrains.kotlin.android")
  id("com.google.devtools.ksp")
  id("kotlin-parcelize")
  id("androidx.navigation.safeargs.kotlin")
  id("com.google.gms.google-services")
  id("com.google.firebase.crashlytics")
  id("com.ncorti.ktfmt.gradle") version "0.23.0"
  id("org.jetbrains.kotlin.plugin.compose")
  id("org.jetbrains.kotlin.plugin.serialization")
}

android {
  compileSdk = 36

  defaultConfig {
    applicationId = "com.a3.yearlyprogess"
    minSdk = 30
    targetSdk = 36
    versionCode = 137
    versionName = "2.16.0-beta09"
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
      isMinifyEnabled = false
      isShrinkResources = false
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
  kotlinOptions { jvmTarget = "21" }
}

dependencies {
  implementation("androidx.core:core-ktx:1.16.0")
  implementation("androidx.appcompat:appcompat:1.7.1")
  implementation("androidx.constraintlayout:constraintlayout:2.2.1")
  implementation("androidx.preference:preference-ktx:1.2.1")
  implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.9.1")
  implementation("com.google.firebase:firebase-crashlytics:19.4.4")
  implementation("androidx.activity:activity-ktx:1.10.1")
  implementation("androidx.work:work-runtime-ktx:2.10.2")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.1")
  implementation("androidx.activity:activity-compose:1.10.1")
  implementation(platform("androidx.compose:compose-bom:2025.06.01"))
  implementation("androidx.compose.ui:ui")
  implementation("androidx.compose.ui:ui-graphics")
  implementation("androidx.compose.ui:ui-tooling-preview")
  implementation("androidx.compose.material3:material3")
  implementation("com.google.accompanist:accompanist-permissions:0.37.3")
  implementation("androidx.compose.ui:ui-tooling:1.8.3")
  implementation("androidx.navigation:navigation-compose:2.9.1")
  testImplementation("junit:junit:4.13.2")
  androidTestImplementation("androidx.test.ext:junit:1.2.1")
  androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

  // RecyclerView Selection
  implementation("androidx.recyclerview:recyclerview-selection:1.2.0")

  // Material Design 3.0
  implementation("com.google.android.material:material:1.14.0-alpha02")

  // Splash Screen for android 11 and below
  implementation("androidx.core:core-splashscreen:1.0.1")

  // Ads
  implementation("com.google.android.gms:play-services-ads:24.4.0")

  // Navigation
  implementation("androidx.navigation:navigation-fragment-ktx:2.9.1")
  implementation("androidx.navigation:navigation-ui-ktx:2.9.1")

  // Coroutines
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
  androidTestImplementation(platform("androidx.compose:compose-bom:2025.06.01"))
  androidTestImplementation("androidx.compose.ui:ui-test-junit4")
  debugImplementation("androidx.compose.ui:ui-tooling")
  debugImplementation("androidx.compose.ui:ui-test-manifest")

  // Room
  val roomVersion = "2.7.2"

  implementation("androidx.room:room-runtime:$roomVersion")
  ksp("androidx.room:room-compiler:$roomVersion")
  implementation("androidx.room:room-ktx:$roomVersion")
  androidTestImplementation("androidx.room:room-testing:$roomVersion")

  // Room backup and restore
  implementation("de.raphaelebner:roomdatabasebackup:1.1.0")

  // Lifecycle Components
  val lifecycleVersion = "2.9.1"

  implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
  implementation("androidx.lifecycle:lifecycle-common-java8:$lifecycleVersion")

  // UMP SDK
  implementation("com.google.android.ump:user-messaging-platform:3.2.0")

  // Preference
  implementation("androidx.preference:preference-ktx:1.2.1")

  // Retrofit
  implementation("com.squareup.retrofit2:retrofit:3.0.0")
  implementation("com.squareup.retrofit2:converter-gson:3.0.0")

  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}
