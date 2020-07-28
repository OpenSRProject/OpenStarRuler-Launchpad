package com.dalolorn.sr2modmanager.adapter;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Settings {
	public String gamePath = "..";

	private Settings() {}

	private static Settings instance = new Settings();

	public static boolean load() throws IOException {
		File file = new File("config.json");
		if(!file.exists()) {
			getInstance().save();
			return false;
		}
		else {
			try (FileReader reader = new FileReader(file)) {
				instance = new Gson().fromJson(reader, Settings.class);
			}
		}
		return true;
	}

	public static Settings getInstance() {
		return instance;
	}

	public void save() throws IOException {
		File file = new File("config.json");
		if(!file.exists()) {
			file.createNewFile();
		}

		try (FileWriter writer = new FileWriter(file, false)) {
			writer.write(new Gson().toJson(this));
		}
	}
}
