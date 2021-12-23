package io.github.openstarruler.launchpad.adapter

import java.io.IOException
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

abstract class Finder<T> internal constructor(pattern: String) : SimpleFileVisitor<Path>() {
    protected val matcher: PathMatcher = FileSystems.getDefault().getPathMatcher("glob:$pattern")
    protected abstract fun find(file: Path): T?
    abstract val result: T?

    // Invoke the pattern matching
    // method on each file.
    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        return if (find(file) != null) FileVisitResult.TERMINATE else FileVisitResult.CONTINUE
    }

    // Invoke the pattern matching
    // method on each directory.
    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        return if (find(dir) != null) FileVisitResult.TERMINATE else FileVisitResult.CONTINUE
    }

    override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
        System.err.println(exc)
        return FileVisitResult.CONTINUE
    }
}
