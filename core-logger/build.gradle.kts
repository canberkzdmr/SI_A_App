import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    jacoco
    `maven-publish`
}

// -----------------------------------------------------------------------------
// Semantic Versioning Configuration
// -----------------------------------------------------------------------------
val versionPropsFile = project.file("version.properties")
val versionProps = Properties().apply {
    if (versionPropsFile.exists()) {
        FileInputStream(versionPropsFile).use { load(it) }
    }
}
val major = versionProps.getProperty("VERSION_MAJOR", "1").toInt()
val minor = versionProps.getProperty("VERSION_MINOR", "0").toInt()
val patch = versionProps.getProperty("VERSION_PATCH", "0").toInt()
val build = versionProps.getProperty("VERSION_BUILD", "1").toInt()
val rawSuffix = versionProps.getProperty("VERSION_SUFFIX", "").trim()
val suffix = if (rawSuffix.isNotEmpty()) "-$rawSuffix" else ""

val loggerVersionName = "$major.$minor.$patch$suffix"
val loggerVersionCode = major * 10000 + minor * 100 + patch

group = "com.cbo.core"
version = loggerVersionName

jacoco {
    toolVersion = "0.8.12"
}

android {
    namespace = "com.cbo.core.logger"
    compileSdk = 35

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField("String", "LOGGER_VERSION_NAME", "\"$loggerVersionName\"")
        buildConfigField("int", "LOGGER_VERSION_CODE", "$loggerVersionCode")
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.cbo.core"
                artifactId = "logger"
                version = loggerVersionName
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Versioning Helper Tasks
// -----------------------------------------------------------------------------
tasks.register("printLoggerVersion") {
    group = "versioning"
    description = "Prints the current version of the core-logger module"
    doLast {
        println("Core-Logger Version: $loggerVersionName (Code: $loggerVersionCode, Build: $build)")
    }
}

fun updateVersionProps(newMajor: Int, newMinor: Int, newPatch: Int, newBuild: Int, newSuffix: String) {
    versionProps.setProperty("VERSION_MAJOR", newMajor.toString())
    versionProps.setProperty("VERSION_MINOR", newMinor.toString())
    versionProps.setProperty("VERSION_PATCH", newPatch.toString())
    versionProps.setProperty("VERSION_BUILD", newBuild.toString())
    versionProps.setProperty("VERSION_SUFFIX", newSuffix)
    FileOutputStream(versionPropsFile).use { versionProps.store(it, "Core-Logger Versioning") }
    println("Updated Core-Logger version to: $newMajor.$newMinor.$newPatch${if (newSuffix.isNotBlank()) "-$newSuffix" else ""}")
}

tasks.register("bumpPatch") {
    group = "versioning"
    description = "Increments patch version of core-logger"
    doLast {
        updateVersionProps(major, minor, patch + 1, build + 1, versionProps.getProperty("VERSION_SUFFIX", ""))
    }
}

tasks.register("bumpMinor") {
    group = "versioning"
    description = "Increments minor version and resets patch to 0"
    doLast {
        updateVersionProps(major, minor + 1, 0, build + 1, versionProps.getProperty("VERSION_SUFFIX", ""))
    }
}

tasks.register("bumpMajor") {
    group = "versioning"
    description = "Increments major version and resets minor and patch to 0"
    doLast {
        updateVersionProps(major + 1, 0, 0, build + 1, versionProps.getProperty("VERSION_SUFFIX", ""))
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*",
        "**/ui/**",
        "**/*_Factory*.*",
        "**/*_MembersInjector*.*"
    )

    val debugTree = fileTree("${project.layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }

    val mainSrc = "${project.projectDir}/src/main/java"
    sourceDirectories.setFrom(files(mainSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(fileTree("${project.layout.buildDirectory.get()}") {
        include(listOf("**/*.exec", "**/*.ec"))
    })
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.kotlinx.coroutines.core)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Jetpack Compose (In-App Log Viewer UI)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
