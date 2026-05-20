plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.apptest.core.designsystem"
    compileSdk = 36

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = libs.versions.javaTarget.get()
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    api(project(":core:common"))                  // AppStrings / AppStringsCatalog for LocalAppStrings

    implementation(platform(libs.androidx.compose.bom))
    api(libs.bundles.compose.material3)
    api(libs.bundles.compose.ui)
    api(libs.androidx.core.ktx)

    debugImplementation(libs.bundles.compose.debug)
}
