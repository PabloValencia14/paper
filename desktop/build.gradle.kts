plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.ui)
    implementation(compose.foundation)
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1")
    
    // JSON serialization
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Native renderer and PDF manipulation engine.
    implementation("org.apache.pdfbox:pdfbox:3.0.2")
    implementation("org.apache.pdfbox:fontbox:3.0.2")

    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
}

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    useJUnitPlatform()
}

compose.desktop {
    application {
        mainClass = "com.pablo.paper.desktop.MainKt"

        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Exe, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi)
            packageName = "PaperDesktop"
            packageVersion = "1.0.0"
            description = "Paper - Advanced PDF Reader & Productivity Suite for Windows"
            vendor = "Pablo"
            windows {
                menuGroup = "Paper"
                upgradeUuid = "6b75c88b-286a-4d37-84bc-877bf7394c92"
            }
        }
    }
}
