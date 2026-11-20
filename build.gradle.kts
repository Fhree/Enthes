import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.detekt) apply false
}

subprojects {
    apply(plugin = rootProject.libs.plugins.detekt.get().pluginId)

    extensions.configure<DetektExtension> {
        parallel = true
        source.setFrom(
            files(
                "src/main/kotlin",
                "src/test/kotlin",
                "src/commonMain/kotlin",
                "src/commonTest/kotlin",
                "src/androidMain/kotlin",
                "src/androidHostTest/kotlin",
                "src/androidDeviceTest/kotlin",
                "src/iosMain/kotlin",
                "src/iosTest/kotlin",
            )
        )
    }

    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
    }

    tasks.withType<Detekt>().configureEach {
        exclude("**/build/**")
    }
}