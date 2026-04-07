/**
 * Shared Maven Central publishing convention.
 * Apply in module build files via:
 *   apply(from = rootProject.file("gradle/publishing.gradle.kts"))
 *
 * Required project properties (set in gradle.properties or CI env):
 *   POM_ARTIFACT_ID         e.g. "formcraft-core"
 *   POM_NAME                e.g. "FormCraft Core"
 *   POM_DESCRIPTION         e.g. "Zero-reflection form validation for KMP"
 *
 * Required secrets (CI environment variables):
 *   ORG_GRADLE_PROJECT_mavenCentralUsername
 *   ORG_GRADLE_PROJECT_mavenCentralPassword
 *   ORG_GRADLE_PROJECT_signingInMemoryKey
 *   ORG_GRADLE_PROJECT_signingInMemoryKeyPassword
 */

// Publishing coordinates — override per module via gradle.properties or ext
val GROUP_ID     = "io.formcraft"
val VERSION      = "0.1.0"

val pomArtifactId: String by project
val pomName:       String by project
val pomDesc:       String by project

apply(plugin = "maven-publish")
apply(plugin = "signing")

extensions.configure<PublishingExtension> {
    publications {
        // For KMP modules the kotlin plugin already registers publications.
        // For Android library modules we register one here.
        if (project.plugins.hasPlugin("com.android.library")) {
            register<MavenPublication>("release") {
                groupId    = GROUP_ID
                artifactId = pomArtifactId
                version    = VERSION

                afterEvaluate {
                    from(components["release"])
                }

                pom {
                    name.set(pomName)
                    description.set(pomDesc)
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
        } else {
            // KMP: configure all auto-generated publications with POM metadata
            withType<MavenPublication> {
                groupId    = GROUP_ID
                version    = VERSION
                pom {
                    name.set(pomName)
                    description.set(pomDesc)
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
    }

    repositories {
        maven {
            name = "MavenCentral"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (VERSION.endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

            credentials {
                username = providers.gradleProperty("mavenCentralUsername").orNull
                    ?: System.getenv("ORG_GRADLE_PROJECT_mavenCentralUsername")
                password = providers.gradleProperty("mavenCentralPassword").orNull
                    ?: System.getenv("ORG_GRADLE_PROJECT_mavenCentralPassword")
            }
        }
    }
}

extensions.configure<SigningExtension> {
    val signingKey = providers.gradleProperty("signingInMemoryKey").orNull
        ?: System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKey")
    val signingPassword = providers.gradleProperty("signingInMemoryKeyPassword").orNull
        ?: System.getenv("ORG_GRADLE_PROJECT_signingInMemoryKeyPassword")

    if (signingKey != null && signingPassword != null) {
        useInMemoryPgpKeys(signingKey, signingPassword)
        val publishing = extensions.getByType<PublishingExtension>()
        sign(publishing.publications)
    }
}
