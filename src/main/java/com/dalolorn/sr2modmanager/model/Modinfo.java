package com.dalolorn.sr2modmanager.model;

import com.dalolorn.sr2modmanager.adapter.sr2utils.DataReader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Modinfo {
	private static final int CUR_COMPATIBILITY = 200;

	public final boolean inRoot;
	public final String folderName;

	public String name;
	public String description;
	public String parentName;
	public int version;
	public int compatibility;
	public boolean isBase;
	public boolean listed;
	public boolean forCurrentVersion;
	public List<String> overrides = new ArrayList<>();
	public List<List<String>> overridePatterns = new ArrayList<>();
	public Map<Integer, String> fallbacks;

	public Modinfo(boolean inRoot, String folderName, File file) throws IOException {
		this.inRoot = inRoot;
		this.folderName = folderName;
		parse(new DataReader(file));
	}

	private void parse(DataReader file) {
		String key;
		String value;
		while(file.next()) {
			key = file.key;
			value = file.value;
			if(key.equalsIgnoreCase("Name")) {
				name = value;
			}
			else if(key.equalsIgnoreCase("Description")) {
				description = value;
			}
			else if(key.equalsIgnoreCase("Override")) {
				overrides.add(value);
				List<String> compiled = file.compilePattern(value);
				overridePatterns.add(compiled);
			}
			else if(key.equalsIgnoreCase("Derives From")) {
				if(value.equals("-"))
					parentName = "";
				else
					parentName = value;
			}
			else if(key.equalsIgnoreCase("Base Mod")) {
				isBase = Boolean.parseBoolean(value);
			}
			else if(key.equalsIgnoreCase("Listed")) {
				listed = Boolean.parseBoolean(value);
			}
			else if(key.equalsIgnoreCase("Version")) {
				version = Integer.parseInt(value);
			}
			else if(key.equalsIgnoreCase("Compatibility")) {
				compatibility = Integer.parseInt(value);
				forCurrentVersion = compatibility >= CUR_COMPATIBILITY;
			}
			else if(key.equalsIgnoreCase("Fallback")) {
				file.splitKeyValue(value, '=');
				fallbacks.put(Integer.parseInt(file.value), file.key);
			}
		}
	}
}
