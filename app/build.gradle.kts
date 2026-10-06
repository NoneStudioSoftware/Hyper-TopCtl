import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
}

// ---- Version: derived from the git commit count ----
// versionCode must increase monotonically, otherwise a new APK cannot install over an existing
// one. The commit count gives that for free. CI checks out with `fetch-depth: 0`; a shallow
// clone would report 1. Building from a source archive without .git falls back to 1 as well.
val gitCommitCount: Int = runCatching {
    project.providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        workingDir = rootProject.projectDir
    }.standardOutput.asText.get().trim().toInt()
}.getOrDefault(1)

// Short commit hash, used to tell builds of the same version apart in artifact names.
val gitShortSha: String? = runCatching {
    project.providers.exec {
        commandLine("git", "rev-parse", "--short=7", "HEAD")
        workingDir = rootProject.projectDir
    }.standardOutput.asText.get().trim().ifBlank { null }
}.getOrNull()

val appVersionCode = gitCommitCount
val appVersionBase = "1.0.0"
val appVersionName = "$appVersionBase.$appVersionCode"

// ---- Release signing ----
// Credentials come from Gradle properties first, environment variables second, so a developer
// can keep them in the user-level ~/.gradle/gradle.properties while CI injects env vars.
// Secrets never live in the repository.
fun signingValue(name: String): String? =
    project.providers.gradleProperty(name).orNull
        ?: project.providers.environmentVariable(name).orNull

val releaseKeystore = signingValue("KEYSTORE_FILE")?.let(::File)?.takeIf { it.isFile }
val releaseKeystorePassword = signingValue("KEYSTORE_PASSWORD")
val releaseKeyAlias = signingValue("KEY_ALIAS")
val releaseKeyPassword = signingValue("KEY_PASSWORD")
val hasReleaseSigning = releaseKeystore != null &&
    releaseKeystorePassword != null &&
    releaseKeyAlias != null &&
    releaseKeyPassword != null

if (!hasReleaseSigning) {
    logger.lifecycle(
        "Release signing is not configured; release APKs will be unsigned. " +
            "Set KEYSTORE_FILE, KEYSTORE_PASSWORD, KEY_ALIAS and KEY_PASSWORD to sign them.",
    )
}

android {
    namespace = "io.github.hypertopctl"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "io.github.hypertopctl"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName
        // Consumed by the UI to render "1.0.0（15）" without re-parsing versionName.
        buildConfigField("String", "VERSION_BASE", "\"$appVersionBase\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// Name artifacts after the version so release uploads are self-describing:
// e.g. Hyper-TopCtl_1.0.0.15_3ea94eb-release.apk
base {
    archivesName.set(
        listOfNotNull("Hyper-TopCtl", appVersionName, gitShortSha).joinToString("_"),
    )
}

dependencies {
    // Modern libxposed API 102: api is compileOnly (provided by framework at runtime),
    // service is bundled so the app can read/write remote preferences.
    compileOnly(libs.libxposed.api)
    implementation(libs.libxposed.service)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.hiddenapibypass)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.material.kolor)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Miuix (HyperOS-style Compose UI)
    implementation(libs.miuix.ui)
    implementation(libs.miuix.icons)
    implementation(libs.miuix.preference)
    implementation(libs.miuix.nav)
}
