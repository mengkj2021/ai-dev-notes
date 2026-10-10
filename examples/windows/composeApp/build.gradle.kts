import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.metadata.extractor)
    implementation(libs.commons.imaging)
}

compose.desktop {
    application {
        mainClass = "com.pictureorganizer.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe)
            packageName = "PictureOrganizer"
            packageVersion = "0.1.0"
            // jpackage on Windows mishandles non-ASCII description/vendor in args files
            description = "Picture Organizer (Windows example)"
            vendor = "AI-DEV examples"
            windows {
                menuGroup = "Picture Organizer"
                // Stable upgrade id for MSI; keep fixed once shipped
                upgradeUuid = "a8c2e6f1-4b3d-4e9a-9c1f-2d7b8e0a5f31"
            }
        }
    }
}
