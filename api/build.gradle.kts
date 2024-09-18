plugins {
    kotlin("jvm") version "1.8.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:2.3.2")
    implementation("io.ktor:ktor-server-netty-jvm:2.3.2")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:2.3.2")
    implementation("io.ktor:ktor-serialization-jackson-jvm:2.3.2")
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("com.github.docker-java:docker-java:3.3.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    implementation("io.ktor:ktor-server-cors-jvm:2.3.2")
}

application {
    mainClass.set("com.example.ApplicationKt")
}

