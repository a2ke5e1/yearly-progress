plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("com.materialkolor:material-kolor-jvm:5.0.1")
    implementation("org.jetbrains.compose.material3:material3:1.12.0-alpha03")

    testImplementation("junit:junit:4.13.2")
}