package com.dalolorn.sr2modmanager.adapter;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Settings {
	public String gamePath = "..";
	public boolean installToUser = false;
	public boolean isFirstRun = true;

	private Settings() {}

	@NotNull private static Settings instance = new Settings();

	public static boolean load() throws IOException {
		var file = new File("config.json");
		if(!file.exists()) {
			getInstance().save();
			return false;
		}
		else {
			try (var reader = new FileReader(file)) {
				instance = new Gson().fromJson(reader, Settings.class);
			}
		}
		return true;
	}

	public String getModsFolder() {
		if(installToUser) {
			if (Utils.IS_WINDOWS) {
				// %USERPROFILE%/Documents/My Games/Star Ruler 2/mods
				return System.getProperty("user.home") + File.separator + "Documents" + File.separator + "My Games" + File.separator + "Star Ruler 2" + File.separator + "mods";
			}
			// %HOME%/.starruler2/mods
			else return System.getProperty("user.home") + File.separator + ".starruler2" + File.separator + "mods";
		}
		else return gamePath + File.separator + "mods";
	}

	@NotNull
	public static Settings getInstance() {
		return instance;
	}

	public void save() throws IOException {
		var file = new File("config.json");
		if(!file.exists()) {
			file.createNewFile();
		}

		try (var writer = new FileWriter(file, false)) {
			writer.write(new Gson().toJson(this));
		}
	}
}
