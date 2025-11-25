import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.compose.desktop)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.ksp)
    id("hnau.android.lib")
}

compose.resources {
    packageOfResClass = "hnau.echospeak.projector.resources"
}

kotlin {
    sourceSets {
        all {
            languageSettings {
                optIn("kotlin.time.ExperimentalTime")
            }
        }
        commonMain {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                implementation(libs.hnau.projector)
                implementation(libs.hnau.model)
                implementation(libs.hnau.dynamiccolor)
                implementation(project(":echospeak:engine"))
                implementation(project(":echospeak:model"))
                implementation(compose.components.resources)
                implementation(libs.kotlin.datetime)
                implementation(libs.kotlin.immutable)
                implementation(libs.pipe.annotations)
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.pipe.processor)
}

tasks.withType<KotlinCompile>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
