import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics.plugin)
}

val localProps = Properties().also { props ->
    val f = rootProject.file("local.properties")
    if (f.exists()) props.load(f.inputStream())
}

val keystoreProps = Properties().also { props ->
    val f = rootProject.file("keystore.properties")
    if (f.exists()) props.load(f.inputStream())
}

android {
    namespace = "com.apptest.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cjc305.apptest"
        minSdk = 28
        targetSdk = 36
        versionCode = 3
        versionName = "0.1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Supabase credentials from local.properties — never hardcoded
        buildConfigField("String", "SUPABASE_URL",           "\"${localProps.getProperty("SUPABASE_URL",           "")}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY",      "\"${localProps.getProperty("SUPABASE_ANON_KEY",      "")}\"")
        // Google OAuth Web Client ID — from Google Cloud Console → OAuth 2.0 Client (Web application type)
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID",   "\"${localProps.getProperty("GOOGLE_WEB_CLIENT_ID",   "")}\"")
    }

    signingConfigs {
        create("upload") {
            val sf = keystoreProps.getProperty("storeFile")
            storeFile = if (sf.isNotEmpty()) rootProject.file(sf) else null
            storePassword = keystoreProps.getProperty("storePassword", "")
            keyAlias      = keystoreProps.getProperty("keyAlias",       "")
            keyPassword   = keystoreProps.getProperty("keyPassword",    "")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("upload")
        }
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
        buildConfig = true
    }
}

dependencies {
    // ── Core modules (all 8) ────────────────────────────────────────────
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:navigation"))

    // ── Feature modules (added as each :feature:* ships) ───────────────
    implementation(project(":feature:home"))
    implementation(project(":feature:myapps"))
    implementation(project(":feature:appdetail"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:testing"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:inbox"))

    // ── AndroidX runtime + lifecycle ────────────────────────────────────
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // ── Compose ─────────────────────────────────────────────────────────
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose.ui)
    implementation(libs.bundles.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    debugImplementation(libs.bundles.compose.debug)

    // ── Firebase (BoM pins versions; R-042 FCM + APT-X-004 Crashlytics) ──
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)

    // ── WorkManager + Hilt-Work ─────────────────────────────────────────
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.hilt.work)

    // ── DataStore — needed by FcmTopicManager (HIGH-003 fix) to persist last subscribed
    //    uid + pending unsubscribes across process death. :core:data already declares the
    //    same dep as `implementation` (not exposed); we add it here for :app's own users.
    implementation(libs.androidx.datastore.preferences)

    // ── Hilt ────────────────────────────────────────────────────────────
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)  // @HiltWorker annotation processing

    // ── Test ────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    androidTestImplementation(libs.bundles.test.android)
}
