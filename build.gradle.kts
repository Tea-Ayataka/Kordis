val ktorVersion = "1.3.0"
val coroutinesVersion = "1.3.3"

plugins {
    kotlin("jvm") version "1.3.61"
}

group = "net.ayataka"
version = "0.2.3"

tasks.compileKotlin {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

repositories {
    mavenLocal()
    jcenter()
    maven(url = "https://kotlin.bintray.com/ktor")
    maven(url = "https://kotlin.bintray.com/kotlinx")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Http client by Ktor
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-okhttp:$ktorVersion")

    implementation("com.neovisionaries:nv-websocket-client:2.8")
    implementation("com.google.code.gson:gson:2.8.5")

    api("org.slf4j:slf4j-api:1.7.25")

    testImplementation("org.apache.logging.log4j:log4j-slf4j-impl:2.11.0")
    testImplementation("junit:junit:4.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks.test {
    val tokenTest: String? by project
    systemProperties("token" to (tokenTest ?: System.getenv("tokenTest")))
}
