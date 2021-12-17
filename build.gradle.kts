import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id("org.beryx.jlink") version "2.24.0"
    kotlin("jvm") version "1.6.10-RC"
}

repositories {
    mavenCentral()
}

val OPENJFX = "org.openjfx"
val JFX_VERSION = "11.0.2"
val JFX_MODULES = listOf(
    "javafx-base",
    "javafx-controls",
    "javafx-fxml",
    "javafx-graphics"
)
val SUPPORTED_PLATFORMS = listOf(
    "linux",
    "win"
)

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.11.1.202105131744-r")
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("org.jetbrains:annotations:20.1.0")
    implementation(kotlin("stdlib-jdk8"))

    JFX_MODULES.forEach { module ->
        SUPPORTED_PLATFORMS.forEach { platform ->
            implementation("$OPENJFX:$module:$JFX_VERSION:$platform")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

jlink {
    launcher {
        name = "SR2ModManager"
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    from(project.file("LICENSE.txt"))
}

var mainClass: String? by application.mainClass // dereference the Property
mainClass = "com.dalolorn.sr2modmanager.view.Main"
var mainModule: String? by application.mainModule
mainModule = "SR2ModManager"

application {
    executableDir = ""
}

tasks.withType<CreateStartScripts> {
    doLast {
        var windowsText = windowsScript.readText()

        windowsText = windowsText.replace(":fail\r\nrem", ":fail\r\npause\r\nrem")

        windowsScript.writeText(windowsText)
    }
}

distributions {
    main {
        contents {
            from(project.file("README.md")) {
                rename("README.md", "README.txt")
            }
            from(project.file("history.json"))
            from(project.file("LICENSE.txt"))
        }
    }
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}