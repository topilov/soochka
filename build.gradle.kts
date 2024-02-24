plugins {
    kotlin("jvm") version "1.9.22"
}

group = "me.topilov"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.6")
    implementation("org.jetbrains.kotlin:kotlin-scripting-common")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm")
    implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}