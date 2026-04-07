plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    `maven-publish`
    signing
}

group   = "io.formcraft"
version = "0.1.0"

android {
    namespace = "io.formcraft.compose"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    api(project(":formcraft-core"))
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.foundation)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

// ── Maven Central publishing ──────────────────────────────────────────────────

afterEvaluate {
    publishing {
        publications {
            register<MavenPublication>("release") {
                groupId    = "io.formcraft"
                artifactId = "formcraft-compose"
                version    = "0.1.0"
                from(components["release"])

                pom {
                    name.set("FormCraft Compose")
                    description.set("Jetpack Compose UI components for FormCraft form validation")
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
}
