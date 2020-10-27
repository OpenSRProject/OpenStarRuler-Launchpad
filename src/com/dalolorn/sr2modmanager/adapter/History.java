package com.dalolorn.sr2modmanager.adapter;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class History {
    public List<String> history = new ArrayList<>();

    private History() {}

    private static History instance = new History();
    private static final int SIZE_LIMIT = 5;

    public static boolean load() throws IOException {
        File file = new File("history.json");
        if (!file.exists()) {
            getInstance().save();
            return false;
        } else {
            try (FileReader reader = new FileReader(file)) {
                instance = new Gson().fromJson(reader, History.class);
            }
        }
        return true;
    }

    public static History getInstance() {
        return instance;
    }

    public void save() throws IOException {
        File file = new File("history.json");
        if (!file.exists()) {
            file.createNewFile();
        }

        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write(new Gson().toJson(this));
        }
    }

    /**
     * Adds an item to the saved list (if distinct), removes last item if list length above SIZE_LIMIT
     *
     * @param url the url to be added
     */
    public void addItem(String url) throws IOException {
        // Removing it so it gets added to the start of the list again
        history.remove(url);

        history.add(0, url);
        while (history.size() > SIZE_LIMIT) {
            history.remove(SIZE_LIMIT);
        }
		save();

    }
}
