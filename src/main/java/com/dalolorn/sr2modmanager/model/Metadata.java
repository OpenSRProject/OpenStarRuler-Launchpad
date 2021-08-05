package com.dalolorn.sr2modmanager.model;

import java.util.ArrayList;
import java.util.List;

public class Metadata {
	public static class Dependency {
		public String name;
		public String repository;
		public String branch;
	}

	public List<Dependency> dependencies = new ArrayList<>();
}
