plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.10"
    id("org.beryx.jlink") version "2.24.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:5.11.1.202105131744-r")
    implementation("com.google.code.gson:gson:2.8.6")
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
        name = "SR2ModManager"
        noConsole = true
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

application {
    executableDir = ""
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
