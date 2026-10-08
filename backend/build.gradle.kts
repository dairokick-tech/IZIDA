plugins {
    kotlin("jvm") version "2.0.21"
    application
}

repositories { mavenCentral() }

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:3.0.2")
    implementation("io.ktor:ktor-server-netty-jvm:3.0.2")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.0.2")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.0.2")
    implementation("org.postgresql:postgresql:42.7.4")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.jetbrains.exposed:exposed-core:0.57.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.57.0")
    implementation("ch.qos.logback:logback-classic:1.5.15")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}

application {
    mainClass.set("com.izida.backend.ApplicationKt")
}
