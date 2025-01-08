package io.github.openstarruler.launchpad.model

class RepoMetadata {
    class Dependency {
        val sameSource: Boolean? = null
        var name: String? = null
        var repository: String? = null
        var branch: String? = null
        var modName: String? = null
    }

    class Mod(
        var rootFolder: String? = null,
        var dependencies: List<Dependency>? = null
    )

    var dependencies: List<Dependency>? = null
    var mods: Map<String, Mod>? = null
        get() = field ?: mapOf("Default mod" to Mod(null, this.dependencies))

}
