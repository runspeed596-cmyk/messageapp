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
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            authentication {
                create<BasicAuthentication>("basic")
            }
            credentials {
                // Do not change the username
                username = "mapbox"
                // Use the secret token you supplied
                password = "sk.eyJ1IjoiaWxpYXhwIiwiYSI6ImNtbDd3bHg4dTAwM2szY3IwdzN6Z3JyazkifQ.hKJt723NGEIg0HWFjUNG6Q"
            }
        }
    }
}

rootProject.name = "MessageApp"
include(":app")
