package io.github.openstarruler.launchpad.adapter

import java.nio.file.Path

class SingleFinder(pattern: String) : Finder<Path?>(pattern) {
    // Returns the result.
    override var result: Path? = null
        private set

    // Compares the glob pattern against
    // the file or directory name.
    override fun find(file: Path): Path? {
        val name = file.fileName
        if (name != null && matcher.matches(name)) {
            result = file
            return file
        }
        return null
    }
}