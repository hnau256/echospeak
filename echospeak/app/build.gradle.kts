import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.ksp)
    id("hnau.android.app")
}

android {
    namespace = "hnau.echospeak"

    defaultConfig {
        val versionPropsFile = file("version.properties")
        val versionProps = Properties().apply {
            load(versionPropsFile.inputStream())
        }
        val localVersionCode = (versionProps["versionCode"] as String).toInt()
        versionName = versionProps["versionName"] as String + "." + localVersionCode
        versionCode = localVersionCode

        tasks.named("preBuild") {
            doFirst {
                versionProps.setProperty("versionCode", (localVersionCode + 1).toString())
                versionProps.store(versionPropsFile.outputStream(), null)
            }
        }
    }

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")

    signingConfigs {
        create("qa") {
            storeFile = file("keystores/qa.keystore")
            storePassword = "password"
            keyAlias = "qa"
            keyPassword = "password"
        }
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix =".debug"
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFile("proguard-rules.pro")
            //signingConfig = signingConfigs.getByName("release")
        }
        create("qa") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            signingConfig = signingConfigs.getByName("qa")
            applicationIdSuffix =".qa"
        }
    }
}

compose.resources {
    packageOfResClass = "hnau.echospeak.projector"
}

kotlin {
    androidTarget()
    sourceSets {
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(libs.hnau.projector)
                implementation(libs.hnau.model)
                implementation(project(":echospeak:engine"))
                implementation(project(":echospeak:model"))
                implementation(project(":echospeak:projector"))
                implementation(compose.components.resources)
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.serialization.core)
            }
        }

        androidMain {
            dependencies {
                implementation(libs.android.activity.compose)
                implementation(libs.android.appcompat)
                implementation(libs.room.runtime)
                implementation(libs.room.ktx)
                implementation(libs.kotlin.serialization.json)
                implementation(libs.kotlin.coroutines.android)
                implementation(libs.kotlin.playServices)
                implementation(libs.google.translate)
            }
        }
    }
}

dependencies {
    kspAndroid(libs.room.processor)
}

afterEvaluate {
    tasks.named("kspDebugKotlinAndroid").configure {
        dependsOn(tasks.named("generateResourceAccessorsForAndroidDebug"))
        dependsOn(tasks.named("generateResourceAccessorsForCommonMain"))
        dependsOn(tasks.named("generateExpectResourceCollectorsForCommonMain"))
        dependsOn(tasks.named("generateActualResourceCollectorsForAndroidMain"))
        dependsOn(tasks.named("generateComposeResClass"))
    }
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.addAll(
                listOf(
                    "-opt-in=kotlin.time.ExperimentalTime"
                )
            )
        }
    }
}

compose.desktop {
    application {
        mainClass = "hnau.echospeak.app.DesktopAppKt"
    }
}
