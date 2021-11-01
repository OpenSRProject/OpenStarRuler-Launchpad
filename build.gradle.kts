plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.jlink") version "2.24.4"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.13.0.202109080827-r")
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("org.jetbrains:annotations:22.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

}

javafx {
    version = "11.0.2"
    modules = mutableListOf("javafx.controls", "javafx.fxml")
}

jlink {
    launcher {
        name = "OSRLaunchpad"
    }

    mergedModuleName.set("io.github.openstarruler.launchpad.merged")

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
    mainClass.set("com.dalolorn.sr2modmanager.view.Main")
    mainModule.set("io.github.openstarruler.launchpad")
    executableDir = ""

    applicationDistribution.run {
        from(project.file("README.md")) {
            rename("README.md", "README.txt")
        }
        from(project.file("history.json"))
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
