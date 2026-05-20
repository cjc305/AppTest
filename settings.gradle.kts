pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AppTest"

include(":app")

include(":core:common")
include(":core:designsystem")
include(":core:ui")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:database")
include(":core:navigation")

// Feature modules — uncomment as each task lands (see _specs/_ai/manifest.yaml).
include(":feature:home")
include(":feature:myapps")
include(":feature:appdetail")
include(":feature:auth")
include(":feature:onboarding")
include(":feature:testing")
include(":feature:profile")
include(":feature:inbox")
