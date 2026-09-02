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
    
    // JavaFX for WebView PDF.js integration
    implementation("org.openjfx:javafx-base:17.0.8:win")
    implementation("org.openjfx:javafx-graphics:17.0.8:win")
    implementation("org.openjfx:javafx-controls:17.0.8:win")
    implementation("org.openjfx:javafx-web:17.0.8:win")
    implementation("org.openjfx:javafx-swing:17.0.8:win")
    
    // High-performance PDF Rendering, AcroForms, OCR, Watermarks, Bates & Manipulation Engine
    implementation("org.apache.pdfbox:pdfbox:3.0.2")
    implementation("org.apache.pdfbox:fontbox:3.0.2")
    
    // Digital Signatures, PKCS#12, AES Cryptography & Permissions
    implementation("org.bouncycastle:bcprov-jdk18on:1.78.1")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
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
