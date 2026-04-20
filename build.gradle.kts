plugins {
    kotlin("multiplatform") version "2.0.0"
}

group = "org.jitsi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    js {
        browser {
            webpackTask {
                cssSupport {
                    enabled.set(true)
                }
            }

            runTask {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }
    sourceSets["jsMain"].languageSettings {
        apply {
            optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            optIn("kotlin.time.ExperimentalTime")
        }
    }

    sourceSets["jsMain"].dependencies {
        implementation(kotlin("stdlib-js"))

        implementation("org.jetbrains.kotlin-wrappers:kotlin-react:18.3.1-pre.770")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-legacy:18.3.1-pre.770")
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom-legacy:18.3.1-pre.770")

        implementation(npm("react", "18.3.1"))
        implementation(npm("react-dom", "18.3.1"))

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")

        implementation(npm("highcharts", "11.4.6"))
        implementation(npm("highcharts-react-official", "3.2.1"))

        implementation("org.jetbrains.kotlin-wrappers:kotlin-styled-next:1.2.4-pre.770")
        implementation(npm("inline-style-prefixer", "~7.0.1"))
        implementation(npm("react-select", "~5.8.0"))
        implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:6.23.1-pre.770")
    }
}
