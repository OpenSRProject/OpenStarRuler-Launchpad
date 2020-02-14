package com.dalolorn.sr2modmanager.model;

import java.util.ArrayList;

public class Metadata {
	public static class Dependency {
		public String name;
		public String repository;
		public String branch;
	}

	public ArrayList<Dependency> dependencies = new ArrayList<>();
}
