pluginManagement {
    repositories {
        // 国内优先：阿里云 Gradle Plugin / Public，失败再回落官方
        maven(url = "https://maven.aliyun.com/repository/gradle-plugin")
        maven(url = "https://maven.aliyun.com/repository/public")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://maven.aliyun.com/repository/spring")
        maven(url = "https://maven.aliyun.com/repository/central")
        mavenCentral()
    }
}

rootProject.name = "flystem-spring-crud-api"
