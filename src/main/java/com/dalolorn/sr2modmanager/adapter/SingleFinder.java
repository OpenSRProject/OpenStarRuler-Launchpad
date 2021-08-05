package com.dalolorn.sr2modmanager.adapter;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class SingleFinder extends Finder<Path> {
	@Nullable
	private Path result;

	public SingleFinder(String pattern) {
		super(pattern);
	}

	// Compares the glob pattern against
	// the file or directory name.
	@Nullable
	protected Path find(Path file) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			result = file;
			return file;
		}
		return null;
	}

	// Returns the result.
	@Nullable
	public Path getResult() {
		return result;
	}
}
