pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net") { name = "Fabric" }
        if (!System.getenv().containsKey("DONT_USE_ALIYUN_MIRROR")) {
            maven(url = "https://maven.aliyun.com/repository/public") {
                name = "Aliyun Mirror"
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}