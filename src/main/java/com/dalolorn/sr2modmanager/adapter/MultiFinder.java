package com.dalolorn.sr2modmanager.adapter;

import java.nio.file.*;
import java.util.ArrayList;

public class MultiFinder extends Finder<ArrayList<Path>> {
	private ArrayList<Path> results = new ArrayList<>();

	public MultiFinder(String pattern) {
		super(pattern);
	}

	// Compares the glob pattern against
	// the file or directory name.
	protected ArrayList<Path> find(Path file) {
		Path name = file.getFileName();
		if (name != null && matcher.matches(name)) {
			results.add(file);
		}
		return null;
	}

	// Returns the result.
	public ArrayList<Path> getResult() {
		return results;
	}
}
