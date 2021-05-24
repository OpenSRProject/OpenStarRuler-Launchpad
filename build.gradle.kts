plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:4.11.0.201803080745-r")
    implementation("com.google.code.gson:gson:2.8.6")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

var mainClass: String? by application.mainClass // dereference the Property
mainClass = "com.dalolorn.sr2modmanager.view.Main"

application {
    executableDir = ""
}

distributions {
    main {
        contents {
            from(project.file("README.md"))
        }
    }
}
