import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("hnau.android.lib")
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
                implementation(libs.hnau.model)
                implementation(libs.kotlin.datetime)
                implementation(libs.pipe.annotations)
                implementation(libs.enumvalues.annotations)
            }
        }
        androidMain
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.pipe.processor)
    add("kspCommonMainMetadata", libs.enumvalues.processor)
}

tasks.withType<KotlinCompile>().configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}
