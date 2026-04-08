plugins {
    alias(libs.plugins.kotlin.multiplatform)
    `maven-publish`
    signing
}

group   = "io.formcraft"
version = "0.1.0"

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test"))
        }
    }
}

// ── Maven Central publishing ──────────────────────────────────────────────────

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set("FormCraft Core")
                description.set("Zero-reflection, type-safe, async-capable form validation for Kotlin Multiplatform")
                url.set("https://github.com/sundramsingh/FormCraft")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("sundramsingh")
                        name.set("Sundram Singh")
                        url.set("https://github.com/sundramsingh")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/sundramsingh/FormCraft.git")
                    developerConnection.set("scm:git:ssh://github.com/sundramsingh/FormCraft.git")
                    url.set("https://github.com/sundramsingh/FormCraft/tree/main")
                }
            }
        }
    }

    repositories {
        maven {
            name = "MavenCentral"
            val releasesUrl  = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (version.toString().endsWith("SNAPSHOT")) snapshotsUrl else releasesUrl
            credentials {
                username = providers.gradleProperty("mavenCentralUsername").orNull
                    ?: System.getenv("ORG_GRADLE_PROJECT_mavenCentralUsername")
                password = providers.gradleProperty("mavenCentralPassword").orNull
                    ?: System.getenv("ORG_GRADLE_PROJECT_mavenCentralPassword")
            }
        }
    }
}

val signingKey = providers.gradleProperty("signingInMemoryKey").orNull
    ?: System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey")
val signingPass = providers.gradleProperty("signingInMemoryKeyPassword").orNull
    ?: System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword")
val skipSigning = providers.gradleProperty("skipSigning").isPresent

if (!signingKey.isNullOrBlank() && !signingPass.isNullOrBlank() && !skipSigning) {
    signing {
        useInMemoryPgpKeys(signingKey, signingPass)
        sign(publishing.publications)
    }
}
