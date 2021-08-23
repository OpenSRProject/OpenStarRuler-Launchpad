package com.dalolorn.sr2modmanager.adapter.sr2utils;

import org.eclipse.jgit.lib.ObjectLoader;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class DataReader {
	public final String filename;
	public final List<String> lines;

	public boolean allowLines;
	public boolean fullLine = false;
	public boolean allowMultiline = true;
	public boolean skipComments = true;
	public boolean skipEmpty = true;

	public boolean inMultiline = false;
	public boolean squash = false;
	public int indent;
	public int lineIndex;
	public String line;
	public String key;
	public String value;

	public DataReader(@NotNull File file) throws IOException {
		this(file, true);
	}

	public DataReader(@NotNull File file, boolean allowLines) throws IOException {
		this.allowLines = allowLines;
		filename = file.getName();

		try (
				var fileStream = new FileInputStream(file);
				var fileReader = new BufferedReader(new InputStreamReader(fileStream))
		) {
			lines = fileReader.lines().collect(Collectors.toUnmodifiableList());
		}
	}

	public DataReader(@NotNull ObjectLoader fileLoader, @NotNull String name) throws IOException {
		this(fileLoader, name, true);
	}

	public DataReader(@NotNull ObjectLoader fileLoader, @NotNull String name, boolean allowLines) throws IOException {
		this.allowLines = allowLines;
		filename = name;

		try (
				var fileStream = fileLoader.openStream();
				var fileReader = new BufferedReader(new InputStreamReader(fileStream))
		) {
			lines = fileReader.lines().map(str -> str+"\n").collect(Collectors.toUnmodifiableList());
		}
	}

	public String position() {
		return filename + " | Line " + lineIndex;
	}

	public boolean feed(String feedLine) {
		line = feedLine;
		return handle();
	}

	public boolean handle() {
		// Handle multiline values
		if(inMultiline) {
			return handleMultilineValues();
		}

		// Cut off comments
		cutOffComments();

		// Get indent level
		if (getIndentLevel()) return false;

		// Detect comments
		if(skipEmpty && line.isEmpty())
			return false;

		boolean isKeyValue = splitKeyValue(line);

		if(!isKeyValue) {
			if(allowLines) {
				fullLine = true;
				return true;
			}
			else {
				return false;
			}
		}
		else {
			key = key.trim();
			value = value.trim();
			fullLine = false;

			// Detect multiline blocks
			if(allowMultiline && (value.equals("<<") || value.equals("<<|"))) {
				squash = value.length() == 3;
				value = "";
				inMultiline = true;
				return false;
			}

			return !key.isEmpty();
		}
	}

	private boolean handleMultilineValues() {
		line = line.trim();

		if(line.length() > 1) {
			if(line.charAt(line.length() - 1) == '\\')
				line = line.substring(0, line.length() - 1);
			else
				line = squash ? " " : "\n";
		}
		else {
			// Preserve empty lines in squash mode
			line += squash ? "\n\n" : "\n";
		}

		if(line.startsWith(">>")) {
			// Remove the last linebreak
			if(value.length() > 0 && value.charAt(value.length() - 1) == '\n')
				value = value.substring(0, value.length() - 1);
			inMultiline = false;
			return true;
		}
		else {
			value += line;
			return false;
		}
	}

	private void cutOffComments() {
		if(skipComments) {
			int commentIndex = line.indexOf("//");
			if(commentIndex != -1)
				line = line.substring(0, commentIndex);
		}
	}

	private boolean getIndentLevel() {
		int pos = line.length() - line.stripLeading().length();
		if(pos == 0) {
			if(skipEmpty)
				return true;
			indent = 0;
		}
		else {
			int rpos = line.substring(0, line.length() - line.stripTrailing().length()).length();
			if(rpos != 0)
				line = line.substring(pos, rpos - pos + 1);
			indent = pos;
		}
		return false;
	}

	public boolean next() {
		while(lineIndex < lines.size()) {
			line = lines.get(lineIndex).trim();
			lineIndex++;
			if(handle())
				return true;
		}

		return false;
	}

	public void reset() {
		lineIndex = 0;
	}

	public boolean splitKeyValue(String input) {
		return splitKeyValue(input, ':');
	}

	public boolean splitKeyValue(String input, char separator) {
		int index = input.indexOf(separator);
		if(index == -1)
			return false;
		key = input.substring(0, index);
		value = input.substring(index+1);
		return true;
	}

	public List<String> compilePattern(String pattern) {
		pattern = pattern.toLowerCase(Locale.ROOT);
		if(pattern.isEmpty())
			return Collections.emptyList();

		List<String> compiled = Arrays.asList(pattern.split("\\*"));
		if(pattern.endsWith("*"))
			compiled.add("");
		return compiled;
	}
}
