plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
}

group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

dependencies {

    // Ktor Server Core Dependencies
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.default.headers)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.sessions)
    implementation(libs.ktor.server.netty)

    // Serialization and Logging Dependencies
    implementation(libs.ktor.serialization.gson)
    implementation(libs.logback.classic)

    // Database (MongoDB) Dependencies
    implementation(libs.kmongo)
    implementation(libs.kmongo.coroutine)

    // Dependency Injection (DI) with Koin
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    // Test Dependencies
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.gson)
    testImplementation(libs.koin.test)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.truth)
}