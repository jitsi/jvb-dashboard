plugins {
    id("org.jetbrains.kotlin.js") version "1.4.10"
}

group = "org.jitsi"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-js"))

    implementation("org.jetbrains:kotlin-react:16.13.1-pre.110-kotlin-1.4.0")
    implementation("org.jetbrains:kotlin-react-dom:16.13.1-pre.110-kotlin-1.4.0")
    implementation(npm("react", "16.13.1"))
    implementation(npm("react-dom", "16.13.1"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")

    implementation(npm("highcharts", "8.2.2"))
    implementation(npm("highcharts-react-official", "3.0.0"))

    implementation("org.jetbrains:kotlin-styled:1.0.0-pre.110-kotlin-1.4.0")
    implementation(npm("styled-components", "~5.1.1"))
    implementation(npm("inline-style-prefixer", "~6.0.0"))
    implementation(npm("react-select", "~3.1.0"))
    implementation("org.jetbrains:kotlin-react-router-dom:5.2.0-pre.136-kotlin-1.4.10")
}

kotlin {
    sourceSets.configureEach {
        languageSettings.apply {
            useExperimentalAnnotation("kotlinx.coroutines.ExperimentalCoroutinesApi")
            useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        }
    }
    js {
        browser {
            webpackTask {
                cssSupport.enabled = true
            }

            runTask {
                cssSupport.enabled = true
            }

            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }
}
