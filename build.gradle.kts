// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.11.0" apply false
    id("com.android.library") version "8.11.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("androidx.navigation.safeargs.kotlin") version  "2.8.5" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.27" apply false
     id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
  id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}

tasks.register("clean",Delete::class){
    delete(rootProject.buildDir)
}