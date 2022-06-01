package io.github.openstarruler.launchpad.adapter

import java.nio.file.Path

class MultiFinder(pattern: String) : Finder<MutableList<Path>?>(pattern) {
    // Returns the result.
    override val result = mutableListOf<Path>()

    // Compares the glob pattern against
    // the file or directory name.
    override fun find(file: Path): MutableList<Path>? {
        val name = file.fileName
        if (name != null && matcher.matches(name)) {
            result.add(file)
        }
        return null
    }
}
