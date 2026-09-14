import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kmpgen)
    alias(libs.plugins.kover)
}

kotlin {
    android {
        namespace = "com.example.rickandmorty.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }

        androidResources {
            enable = true
        }
    }
    
    jvm()
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(libs.compose.material.icons.extended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test.junit5)
            implementation(libs.junit.params)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mockk)
            runtimeOnly(libs.junit.engine)
            runtimeOnly(libs.junit.platform.launcher)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("ch.qos.logback:logback-classic:1.4.14")
            implementation(libs.ktor.client.cio)
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.example.rickandmorty.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.example.rickandmorty"
            packageVersion = "1.0.0"
        }
    }
}

kmpgen {
    spec(packageName = "com.example.rickandmorty.data.remote.kmpgen") {
        specFile = file("src/commonMain/kotlin/com/example/rickandmorty/data/remote/openapi/rick-and-morty-openapi.json")
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kover {
    reports {
        total {
            html {
                onCheck = true // Generates the report when you run ./gradlew check
            }
            xml {
                onCheck = true
            }
        }
        filters {
            // Exclude the kmpgen-generated OpenAPI client (not hand-written code)
            excludes {
                packages("com.example.rickandmorty.data.remote.kmpgen")
            }
        }
    }
}