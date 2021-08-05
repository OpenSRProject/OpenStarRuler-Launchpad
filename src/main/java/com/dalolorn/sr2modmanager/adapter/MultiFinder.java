package com.dalolorn.sr2modmanager.adapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;

public class MultiFinder extends Finder<ArrayList<Path>> {
	@NotNull private final ArrayList<Path> results = new ArrayList<>();

	public MultiFinder(@NotNull String pattern) {
		super(pattern);
	}

	// Compares the glob pattern against
	// the file or directory name.
	@Nullable
	protected ArrayList<Path> find(@NotNull Path file) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			results.add(file);
		}
		return null;
	}

	// Returns the result.
	@NotNull
	public ArrayList<Path> getResult() {
		return results;
	}
}
