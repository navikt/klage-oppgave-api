import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val exposedVersion = "0.21.1"
val mockkVersion = "1.9.3"
val h2Version = "1.4.200"
val tokenValidationVersion = "1.3.1"
val oidcSupportVersion = "0.2.18"
val logstashVersion = "5.1"
val springSleuthVersion = "2.2.3.RELEASE"
val unleashVersion = "3.3.3"
val problemSpringWebStartVersion = "0.26.2"

val springRetryVersion = "1.3.0"
val springMockkVersion = "2.0.3"
val springFoxVersion = "3.0.0"

val githubUser: String by project
val githubPassword: String by project

val testContainersVersion = "1.15.0"

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/simple-slack-poster")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
    jcenter()
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.4.10"
    id("org.springframework.boot") version "2.3.4.RELEASE"
    id("org.jetbrains.kotlin.plugin.spring") version "1.4.10"
    idea
}

apply(plugin = "io.spring.dependency-management")

dependencies {
    implementation(kotlin("stdlib"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")

    implementation("org.springframework.cloud:spring-cloud-starter-sleuth:$springSleuthVersion")
    implementation("io.springfox:springfox-boot-starter:$springFoxVersion")

    implementation("org.projectreactor:reactor-spring:1.0.1.RELEASE")

    implementation("org.flywaydb:flyway-core")
    implementation("com.zaxxer:HikariCP")
//    implementation("org.jetbrains.exposed:exposed-spring-boot-starter:$exposedVersion")
//    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("ch.qos.logback:logback-classic")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("no.nav.security:token-validation-spring:$tokenValidationVersion")
    implementation("no.nav.security:token-client-spring:$tokenValidationVersion")
    implementation("no.nav.security:oidc-spring-support:$oidcSupportVersion")

    implementation("no.nav.slackposter:simple-slack-poster:5")
    implementation("org.springframework.retry:spring-retry:$springRetryVersion")
    implementation("no.finn.unleash:unleash-client-java:$unleashVersion")
    implementation("org.zalando:problem-spring-web-starter:$problemSpringWebStartVersion")

    implementation("org.springframework.boot:spring-boot-starter-data-elasticsearch")

    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("com.ninja-squad:springmockk:$springMockkVersion")

    testImplementation("com.h2database:h2:$h2Version")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage")
        exclude(group = "org.mockito")
    }
    testImplementation("org.testcontainers:testcontainers:$testContainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testContainersVersion")
    testImplementation("org.testcontainers:elasticsearch:$testContainersVersion")
}

idea {
    module {
        isDownloadJavadoc = true
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    this.archiveFileName.set("app.jar")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main/kotlin")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test/kotlin")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
