// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}

// Windows + OneDrive can lock files under `<module>/build/**` during incremental Android resource packaging.
// To avoid random build failures, place all build outputs under `%LOCALAPPDATA%/MemCloudBuild/`.
val memCloudBuildRoot = System.getenv("LOCALAPPDATA")?.let { java.io.File(it, "MemCloudBuild") }
subprojects {
    if (memCloudBuildRoot != null) {
        buildDir = java.io.File(memCloudBuildRoot, project.path.replace(':', '_'))
    }
}