package com.dalolorn.sr2modmanager.adapter;

import java.nio.file.*;

public class SingleFinder extends Finder<Path> {
	private Path result;

	public SingleFinder(String pattern) {
		super(pattern);
	}

	// Compares the glob pattern against
	// the file or directory name.
	protected Path find(Path file) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			result = file;
			return file;
		}
		return null;
	}

	// Returns the result.
	public Path getResult() {
		return result;
	}
}
