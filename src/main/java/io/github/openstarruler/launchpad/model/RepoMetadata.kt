package io.github.openstarruler.launchpad.model

class RepoMetadata {
    class Dependency {
        var name: String? = null
        var repository: String? = null
        var branch: String? = null
        var modName: String? = null
    }

    class Mod {
        var rootFolder: String? = null
        var dependencies: List<Dependency> = ArrayList()
    }

    var dependencies: List<Dependency>? = ArrayList()
    var mods: Map<String?, Mod?> = HashMap()
}