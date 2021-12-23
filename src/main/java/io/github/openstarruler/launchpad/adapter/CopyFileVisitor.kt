package io.github.openstarruler.launchpad.adapter

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

class CopyFileVisitor(private val targetPath: Path) : SimpleFileVisitor<Path>() {
    private var sourcePath: Path? = null

    @Throws(IOException::class)
    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        if (sourcePath == null) {
            sourcePath = dir
        } else {
            Files.createDirectories(targetPath.resolve(sourcePath!!.relativize(dir)))
        }
        return FileVisitResult.CONTINUE
    }

    @Throws(IOException::class)
    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        Files.copy(file, targetPath.resolve(sourcePath!!.relativize(file)))
        return FileVisitResult.CONTINUE
    }
}
