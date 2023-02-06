import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.13"
    id("org.javamodularity.moduleplugin") version "1.8.12"
    id("org.beryx.jlink") version "2.26.0"
    kotlin("jvm") version "1.8.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(kotlin("bom")))
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.0.0.202111291000-r")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains:annotations:6.4.0.202211300538-r")
    implementation(kotlin("stdlib-jdk8"))

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
    implementation("com.squareup.okhttp3:okhttp")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

javafx {
    version = "19.0.2.1"
    modules = mutableListOf("javafx.controls", "javafx.fxml")
}

jlink {
    launcher {
        name = "OSRLaunchpad"
    }
    // Remove kotlin builtin metadata files, to not get problems with JPMS because of duplicate packages (we don't use kotlin-reflect anyways)
    jarExclude("kotlin-stdlib-common", "**/*")

    // Add SSL support
    addOptions("--add-modules", "jdk.crypto.cryptoki")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Jar> {
    from(project.file("LICENSE.txt"))
}

application {
    mainClass.set("io.github.openstarruler.launchpad.view.Main")
    mainModule.set("io.github.openstarruler.launchpad")
    executableDir = ""

    applicationDistribution.run {
        from(project.file("README.md")) {
            rename("README.md", "README.txt")
        }
        from(project.file("LICENSE.txt"))
    }
}

tasks.withType<CreateStartScripts> {
    applicationName = "OSRLaunchpad"

    doLast {
        var windowsText = windowsScript.readText()

        windowsText = windowsText.replace(":fail\r\nrem", ":fail\r\npause\r\nrem")

        windowsScript.writeText(windowsText)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}
