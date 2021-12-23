package io.github.openstarruler.launchpad.adapter

import java.nio.file.Path

class MultiFinder(pattern: String) : Finder<ArrayList<Path>?>(pattern) {
    // Returns the result.
    override val result = ArrayList<Path>()

    // Compares the glob pattern against
    // the file or directory name.
    override fun find(file: Path): ArrayList<Path>? {
        val name = file.fileName
        if (name != null && matcher.matches(name)) {
            result.add(file)
        }
        return null
    }
}
