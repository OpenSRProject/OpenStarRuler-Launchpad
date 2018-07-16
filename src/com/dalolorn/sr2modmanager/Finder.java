package com.dalolorn.sr2modmanager;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public class Finder extends SimpleFileVisitor<Path> {
	private final PathMatcher matcher;
	private Path result;

	Finder(String pattern) {
		matcher = FileSystems.getDefault()
				.getPathMatcher("glob:" + pattern);
	}

	// Compares the glob pattern against
	// the file or directory name.
	Path find(Path file) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			result = file;
			return file;
		}
		return null;
	}

	// Returns the result.
	Path getResult() {
		return result;
	}

	// Invoke the pattern matching
	// method on each file.
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
		if(find(file) != null)
			return FileVisitResult.TERMINATE;
		return CONTINUE;
	}

	// Invoke the pattern matching
	// method on each directory.
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
		if(find(dir) != null)
			return FileVisitResult.TERMINATE;
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) {
		System.err.println(exc);
		return CONTINUE;
	}
}
