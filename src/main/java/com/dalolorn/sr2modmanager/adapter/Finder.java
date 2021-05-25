package com.dalolorn.sr2modmanager.adapter;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public abstract class Finder<T> extends SimpleFileVisitor<Path> {
	protected final PathMatcher matcher;

	Finder(String pattern) {
		matcher = FileSystems.getDefault()
				.getPathMatcher("glob:" + pattern);
	}

	protected abstract T find(Path file);

	public abstract T getResult();

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
