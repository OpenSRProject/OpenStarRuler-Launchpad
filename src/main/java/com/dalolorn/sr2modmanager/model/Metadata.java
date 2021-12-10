package com.dalolorn.sr2modmanager.model;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Metadata {
	public static class Dependency {
		public String name;
		public String repository;
		public String branch;
		@Nullable public String modName;
	}

	public static class Mod {
		public String rootFolder;
		public List<Dependency> dependencies = new ArrayList<>();
	}

	public List<Dependency> dependencies = new ArrayList<>();
	public Map<String, Mod> mods = new HashMap<>();
}
