// Root build script — keep minimal. All plugins declared with `apply false` and applied per module.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics.plugin) apply false
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

// ────────────────────────────────────────────────────────────────────────────
// Detekt: per-subproject scan, shared config at config/detekt/detekt.yml
// ────────────────────────────────────────────────────────────────────────────
subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")

    extensions.configure<io.gitlab.arturbosch.detekt.extensions.DetektExtension> {
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        autoCorrect = false
        parallel = true
        ignoreFailures = false
        source.setFrom("src/main/kotlin", "src/test/kotlin", "src/androidTest/kotlin")
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Spotless: ktlint formatter on all Kotlin sources
// ────────────────────────────────────────────────────────────────────────────
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**", "**/generated/**")
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(
            mapOf(
                "ktlint_standard_filename" to "disabled",
            ),
        )
    }
    kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**")
        ktlint(libs.versions.ktlint.get())
    }
}

// ────────────────────────────────────────────────────────────────────────────
// File-line-limit enforcer (AppTest hard rule APT-Q-003: every source file ≤ 200 lines).
// `LargeClass` in detekt already catches class size, but file-level scope catches multi-class
// or top-level-fn files that detekt misses. Run via `./gradlew enforceFileLineLimit`.
// ────────────────────────────────────────────────────────────────────────────
tasks.register("enforceFileLineLimit") {
    group = "verification"
    description = "Fail when any .kt source file exceeds 200 lines (per APT-Q-003)."

    doLast {
        val limit = 200
        val whitelist = setOf(
            // AppStrings.kt is a pure data catalog (no logic) — see DEPENDENCY.md note
            "core/common/src/main/kotlin/com/apptest/core/common/AppStrings.kt",
        )

        val offenders = mutableListOf<Pair<String, Int>>()
        fileTree(rootDir) {
            include("**/*.kt")
            exclude("**/build/**", "**/generated/**", "**/.gradle/**")
        }.forEach { file ->
            val rel = file.relativeTo(rootDir).invariantSeparatorsPath
            if (rel in whitelist) return@forEach
            val lines = file.readLines().count()
            if (lines > limit) offenders += rel to lines
        }
        if (offenders.isNotEmpty()) {
            val report = offenders.joinToString("\n") { "  ${it.first}: ${it.second} lines" }
            throw GradleException("Files exceeding $limit-line limit (APT-Q-003):\n$report")
        }
    }
}
