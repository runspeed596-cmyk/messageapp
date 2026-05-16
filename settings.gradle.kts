pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Iranian mirrors — uncomment when on national network only
        // maven { url = uri("https://maven.irrepo.ir/public") }
        // maven { url = uri("https://maven.map.ir") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Iranian mirrors — uncomment when on national network only
        // maven { url = uri("https://maven.irrepo.ir/public") }
        // maven { url = uri("https://maven.map.ir") }
    }
}

rootProject.name = "MessageApp"
include(":app")
