package com.dalolorn.sr2modmanager.adapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public abstract class Finder<T> extends SimpleFileVisitor<Path> {
	@NotNull protected final PathMatcher matcher;

	Finder(@NotNull String pattern) {
		matcher = FileSystems.getDefault()
				.getPathMatcher("glob:" + pattern);
	}

	@Nullable
	protected abstract T find(Path file);

	@Nullable
	public abstract T getResult();

	// Invoke the pattern matching
	// method on each file.
	@Override
	@NotNull
	public FileVisitResult visitFile(
			@NotNull Path file,
			@NotNull BasicFileAttributes attrs
	) {
		if(find(file) != null)
			return FileVisitResult.TERMINATE;
		return CONTINUE;
	}

	// Invoke the pattern matching
	// method on each directory.
	@Override
	@NotNull
	public FileVisitResult preVisitDirectory(
			@NotNull Path dir,
			@NotNull BasicFileAttributes attrs
	) {
		if(find(dir) != null)
			return FileVisitResult.TERMINATE;
		return CONTINUE;
	}

	@Override
	@NotNull
	public FileVisitResult visitFileFailed(
			@NotNull Path file,
			@NotNull IOException exc
	) {
		System.err.println(exc);
		return CONTINUE;
	}
}
